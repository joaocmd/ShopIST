package pt.ulisboa.tecnico.cmov.shopist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.utils.RecyclerAdapter

class ListPantryActivity : AppCompatActivity() {
    companion object {
        const val GET_PANTRY_EXTRA = "pt.ulisboa.tecnico.cmov.shopist.GET_PANTRY_EXTRA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_pantry)

        initListPantries()
    }

    private fun initListPantries() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val globalData = applicationContext as ShopIST
        val adapter = RecyclerAdapter(this, globalData.pantries)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    fun onNewPantry(view: View) {
        // TODO:
    }
}