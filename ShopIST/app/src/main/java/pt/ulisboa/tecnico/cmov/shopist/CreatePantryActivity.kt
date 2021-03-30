package pt.ulisboa.tecnico.cmov.shopist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST

class CreatePantryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_pantry_list)
    }

    fun onCreatePantry(view: View) {
        val globalData = applicationContext as ShopIST

        val newPantry = PantryList(findViewById<EditText>(R.id.editTitle).text.toString())
        globalData.addPantryList(newPantry)

        // Save data in file
        globalData.savePersistent()
        finish()
    }
}