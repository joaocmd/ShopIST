package pt.ulisboa.tecnico.cmov.shopist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import pt.ulisboa.tecnico.cmov.shopist.domain.Product
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST

class CreateProductActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_product)
    }

    fun onCreateProduct(view: View) {

        val productName = findViewById<EditText>(R.id.productName).text.toString()
        val product = Product(productName)

        val globalData = applicationContext as ShopIST
        globalData.addProduct(product)

        // Save data in file
        globalData.savePersistent()

        finish()
    }
}