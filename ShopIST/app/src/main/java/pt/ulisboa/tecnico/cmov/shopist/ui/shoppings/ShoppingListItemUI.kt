package pt.ulisboa.tecnico.cmov.shopist.ui.shoppings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.BarcodeScannerActivity
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.TopBarController
import pt.ulisboa.tecnico.cmov.shopist.TopBarItems
import pt.ulisboa.tecnico.cmov.shopist.domain.Item
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList.Quantity
import pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList.ShoppingListItem
import pt.ulisboa.tecnico.cmov.shopist.ui.dialogs.ConfirmationDialog
import pt.ulisboa.tecnico.cmov.shopist.ui.products.CreateProductUI
import pt.ulisboa.tecnico.cmov.shopist.ui.pantries.PantriesListUI
import pt.ulisboa.tecnico.cmov.shopist.utils.API

class ShoppingListItemUI : Fragment() {
    // TODO: Add button to set to min and max quantity on cart

    private lateinit var recyclerView: RecyclerView
    private lateinit var shoppingListItem: ShoppingListItem
    private lateinit var shopIST: ShopIST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shopIST = requireActivity().applicationContext as ShopIST
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_store_shopping_list_item, container, false)
        shoppingListItem = shopIST.currentShoppingListItem!!

        root.findViewById<Button>(R.id.cancelButton).setOnClickListener{ cancel() }
        root.findViewById<Button>(R.id.okButton).setOnClickListener{ saveReturn() }

        // Set product title
        root.findViewById<TextView>(R.id.productTitleView).text = shoppingListItem.product.name

        recyclerView = root.findViewById(R.id.shoppingListItemList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ShoppingListItemListAdapter(shoppingListItem)

        return root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        TopBarController.optionsMenu(menu, requireActivity(),
            shoppingListItem.product.name, listOf(TopBarItems.Edit, TopBarItems.ScanBarcode, TopBarItems.Delete))

        TopBarController.setOnlineOptions(menu, shopIST.isAPIConnected)
        if (shoppingListItem.product.isShared) {
            TopBarController.setSharedOptions(menu, shopIST.isAPIConnected)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_scan_barcode -> {
                readBarcode()
            }
            R.id.action_edit -> {
                findNavController().navigate(
                    R.id.action_nav_store_shopping_list_item_to_nav_view_product,
                    bundleOf(
                        CreateProductUI.ARG_PRODUCT_ID to shoppingListItem.product.uuid.toString()
                    )
                )
            }
            R.id.action_delete -> deleteItemShoppingList()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun deleteItemShoppingList() {
        ConfirmationDialog(
            requireContext(),
            getString(R.string.confirm_pantry_item_delete),
            {
                // Remove item from shopping list
                if (shoppingListItem.product.isShared) {
                    shoppingListItem.shoppingList.removeItem(shoppingListItem.product.uuid)
                    // TODO: What happens if the server can't accept it? The product will remain equal, is that bad?
                    API.getInstance(requireContext()).postProduct(shoppingListItem.product, {
                        saveReturn()
                    }, {
                    })
                } else {
                    shoppingListItem.shoppingList.removeItem(shoppingListItem.product.uuid)
                    saveReturn()
                }
            },
            {}
        )
    }

    // Barcode getter
    private fun readBarcode() {
        val intent = Intent(activity?.applicationContext, BarcodeScannerActivity::class.java)
        startActivityForResult(intent, PantriesListUI.GET_BARCODE_PRODUCT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PantriesListUI.GET_BARCODE_PRODUCT && resultCode == AppCompatActivity.RESULT_OK) {
            if (data !== null) {
                val barcode = data.getStringExtra(BarcodeScannerActivity.BARCODE)
                shoppingListItem.product.barcode = barcode
                val globalData = activity?.applicationContext as ShopIST
                globalData.savePersistent()
                Toast.makeText(context, String.format(
                    getString(R.string.barcode_read),
                    barcode
                ), Toast.LENGTH_SHORT).show()
            }
        }
        else if (requestCode == PantriesListUI.GET_BARCODE_PRODUCT && resultCode == AppCompatActivity.RESULT_CANCELED) {
            Log.d(ShopIST.TAG, "Couldn't find barcode")
        }
    }

    private fun saveReturn() {
        (requireContext().applicationContext as ShopIST).savePersistent()
        findNavController().popBackStack()
    }

    private fun cancel() {
        findNavController().popBackStack()
    }

    //--

    inner class ShoppingListItemListAdapter(
        var shoppingListItem: ShoppingListItem
    ) :
        RecyclerView.Adapter<ShoppingListItemListAdapter.ViewHolder>() {

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            private val textView: TextView = view.findViewById(R.id.rowText)
            private val pantryView: TextView = view.findViewById(R.id.pantryViewLocal)
            private val needingView: TextView = view.findViewById(R.id.needingViewLocal)
            private val currentQuantity: TextView = view.findViewById(R.id.currentQuantity)

            fun bind(item: Item) {
                val pantryList = item.pantryList
                textView.text = pantryList.name

                val quantities = shoppingListItem.quantities[pantryList]!!
                pantryView.text = quantities.pantry.toString()
                needingView.text = quantities.needing.toString()
                //cartView.text = quantities.cart.toString()
                currentQuantity.text = quantities.cart.toString()

                view.findViewById<View>(R.id.moreButton).setOnClickListener {
                    item.product.barcode?.let {
                        shopIST.productOrder.add(it)
                    }
                    // Add and update view
                    shoppingListItem.add(item.pantryList)
                    //cartView.text = quantities.cart.toString()
                    currentQuantity.text = quantities.cart.toString()
                }
                view.findViewById<View>(R.id.lessButton).setOnClickListener {
                    // Subtract and update view
                    shoppingListItem.remove(item.pantryList)
                    //cartView.text = quantities.cart.toString()
                    currentQuantity.text = quantities.cart.toString()
                }
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.shopping_list_cart_quantity_row, viewGroup, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(shoppingListItem.items[position])
        }

        override fun getItemCount() = shoppingListItem.items.size
    }
}