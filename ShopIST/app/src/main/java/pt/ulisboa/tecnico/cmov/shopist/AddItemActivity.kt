package pt.ulisboa.tecnico.cmov.shopist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import pt.ulisboa.tecnico.cmov.shopist.domain.Item
import pt.ulisboa.tecnico.cmov.shopist.domain.Product
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST


class AddItemActivity : AppCompatActivity() {

    private var idx : Int = 0
    private var selectedProduct: Product? = null
    private var products = emptyList<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get pantry list
        idx = intent.getIntExtra(PantriesListActivity.GET_PANTRY_INDEX_INT, 0) // FIXME: Default value must not be 0

        setContentView(R.layout.activity_add_item)
        initListProducts()
    }

    override fun onResume() {
        super.onResume()

        // Update list with new products
        val productsList = findViewById<RadioGroup>(R.id.productsList)
        val globalData = applicationContext as ShopIST
        products = globalData.getAllProducts()
        productsList.removeAllViews()
        addProductsToGroup(products, productsList)
    }

    private fun initListProducts() {
        val globalData = applicationContext as ShopIST
        products = globalData.getAllProducts()

        val productsList = findViewById<RadioGroup>(R.id.productsList)
        addProductsToGroup(products, productsList)
    }

    private fun addProductsToGroup(products: List<Product>, productsList: RadioGroup) {
        for ((index, product) in products.listIterator().withIndex()) {
            // TODO: Discuss if a product in pantry list should appear here
            val radioButton = RadioButton(this)
            radioButton.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            radioButton.text = product.name
            radioButton.id = index
            productsList.addView(radioButton)
        }

        productsList.setOnCheckedChangeListener { _, checkedId ->
            selectedProduct = products[checkedId]
        }
    }

    fun onAddItem(view: View) {
        if (selectedProduct === null) {
            // FIXME: Stringify this text
            Toast.makeText(applicationContext, "First select a product.", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if is a number
        val pantryQuantityText = findViewById<EditText>(R.id.productQuantity).text.toString()
        try {
            if (pantryQuantityText.isEmpty() || pantryQuantityText.toInt() < 0) {
                // FIXME: Stringify this text
                Toast.makeText(applicationContext, "Select a quantity equal or above 0.", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: NumberFormatException) {
            // FIXME: Stringify this text
            Toast.makeText(applicationContext, "Select a quantity equal or above 0.", Toast.LENGTH_SHORT).show()
            Log.d(ShopIST.TAG, "Invalid number inserted: \'$pantryQuantityText\'")
            return
        }

        val pantryQuantity = pantryQuantityText.toInt()
        val globalData = applicationContext as ShopIST

        // Check if product is already in pantry
        val currentPantry = globalData.getPantryList(idx)

        val previousItem = currentPantry.hasProduct(selectedProduct!!)
        if (previousItem != null) {
            previousItem.pantryQuantity += pantryQuantity
        } else {
            currentPantry.addItem(Item(selectedProduct!!, pantryQuantity, 0, 0))
        }

        // Save data in file
        globalData.savePersistent()

        finish()
    }

    fun onCreateNewProduct(view: View) {
        val int = Intent(applicationContext, CreateProductActivity::class.java)
        startActivity(int)
    }
}