package pt.ulisboa.tecnico.cmov.shopist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST

class CreatePantry : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_pantry_list)
    }

    fun onCreatePantry(view: View) {
        val globalData = applicationContext as ShopIST

        val newPantry = PantryList(findViewById<EditText>(R.id.editTextPantryName).text.toString())
        //newPantry.addProduct(Product("1-Product1"))
        //newPantry.addProduct(Product("1-Product2"))
        //newPantry.addProduct(Product("1-Product3")
        globalData.addPantryList(
            newPantry
        )

        // Save data in file
        globalData.savePersistent()

        finish()
    }
}