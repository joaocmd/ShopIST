package pt.ulisboa.tecnico.cmov.shopist

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST

class PantryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantry)

        // Get pantry list
        val idx = intent.getStringExtra(ListPantryActivity.GET_PANTRY_EXTRA)!!.toInt()
        val globalData = applicationContext as ShopIST
        val pantryList = globalData.getPantryList(idx)

        // TODO: Set products list
        val listView = findViewById<ListView>(R.id.recyclerView)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, pantryList.products)
        listView.adapter = adapter
    }

    fun onNewProduct(view: View) {
        // TODO:
    }
}