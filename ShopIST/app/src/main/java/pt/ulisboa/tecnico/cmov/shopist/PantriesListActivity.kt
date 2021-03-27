package pt.ulisboa.tecnico.cmov.shopist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.utils.RecyclerAdapter

class PantriesListActivity : AppCompatActivity() {
    companion object {
        const val GET_PANTRY_INDEX_INT = "shopist.ListPantryActivity.GET_PANTRY_INDEX_INT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        supportActionBar?.hide(); //hide the title bar

        setContentView(R.layout.activity_list_pantries)

        initListPantries()
    }

    override fun onResume() {
        initListPantries()
        super.onResume()
    }

    private fun initListPantries() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val globalData = applicationContext as ShopIST
        if (globalData.pantries.isEmpty()) {
            globalData.startUp()
        }
        val adapter = RecyclerAdapter(globalData.pantries)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    fun onNewPantry(view: View) {
        val intent = Intent(applicationContext, CreatePantryActivity::class.java)
        startActivity(intent)
        // TODO:
    }
}