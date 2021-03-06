package pt.ulisboa.tecnico.cmov.shopist.ui.shoppings

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_stores_list.*
import pt.ulisboa.tecnico.cmov.shopist.BarcodeScannerActivity
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.TopBarController
import pt.ulisboa.tecnico.cmov.shopist.TopBarItems
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.domain.Store
import pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList.ShoppingList
import pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList.ShoppingListItem
import pt.ulisboa.tecnico.cmov.shopist.ui.dialogs.ConfirmationDialog
import pt.ulisboa.tecnico.cmov.shopist.ui.dialogs.ImageFullScreenDialog
import pt.ulisboa.tecnico.cmov.shopist.ui.pantries.AddItemUI
import pt.ulisboa.tecnico.cmov.shopist.ui.pantries.PantryItemUI
import pt.ulisboa.tecnico.cmov.shopist.ui.products.ProductUI
import pt.ulisboa.tecnico.cmov.shopist.utils.API
import java.io.File
import java.util.*

class ShoppingListUI : Fragment() {

    private lateinit var root: View
    private lateinit var shoppingList: ShoppingList
    private lateinit var store: Store
    private lateinit var storeId: UUID
    private lateinit var recyclerAdapter: ShoppingListAdapter
    private lateinit var globalData: ShopIST
    private var currentOrder: List<String>? = null

    private lateinit var menuRoot: Menu

