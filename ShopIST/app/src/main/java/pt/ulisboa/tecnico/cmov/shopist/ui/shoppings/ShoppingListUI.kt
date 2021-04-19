package pt.ulisboa.tecnico.cmov.shopist.ui.shoppings

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.core.os.bundleOf
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
import pt.ulisboa.tecnico.cmov.shopist.ui.pantries.PantryItemUI
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

    private lateinit var menuRoot: Menu

    companion object {
        const val ARG_STORE_ID = "pantryId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            storeId = UUID.fromString(it.getString(ARG_STORE_ID))
            val globalData = requireActivity().applicationContext as ShopIST
            shoppingList = globalData.getShoppingList(storeId)
            store = globalData.getStore(storeId)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_store_shopping_list, container, false)
        val listView: RecyclerView = root.findViewById(R.id.productsList)

        recyclerAdapter = ShoppingListAdapter(shoppingList)

        listView.layoutManager = LinearLayoutManager(context)
        listView.adapter = recyclerAdapter

        // Navigation Buttons
        root.findViewById<View>(R.id.cancelButton).setOnClickListener { cancel() }

        // Checkout button
        root.findViewById<View>(R.id.okButton).setOnClickListener { confirmCheckout() }

        /*
        // TODO: Improve the location of this button
        root.findViewById<View>(R.id.editStoreButton).setOnClickListener {
            editStore()
        }
        */

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

    fun editStore() {
        findNavController().navigate(
            R.id.action_nav_store_shopping_list_to_nav_create_shopping_list,
            bundleOf(
                CreateShoppingListUI.ARG_STORE_ID to storeId.toString()
            )
        )
    }

    override fun onResume() {
        getPrices()
        super.onResume()
        recyclerAdapter.notifyDataSetChanged()

        val globalData = requireActivity().applicationContext as ShopIST
        globalData.callbackDataSetChanged = {
            shoppingList = globalData.getShoppingList(storeId)
            recyclerAdapter.shoppingList = shoppingList
            recyclerAdapter.notifyDataSetChanged()
        }

        setEnableButtons(globalData.isAPIConnected)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menuRoot = menu
        val items = mutableListOf(TopBarItems.Edit)
        if (store.location != null) {
            items.add(TopBarItems.Directions)
        }
        TopBarController.optionsMenu(menu, requireActivity(), store.name, items)

        val globalData = requireActivity().applicationContext as ShopIST
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
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_checkout))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                saveAndReturn()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun saveAndReturn() {
        shoppingList.saveChanges()
        val pantriesToUpdate = shoppingList.getPantries().filter { p -> p.isShared }

        pantriesToUpdate.forEach {
            API.getInstance(requireContext()).updatePantry(it)
        }

        (requireActivity().applicationContext as ShopIST).savePersistent()

        Toast.makeText(context, getString(R.string.checkout_complete), Toast.LENGTH_SHORT).show()

        cancel()
    }

    private fun getPrices() {
        if (store.location === null) {
            return
        }
        val products = shoppingList.items.map { i -> i.product }.filter { p -> p.barcode !== null }.toSet().toList()
        val barcodeProducts = products.map { p -> p.barcode!! to p }.toMap()
        API.getInstance(requireContext()).getPricesForStore(
            products,
            store.location!!,
            { res ->
                res.forEach { entry ->
                    barcodeProducts[entry.key]?.setPrice(store, entry.value)
                }
                val globalData = requireActivity().applicationContext as ShopIST
                if (globalData.callbackDataSetChanged !== null) {
                    globalData.callbackDataSetChanged!!()
                }
            },
            {
                // FIXME: handle gracefully
            }
        )
    }


    inner class ShoppingListAdapter(var shoppingList: ShoppingList) :
        RecyclerView.Adapter<ShoppingListAdapter.ViewHolder>() {


        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
            private val textView: TextView = view.findViewById(R.id.rowText)
            private val pantryQuantityView : TextView = view.findViewById(R.id.pantryQuantityDisplay)
            private val needingQuantityView : TextView = view.findViewById(R.id.needingQuantityDisplay)
            private val cartQuantityView : TextView = view.findViewById(R.id.cartQuantityDisplay)

            fun bind(item: ShoppingListItem) {
                textView.text = item.product.name
                val quantities = item.getAllQuantities()
                pantryQuantityView.text = quantities.pantry.toString()
                needingQuantityView.text = quantities.needing.toString()
                cartQuantityView.text = quantities.cart.toString()

                val productView: LinearLayout = view.findViewById(R.id.firstLayout)
                val quantityView: LinearLayout = view.findViewById(R.id.thirdLayout)

                productView.setOnClickListener {
                    view.findNavController().navigate(
                        R.id.action_nav_store_shopping_list_to_nav_view_product,
                        bundleOf(
                            PantryItemUI.ARG_PRODUCT_ID to item.product.uuid.toString()
                        )
                    )
                }

                quantityView.setOnClickListener {
                    // set current item because we can't pass object references in bundles
                    (activity?.applicationContext as ShopIST).currentShoppingListItem = item
                    findNavController().navigate(R.id.action_nav_store_shopping_list_to_nav_store_shopping_list_item)
                }

                // Set last image
                if (item.product.images.size > 0) {
                    val globalData = requireActivity().applicationContext as ShopIST

                    val index = item.product.getLastImageIndex()
                    val imageFileName = "${item.product.uuid}_$index${ShopIST.IMAGE_EXTENSION}"
                    val imagePath = File(globalData.getImageFolder(), imageFileName)

                    val imageBitmap = BitmapFactory.decodeFile(imagePath.absolutePath)

                    view.findViewById<ImageView>(R.id.productImageView).setImageBitmap(imageBitmap)
                }
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.product_row, viewGroup, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(shoppingList.items[position])
        }

        override fun getItemCount() = shoppingList.items.size
    }
}