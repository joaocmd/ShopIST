package pt.ulisboa.tecnico.cmov.shopist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.Product
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST

class CreateProduct : AppCompatActivity() {

    private lateinit var associatedPantry: PantryList
    private var idx : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get pantry list
        idx = intent.getIntExtra(ListPantriesActivity.GET_PANTRY_INDEX_INT, 0) // FIXME: Default value must not be 0

        setContentView(R.layout.activity_create_product)
    }

    fun onCreateProduct(view: View) {

        val productName = findViewById<EditText>(R.id.productName).text.toString()
        val productQuantity : Int = findViewById<EditText>(R.id.productQuantity).text.toString().toInt()

        val globalData = applicationContext as ShopIST
        globalData.getPantryList( idx ?: 0).addProduct(Product(productName, productQuantity))

        //get current pantry
        //create and add product
        /*
        val newPantry = PantryList(findViewById<EditText>(R.id.editTextPantryName).text.toString())
        //newPantry.addProduct(Product("1-Product1"))
        //newPantry.addProduct(Product("1-Product2"))
        //newPantry.addProduct(Product("1-Product3")
        globalData.addPantryList(
                newPantry
        )
        */
        finish()
    }
}