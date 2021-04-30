package pt.ulisboa.tecnico.cmov.shopist.ui.shoppings

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.TopBarController
import pt.ulisboa.tecnico.cmov.shopist.TopBarItems
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.domain.Store
import pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList.ShoppingList
import pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList.ShoppingListItem
import pt.ulisboa.tecnico.cmov.shopist.ui.dialogs.ConfirmationDialog
import pt.ulisboa.tecnico.cmov.shopist.ui.dialogs.ImageFullScreenDialog
import pt.ulisboa.tecnico.cmov.shopist.ui.products.ProductUI
import pt.ulisboa.tecnico.cmov.shopist.utils.API
import java.io.File
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [ShoppingListUI.newInstance] factory method to
 * create an instance of this fragment.
 */
class ShoppingListUI : Fragment() {

    private lateinit var root: View
    private lateinit var shoppingList: ShoppingList
    private lateinit var store: Store
    private lateinit var storeId: UUID
    private lateinit var recyclerAdapter: ShoppingListAdapter
    private lateinit var globalData: ShopIST

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_store_shopping_list, container, false)

        // Hide pantry quantities
        root.findViewById<ImageView>(R.id.pantryQuantityDisplay).visibility = View.GONE

        val listView: RecyclerView = root.findViewById(R.id.productsList)
        recyclerAdapter = ShoppingListAdapter(shoppingList)
        listView.layoutManager = LinearLayoutManager(context)
        listView.adapter = recyclerAdapter

        // Checkout button
        root.findViewById<View>(R.id.okButton).setOnClickListener { confirmCheckout() }

        return root
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
        getPrices()

        // Verify if some product should not appear in this list
        shoppingList.items.removeIf {
            !it.product.hasStore(store.uuid)
        }

        recyclerAdapter.notifyDataSetChanged()
        globalData.callbackDataSetChanged = {
            // shoppingList = globalData.getShoppingList(storeId)
            recyclerAdapter.shoppingList = shoppingList
            recyclerAdapter.notifyDataSetChanged()
        }

        setEnableButtons(globalData.isAPIConnected)

        // Get product order
        store.location?.let { storeLocation ->
            API.getInstance(requireContext()).getProductOrder(
                storeLocation,
                shoppingList.items.map { it.product },
                { order ->
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

                    globalData.callbackDataSetChanged?.invoke()
                },
                {
                    // Ignore
                }
            )
        }

        // TODO: Get one image for each product
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menuRoot = menu
        val items = mutableListOf(TopBarItems.Edit, TopBarItems.Delete)
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
        shoppingList.saveChanges()
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

            // TODO: What happens if we can't reach the server? Do the products stay the same?
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

    inner class ShoppingListAdapter(var shoppingList: ShoppingList) :
        RecyclerView.Adapter<ShoppingListAdapter.ViewHolder>() {


        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
            private val imageView: ImageView = view.findViewById(R.id.productImageView)
            private val textView: TextView = view.findViewById(R.id.rowText)
            private val pantryQuantityView : TextView = view.findViewById(R.id.pantryQuantityDisplay)
            private val needingQuantityView : TextView = view.findViewById(R.id.needingQuantityDisplay)
            private val cartQuantityView : TextView = view.findViewById(R.id.cartQuantityDisplay)

            fun bind(item: ShoppingListItem) {
                textView.text = item.product.getTranslatedName()
                val quantities = item.getAllQuantities()
                pantryQuantityView.text = quantities.pantry.toString()
                needingQuantityView.text = quantities.needing.toString()
                cartQuantityView.text = quantities.cart.toString()

                view.setOnLongClickListener {
                    view.findNavController().navigate(
                        R.id.action_nav_store_shopping_list_to_nav_view_product,
                        bundleOf(
                            ProductUI.ARG_PRODUCT_ID to item.product.uuid.toString()
                        )
                    )
                    true
                }

                view.setOnClickListener {
                    // set current item because we can't pass object references in bundles
                    (activity?.applicationContext as ShopIST).currentShoppingListItem = item
                    findNavController()
                        .navigate(R.id.action_nav_store_shopping_list_to_nav_store_shopping_list_item)
                }

                imageView.setOnClickListener {
                    displayFullScreenImage(it as ImageView)
                }
                // Set last image
                if (item.product.images.size > 0) {
                    // TODO: Use cache to get image
                    // Get image from cache
                    if (item.product.barcode !== null) {
                        API.getInstance(requireContext()).getProductImages(item.product, { imageIds ->
                            val lastImage = imageIds[imageIds.size-1]

                            // Get image from cache
                            globalData.imageCache.getAsImage(UUID.fromString(lastImage), { image ->
                                root.findViewById<ImageView>(R.id.productImageView).setImageBitmap(image)
                            }, { })

                            item.product.images = imageIds.toMutableList()
                        }, {
                            // Verify if locally we have the image
                            if (item.product.images.size > 0) {
                                val imageFileName = item.product.getLastImageName()
                                val imagePath = File(globalData.getImageFolder(), imageFileName)
                                val imageBitmap = BitmapFactory.decodeFile(imagePath.absolutePath)
                                view.findViewById<ImageView>(R.id.productImageView).setImageBitmap(imageBitmap)
                            }
                        })
                    } else {
                        val imageFileName = item.product.getLastImageName()
                        val imagePath = File(globalData.getImageFolder(), imageFileName)
                        val imageBitmap = BitmapFactory.decodeFile(imagePath.absolutePath)
                        view.findViewById<ImageView>(R.id.productImageView).setImageBitmap(imageBitmap)
                    }
                }
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.product_row, viewGroup, false)

            // Hide pantry quantities
            view.findViewById<TextView>(R.id.pantryQuantityDisplay).visibility = View.GONE

            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(shoppingList.items[position])
        }

        override fun getItemCount() = shoppingList.items.size
    }
}