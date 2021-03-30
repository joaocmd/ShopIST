package pt.ulisboa.tecnico.cmov.shopist.ui.pantries

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.Item
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.Product
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import java.util.*


class AddItem : Fragment() {

    private lateinit var pantryList: PantryList
    private var selectedProduct: Product? = null
    private var products: List<Product> = emptyList()

    private lateinit var productsList: RadioGroup
    private lateinit var pantryQuantityView: EditText

    companion object {
        const val ARG_PANTRY_ID = "pantryId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val id = UUID.fromString(it.getString(ARG_PANTRY_ID))
            pantryList = (requireActivity().applicationContext as ShopIST).getPantryList(id)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_pantry_add_item, container, false)
        val globalData = activity?.applicationContext as ShopIST
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

    override fun onResume() {
        super.onResume()

        // Update list with new products
        val globalData = activity?.applicationContext as ShopIST
        products = globalData.getAllProducts()
        productsList.removeAllViews()
        addProductsToGroup(products, productsList)
    }

    private fun addProductsToGroup(products: List<Product>, productsList: RadioGroup) {
        for ((index, product) in products.listIterator().withIndex()) {
            // TODO: Discuss if a product in pantry list should appear here
            val radioButton = RadioButton(context)
            radioButton.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            radioButton.text = product.name
            radioButton.id = index
            productsList.addView(radioButton)
        }

        productsList.setOnCheckedChangeListener { _, checkedId ->
            selectedProduct = products[checkedId]
        }
    }

    private fun onAddItem() {
        if (selectedProduct === null) {
            // FIXME: Stringify this text
            Toast.makeText(context, "First select a product.", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if is a number
        val pantryQuantityText = pantryQuantityView.text.toString()
        try {
            if (pantryQuantityText.isEmpty() || pantryQuantityText.toInt() < 0) {
                // FIXME: Stringify this text
                Toast.makeText(context, "Select a quantity equal or above 0.", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: NumberFormatException) {
            // FIXME: Stringify this text
            Toast.makeText(context, "Select a quantity equal or above 0.", Toast.LENGTH_SHORT).show()
            Log.d(ShopIST.TAG, "Invalid number inserted: \'$pantryQuantityText\'")
            return
        }

        val pantryQuantity = pantryQuantityText.toInt()

        // Check if product is already in pantry
        if (!pantryList.hasProduct(selectedProduct!!)) {
            pantryList.addItem(Item(selectedProduct!!, pantryList, pantryQuantity, 0, 0))
        } else {
            // FIXME: disable this
            Toast.makeText(context, "The item is already in your pantry list", Toast.LENGTH_LONG).show()
        }

        // Save data in file
        (activity?.applicationContext as ShopIST).savePersistent()
        findNavController().popBackStack()
    }

    private fun onCreateNewProduct() {
        findNavController().navigate(R.id.action_nav_add_item_to_nav_create_product)
    }
}