    companion object {
        const val ARG_STORE_ID = "pantryId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            storeId = UUID.fromString(it.getString(ARG_STORE_ID))
            globalData = requireActivity().applicationContext as ShopIST
            globalData.productOrder.clear()
            shoppingList = globalData.getShoppingList(storeId)
            store = globalData.getStore(storeId)
            globalData.currentShoppingList = shoppingList
        }
        setHasOptionsMenu(true)
    }

    private fun setTotals() {
        root.findViewById<TextView>(R.id.totalNeedingQuantityDisplay).text = store.itemQuantityTotal(globalData.pantries.toList()).toString()
        root.findViewById<TextView>(R.id.totalCartQuantityDisplay).text = store.itemCheckoutTotal(globalData.pantries.toList()).toString()
        root.findViewById<TextView>(R.id.totalMoneyNeeded).text = store.itemPriceTotal(globalData.pantries.toList()).toString() + "???"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_store_shopping_list, container, false)

        // Hide pantry quantities
        root.findViewById<ImageView>(R.id.pantryQuantityDisplay).visibility = View.GONE
        root.findViewById<View>(R.id.transferOneItem).visibility = View.GONE

        setTotals()

        val listView: RecyclerView = root.findViewById(R.id.productsList)
        recyclerAdapter = ShoppingListAdapter(shoppingList)
        listView.layoutManager = LinearLayoutManager(context)
        listView.adapter = recyclerAdapter

        // Checkout button
        root.findViewById<View>(R.id.okButton).setOnClickListener { confirmCheckout() }
        root.findViewById<SwipeRefreshLayout>(R.id.swiperRefresh2).setOnRefreshListener { onRefresh(swiperRefresh2) }

        return root
    }

    private fun updateData(callback: (() -> Unit)? = null) {

        getPrices()

        // Verify if some product should not appear in this list
        shoppingList.items.removeIf {
            !it.product.hasStore(store.uuid)
        }

        recyclerAdapter.notifyDataSetChanged()
        globalData.callbackDataSetChanged = {
            shoppingList = globalData.getShoppingList(shoppingList.store!!.uuid)
            globalData.currentShoppingList = shoppingList
            recyclerAdapter.shoppingList = shoppingList

            currentOrder?.let { order ->
                val newOrder = shoppingList.items
                    .sortedWith { s1, s2 ->
                        val s1Barcode = s1.product.barcode
                        val s2Barcode = s2.product.barcode
                        when {
                            s1Barcode == null && s2Barcode == null -> 0
                            s2Barcode == null -> -1
                            s1Barcode == null -> 1
                            else -> order.indexOf(s1Barcode) - order.indexOf(s2Barcode)
                        }
                    }
                shoppingList.items = newOrder.toMutableList()
            }
            recyclerAdapter.notifyDataSetChanged()
            setTotals()
        }

        setEnableButtons(globalData.isAPIConnected)

        globalData.pantries.forEach {
            if (it.isShared) {
                // Check for updates
                API.getInstance(requireContext()).getPantry(it.uuid, { result ->
                    globalData.populateFromServer(result)
                    globalData.callbackDataSetChanged?.invoke()
                }, {
                })
            }
        }

        // Get product order
        store.location?.let { storeLocation ->
            API.getInstance(requireContext()).getProductOrder(
                storeLocation,
                shoppingList.items.map { it.product },
                { order ->
                    currentOrder = order
                    globalData.callbackDataSetChanged?.invoke()
                },
                {
                    // Ignore
                }
            )
        }

        callback?.invoke()
    }

    private fun onRefresh(refresh : SwipeRefreshLayout) {
        updateData {
            refresh.isRefreshing = false
        }
    }

    private fun setEnableButtons(enabled: Boolean) {
        if (store.isShared) {
            if (this::menuRoot.isInitialized) {
                TopBarController.setSharedOptions(menuRoot, enabled)
            }
        } else {
            if (this::menuRoot.isInitialized) {
                TopBarController.setSharedOptions(menuRoot, true)
            }
        }
        if (this::menuRoot.isInitialized) {
            TopBarController.setOnlineOptions(menuRoot, enabled)
        }
    }

    private fun editStore() {
        findNavController().navigate(
            R.id.action_nav_store_shopping_list_to_nav_create_shopping_list,
            bundleOf(
                CreateShoppingListUI.ARG_STORE_ID to storeId.toString()
            )
        )
    }

    override fun onResume() {
        super.onResume()
        updateData()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menuRoot = menu
        val items = mutableListOf(TopBarItems.Edit, TopBarItems.Delete, TopBarItems.Barcode)
        if (store.location != null) {
            items.add(TopBarItems.Directions)
        }
        TopBarController.optionsMenu(menu, requireActivity(), store.name, items)

        setEnableButtons(globalData.isAPIConnected)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> editStore()
            R.id.action_get_directions -> {
                val location = store.location!!
                val gmmIntentUri = Uri.parse("google.navigation:q=${location.latitude},${location.longitude}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }
            R.id.action_scan_barcode -> {
                val intent = Intent(activity?.applicationContext, BarcodeScannerActivity::class.java)
                startActivityForResult(intent, AddItemUI.GET_BARCODE_PRODUCT)
            }
            R.id.action_delete -> confirmDeleteStore()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun cancel() {
        // Shouldn't be needed but can't hurt
        (activity?.applicationContext as ShopIST).currentShoppingListItem = null
        findNavController().popBackStack()
    }

    private fun confirmCheckout() {
        ConfirmationDialog(
            requireContext(),
            getString(R.string.confirm_checkout),
            {
                saveAndReturn()
            }, {
            }
        )
    }

    private fun saveAndReturn() {
        shoppingList.saveToPantries()
        val pantriesToUpdate = shoppingList.getPantries().filter { p -> p.isShared }

        pantriesToUpdate.forEach {
            API.getInstance(requireContext()).updatePantry(it)
        }

        store.location?.let {
            API.getInstance(requireContext()).submitProductOrder(it, globalData.productOrder.toList())
        }

        (requireActivity().applicationContext as ShopIST).savePersistent()

        Toast.makeText(context, getString(R.string.checkout_complete), Toast.LENGTH_SHORT).show()

        cancel()
    }

    private fun getPrices() {
        store.location ?: return
        val products = shoppingList.items.map { i -> i.product }.filter { p -> p.barcode !== null }.toSet().toList()
        val barcodeProducts = products.map { p -> p.barcode!! to p }.toMap()
        API.getInstance(requireContext()).getPricesForStore(
            products,
            store.location!!,
            { res ->
                res.forEach { entry ->
                    barcodeProducts[entry.key]?.setPrice(store, entry.value)
                }
                globalData.callbackDataSetChanged?.invoke()
            },
            {
                // Ignore, can't update prices
            }
        )
    }

    private fun confirmDeleteStore() {
        ConfirmationDialog(
            requireContext(),
            getString(R.string.confirm_store_delete),
            {
                deleteStore()
            }, {}
        )
    }

    private fun deleteStore() {
        val globalData = requireContext().applicationContext as ShopIST

        if (store.isShared) {
            val productsToUpdate = globalData.getProductsWithStore(store.uuid)
            globalData.removeStore(store.uuid)

            productsToUpdate.forEach {
                API.getInstance(requireContext()).postProduct(it, {}, {})
            }

            globalData.savePersistent()

        } else {
            globalData.removeStore(store.uuid)
            globalData.savePersistent()
        }
    }

    fun displayFullScreenImage(imageView: ImageView) {
        val dialog = ImageFullScreenDialog(this, imageView)
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == AddItemUI.GET_BARCODE_PRODUCT) {
            data?.let {
                data.getStringExtra(BarcodeScannerActivity.BARCODE)?.let { barcode ->
                    shoppingList.items.find { item -> item.product.barcode == barcode }
                        ?.let { item ->
                            (requireActivity().applicationContext as ShopIST).currentShoppingListItem =
                                item
                            findNavController()
                                .navigate(R.id.action_nav_store_shopping_list_to_nav_store_shopping_list_item)
                        } ?: Toast.makeText(
                        context, String.format(
                            getString(R.string.no_such_product_with_barcode),
                            barcode,
                            shoppingList.store?.name
                        ), Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}

inner class ShoppingListAdapter(var shoppingList: ShoppingList) :
        RecyclerView.Adapter<ShoppingListAdapter.ViewHolder>() {

        inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
            private val imageView: ImageView = view.findViewById(R.id.productImageView)
            private val textView: TextView = view.findViewById(R.id.rowText)
            private val pantryQuantityView : TextView = view.findViewById(R.id.pantryQuantityDisplay)
            private val needingQuantityView : TextView = view.findViewById(R.id.needingQuantityDisplay)
            private val cartQuantityView : TextView = view.findViewById(R.id.cartQuantityDisplay)
            private val moneyView: TextView = view.findViewById(R.id.moneyNeeded)

            private fun changePrice(item: ShoppingListItem) {

                val quantities = item.getAllQuantities()
                val price = item.product.prices[store]
                if(price != null) {
                    moneyView.text = (price.toDouble() * quantities.cart).toString() + "???"
                }
                else {
                    moneyView.text = "---"
                }
            }

            fun bind(item: ShoppingListItem) {
                textView.text = item.product.getTranslatedName()
                val quantities = item.getAllQuantities()
                pantryQuantityView.text = quantities.pantry.toString()
                needingQuantityView.text = quantities.needing.toString()
                cartQuantityView.text = quantities.cart.toString()

                changePrice(item)

                view.setOnLongClickListener {
                    view.findNavController().navigate(
                        R.id.action_nav_store_shopping_list_to_nav_view_product,
                        bundleOf(
                            ProductUI.ARG_PRODUCT_ID to item.product.uuid.toString(),
                            ProductUI.ARG_STORE_ID to storeId.toString()
                        )
                    )
                    true
                }

                view.setOnClickListener {
                    // set current item because we can't pass object references in bundles
                    (requireActivity().applicationContext as ShopIST).currentShoppingListItem = item
                    findNavController()
                        .navigate(R.id.action_nav_store_shopping_list_to_nav_store_shopping_list_item)
                }

                imageView.setOnClickListener {
                    displayFullScreenImage(it as ImageView)
                }
                // Set last image
                if (item.product.barcode != null) {
                    API.getInstance(requireContext()).getProductImages(item.product, { imageIds ->
                        item.product.images = imageIds.toMutableList()

                        if (imageIds.isNotEmpty()) {
                            val lastImage = item.product.getLastImageId()

                            // Get image from cache
                            globalData.imageCache.getAsImage(UUID.fromString(lastImage), { image ->
                                imageView.setImageBitmap(image)
                                imageView.visibility = View.VISIBLE
                            }, { })
                        }
                    }, {
                        // Verify if locally we have the image
                        if (item.product.images.size > 0) {
                            val imageFileName = item.product.getLastImageName()
                            val imagePath = File(globalData.getImageFolder(), imageFileName)
                            val imageBitmap = BitmapFactory.decodeFile(imagePath.absolutePath)
                            imageView.setImageBitmap(imageBitmap)
                            imageView.visibility = View.VISIBLE
                        }
                    })
                }
                else if (item.product.images.size > 0) {
                    val uuid = UUID.fromString(item.product.getLastImageId())
                    val globalData = (requireContext().applicationContext as ShopIST)
                    globalData.imageCache.getAsImage(uuid, {
                        val imageView = view.findViewById<ImageView>(R.id.productImageView)
                        imageView.setImageBitmap(it)
                        imageView.visibility = View.VISIBLE
                    }, {})
                }
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.product_row, viewGroup, false)

            // Hide pantry quantities
            view.findViewById<TextView>(R.id.pantryQuantityDisplay).visibility = View.GONE
            view.findViewById<ImageButton>(R.id.transferOneItem).visibility = View.GONE

            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(shoppingList.items[position])
        }

        override fun getItemCount() = shoppingList.items.size
    }
}