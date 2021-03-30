package pt.ulisboa.tecnico.cmov.shopist

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.utils.RecyclerAdapter


class PantriesListActivity : AppCompatActivity() {

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
        val adapter = RecyclerAdapter(globalData.pantries, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    fun onNewPantry(view: View) {
        val intent = Intent(applicationContext, CreatePantryActivity::class.java)
        startActivity(intent)
        // TODO:
    }

    // FIXME: This whole code needs to be removed (including the button in layout)
    val REQUEST_GET_MAP_LOCATION = 0;
    fun dummyMap(view: View) {
        val intent = Intent(applicationContext, LocationPickerActivity::class.java)
        startActivityForResult(intent, REQUEST_GET_MAP_LOCATION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GET_MAP_LOCATION && resultCode == RESULT_OK) {
            if (data !== null) {
                val lat = data.getDoubleExtra(LocationPickerActivity.LATITUDE, 0.0)
                val lon = data.getDoubleExtra(LocationPickerActivity.LONGITUDE, 0.0)
                Log.d(ShopIST.TAG, "Lat: $lat, Lon: $lon")
            }
        } else if (requestCode == REQUEST_GET_MAP_LOCATION && resultCode == RESULT_CANCELED) {
            Log.d(ShopIST.TAG, "Location canceled")
        }
    }
}