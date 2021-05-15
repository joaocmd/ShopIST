package pt.ulisboa.tecnico.cmov.shopist.ui.shoppings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pt.ulisboa.tecnico.cmov.shopist.BarcodeScannerActivity
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.TopBarController
import pt.ulisboa.tecnico.cmov.shopist.TopBarItems
import pt.ulisboa.tecnico.cmov.shopist.domain.Item
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList.Quantity
import pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList.ShoppingListItem
import pt.ulisboa.tecnico.cmov.shopist.ui.dialogs.ConfirmationDialog
import pt.ulisboa.tecnico.cmov.shopist.ui.dialogs.PromptMessage
import pt.ulisboa.tecnico.cmov.shopist.ui.pantries.PantriesListUI
import pt.ulisboa.tecnico.cmov.shopist.ui.products.CreateProductUI
import pt.ulisboa.tecnico.cmov.shopist.ui.products.ProductUI
import pt.ulisboa.tecnico.cmov.shopist.utils.API
import java.net.URI
import java.util.*

class ShoppingListItemUI : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var shoppingListItem: ShoppingListItem
    private lateinit var shopIST: ShopIST
    private lateinit var totalPrice : TextView

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
        shoppingListItem.reset()

        totalPrice = root.findViewById<TextView>(R.id.priceValue)
        changePrice()

        root.findViewById<Button>(R.id.changePrice).setOnClickListener{ showDialogAddPrice() }
        root.findViewById<Button>(R.id.cancelButton).setOnClickListener{ cancel() }
        root.findViewById<Button>(R.id.okButton).setOnClickListener{ saveReturn() }

        recyclerView = root.findViewById(R.id.shoppingListItemList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ShoppingListItemListAdapter(shoppingListItem)

        return root
    }

    private fun changePrice() {

        val quantities = shoppingListItem.getAllQuantities()
        val price = shoppingListItem.product.prices[shoppingListItem.shoppingList.store]
        if(price != null) {
            totalPrice.text = (price.toDouble()).toString() + "€"
        }
        else {
            totalPrice.text = "---"
        }
    }

    private fun showDialogAddPrice() {
        // Inflate layout for dialog
        val inflater = requireActivity().layoutInflater
        val alert = AlertDialog.Builder(requireContext())
        alert.setTitle(getString(R.string.product_add_price_one_store))

        val alertRoot = inflater.inflate(R.layout.dialog_price_product_one_store, null)
        alert.setView(alertRoot)
        alert.setPositiveButton(getString(R.string.ok), null)
        alert.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }


        // Override Success button to make sure the user meets the conditions
        val dialog = alert.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(object :
            View.OnClickListener {
            override fun onClick(v: View?) {
                val priceText = alertRoot.findViewById<EditText>(R.id.productPrice).text.toString()
                try {
                    if (priceText.isEmpty() || priceText.toDouble() <= 0) {
                        Toast.makeText(
                            context,
                            getString(R.string.price_above_zero),
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(
                        context,
                        getString(R.string.price_above_zero),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(ProductUI.TAG, "Invalid number inserted: \'$priceText\'")
                    return
                }

                val price = priceText.toDouble()
                val priceStore = shoppingListItem.shoppingList.store
                val product = shoppingListItem.product

                if (priceStore !== null && product.barcode !== null) {
                    // Set price and send to server
                    product.setPrice(priceStore!!, price)

                    changePrice()
                    API.getInstance(requireContext()).submitPriceProduct(price, product,
                        priceStore!!, {
                            Log.d(ProductUI.TAG, "Product price sent")
                        }, {
                            Log.d(ProductUI.TAG, "Could not send price")
                        })

                    (requireActivity().applicationContext as ShopIST).savePersistent()
                    dialog.dismiss()
                } else if (priceStore == null) {
                    Toast.makeText(
                        context,
                        getString(R.string.first_choose_store),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Set local price
                    product.setPrice(priceStore!!, price)

                    changePrice()
                    dialog.dismiss()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (shoppingListItem.product.barcode == null) {
            shopIST.promptSettings.getPrompt(PromptMessage.ADD_BARCODE, this) {
                readBarcode()
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        TopBarController.optionsMenu(menu, requireActivity(),
            shoppingListItem.product.name, listOf(TopBarItems.Edit, TopBarItems.Share, TopBarItems.Barcode, TopBarItems.Delete))

        TopBarController.setOnlineOptions(menu, shopIST.isAPIConnected)
        if (shoppingListItem.product.isShared) {
            TopBarController.setSharedOptions(menu, shopIST.isAPIConnected)
        }
    }

    private fun shareProduct() {
        val product = shoppingListItem.product
        val globalData = activity?.applicationContext as ShopIST

        API.getInstance(requireContext()).postProduct(product, {
            // Stores product as shared
            product.share()
            globalData.addProduct(product)
            globalData.savePersistent()

            // Share code with the user
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT, getString(R.string.share_product_message_in_store).format(
                        product.name, shoppingListItem.shoppingList.store?.name, ShopIST.createUri(product)
                    )
                )
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }, {
            Toast.makeText(context, getString(R.string.error_getting_link), Toast.LENGTH_SHORT)
                .show()
        })
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
            R.id.action_share -> shareProduct()
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
        // Save item quantities and synchronize
        shoppingListItem.save()
        shoppingListItem.quantities.forEach { e ->
            API.getInstance(requireContext()).updatePantry(e.key)
        }

        val globalData = (requireContext().applicationContext as ShopIST)
        globalData.callbackDataSetChanged?.invoke()
        globalData.savePersistent()
        findNavController().popBackStack()
    }

    private fun cancel() {
        findNavController().popBackStack()
    }

    //--

    inner class ShoppingListItemListAdapter(
        var shoppingListItem: ShoppingListItem,
        var touchingDown : Boolean = false
    ) :
        RecyclerView.Adapter<ShoppingListItemListAdapter.ViewHolder>() {

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            private val textView: TextView = view.findViewById(R.id.rowText)
            private val needingView: TextView = view.findViewById(R.id.needingViewLocal)
            private val currentQuantity: TextView = view.findViewById(R.id.currentQuantity)

            @SuppressLint("ClickableViewAccessibility")
            fun bind(item: Item) {
                val pantryList = item.pantryList
                textView.text = pantryList.name

                val quantities = shoppingListItem.tempQuantities[pantryList]!!
                // Set initial value to needing value if not added any
                if (quantities.cart == 0) quantities.cart = quantities.needing
                needingView.text = quantities.needing.toString()
                currentQuantity.text = quantities.cart.toString()

                view.findViewById<View>(R.id.moreButton).setOnClickListener {
                    addCart(item, currentQuantity, quantities)
                }

                view.findViewById<View>(R.id.moreButton).setOnTouchListener { view, motionEvent ->
                    onTouchButton(view, motionEvent) {
                        lifecycleScope.launch {
                            addCartWhileHeld(item, currentQuantity, quantities)
                        }
                    }
                }

                view.findViewById<View>(R.id.lessButton).setOnClickListener {
                    subCart(item, currentQuantity, quantities)
                }

                view.findViewById<View>(R.id.lessButton).setOnTouchListener { view, motionEvent ->
                    onTouchButton(view, motionEvent) {
                        lifecycleScope.launch {
                            subCartWhileHeld(item, currentQuantity, quantities)
                        }
                    }
                }

                // Disable buttons if shared and not connect
                if (!shopIST.isAPIConnected && pantryList.isShared) {
                    view.findViewById<View>(R.id.moreButton).isEnabled = false
                    view.findViewById<View>(R.id.lessButton).isEnabled = false
                }
            }
        }

        private suspend fun addCartWhileHeld(item: Item, currentQuantity: TextView, quantities : Quantity) {
            var counterUntilWorking = 5;
            var currentCounter = 0;
            while(touchingDown) {
                currentCounter++
                if(currentCounter > counterUntilWorking) {
                    addCart(item, currentQuantity, quantities)
                }
                delay(100L)
            }
        }

        private suspend fun subCartWhileHeld(item: Item, currentQuantity: TextView, quantities : Quantity) {
            var counterUntilWorking = 5;
            var currentCounter = 0;
            while(touchingDown) {
                currentCounter++
                if(currentCounter > counterUntilWorking) {
                    subCart(item, currentQuantity, quantities)
                }
                delay(100L)
            }
        }

        private fun addCart(item: Item, currentQuantity: TextView, quantities : Quantity) {

            item.product.barcode?.let {
                shopIST.productOrder.add(it)
            }
            // Add and update view
            shoppingListItem.add(item.pantryList)
            //cartView.text = quantities.cart.toString()
            currentQuantity.text = quantities.cart.toString()
        }
        private fun subCart(item: Item, currentQuantity: TextView, quantities : Quantity) {

            // Subtract and update view
            shoppingListItem.remove(item.pantryList)
            //cartView.text = quantities.cart.toString()
            currentQuantity.text = quantities.cart.toString()
        }


        private fun onTouchButton(view: View, motionEvent: MotionEvent, callback: (() -> Unit) ): Boolean {
            when(motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchingDown = true
                    callback.invoke()
                }
                MotionEvent.ACTION_UP -> {
                    touchingDown = false
                }
            }
            return false
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