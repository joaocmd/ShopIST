package pt.ulisboa.tecnico.cmov.shopist.ui.pantries

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pt.ulisboa.tecnico.cmov.shopist.BarcodeScannerActivity
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.TopBarController
import pt.ulisboa.tecnico.cmov.shopist.TopBarItems
import pt.ulisboa.tecnico.cmov.shopist.domain.Item
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.Product
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.utils.API
import java.util.*


class AddItemUI : Fragment() {

    private lateinit var pantryList: PantryList
    private var selectedProduct: Product? = null
    private var products: List<Product> = emptyList()

    private lateinit var productsList: RadioGroup
    private lateinit var pantryQuantityView: EditText

    companion object {
        const val TAG = "${ShopIST.TAG}.addItemUI"
        const val ARG_PANTRY_ID = "pantryId"
        const val GET_BARCODE_PRODUCT = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val id = UUID.fromString(it.getString(ARG_PANTRY_ID))
            pantryList = (requireActivity().applicationContext as ShopIST).getPantryList(id)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_pantry_add_item, container, false)
        val globalData = requireContext().applicationContext as ShopIST
        products = globalData.getAllProducts()

        // Views
        productsList = root.findViewById(R.id.productsList)
        pantryQuantityView = root.findViewById(R.id.productQuantity)
        addProductsToGroup(products, productsList)

        // Navigation buttons
        root.findViewById<View>(R.id.okButton).setOnClickListener { onAddItem() }
        root.findViewById<View>(R.id.addNewProduct).setOnClickListener { onCreateNewProduct() }

        return root
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        TopBarController.optionsMenu(
            menu,
            requireActivity(),
            getString(R.string.add_item_title),
            listOf(TopBarItems.Barcode)
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
           R.id.action_scan_barcode -> scanBarcode()
           else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onResume() {
        super.onResume()

        // Update list with new products
        val globalData = activity?.applicationContext as ShopIST
        products = globalData.getAllProducts()
        addProductsToGroup(products, productsList)
    }

    private fun addProductsToGroup(products: List<Product>, radioGroup: RadioGroup) {
        radioGroup.removeAllViews()
        for ((index, product) in products.listIterator().withIndex()) {
            val radioButton = RadioButton(context)
            radioButton.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            radioButton.text = product.getTranslatedName()
            radioButton.id = index
            radioGroup.addView(radioButton)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId >= 0) {
                selectedProduct = products[checkedId]
            }
        }

        checkProduct(selectedProduct)
    }

    private fun checkProduct(product: Product?) {
        product?.let {
            selectedProduct = product
            val index = products.indexOf(product)
            productsList.check(index)

            Toast.makeText(
                context, String.format(
                    getString(R.string.product_barcode_read),
                    product.name
                ), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun onAddItem() {
        if (selectedProduct === null) {
            Toast.makeText(
                context,
                getString(R.string.product_needed),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Check if is a number
        val pantryQuantityText = pantryQuantityView.text.toString()
        try {
            if (pantryQuantityText.isEmpty() || pantryQuantityText.toInt() < 0) {
                Toast.makeText(context,
                    getString(R.string.quantity_equal_or_above_zero),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(context,
                getString(R.string.quantity_equal_or_above_zero),
                Toast.LENGTH_SHORT
            ).show()
            Log.d(ShopIST.TAG, "Invalid number inserted: \'$pantryQuantityText\'")
            return
        }

        val pantryQuantity = pantryQuantityText.toInt()

        // Check if product is already in pantry
        if (!pantryList.hasProduct(selectedProduct!!)) {
            pantryList.addItem(Item(selectedProduct!!, pantryList, pantryQuantity, 0, 0))

            if (pantryList.isShared) {
                selectedProduct!!.isShared = true
                API.getInstance(requireContext()).updatePantry(pantryList)
            }
        } else {
            Toast.makeText(context,
                getString(R.string.product_already_in_pantry),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Save data in file
        (activity?.applicationContext as ShopIST).savePersistent()
        findNavController().popBackStack()
    }

    private fun onCreateNewProduct() {
        findNavController().navigate(R.id.action_nav_add_item_to_nav_create_product)
    }

    private fun scanBarcode() {
        val intent = Intent(activity?.applicationContext, BarcodeScannerActivity::class.java)
        startActivityForResult(intent, GET_BARCODE_PRODUCT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == GET_BARCODE_PRODUCT) {
            data?.let {
                val barcode = data.getStringExtra(BarcodeScannerActivity.BARCODE)
                val globalData = (requireContext().applicationContext) as ShopIST

                // Select product associated with this barcode
                if (barcode != null) {
                    selectedProduct = globalData.getProductByBarcode(barcode)
                }
            }
        }
    }
}
