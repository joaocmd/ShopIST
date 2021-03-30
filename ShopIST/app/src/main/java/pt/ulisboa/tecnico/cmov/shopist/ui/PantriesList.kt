package pt.ulisboa.tecnico.cmov.shopist.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.CreatePantryActivity
import pt.ulisboa.tecnico.cmov.shopist.LocationPickerActivity
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.utils.RecyclerAdapter

/**
 * A simple [Fragment] subclass.
 * Use the [PantriesList.newInstance] factory method to
 * create an instance of this fragment.
 */
class PantriesList : Fragment() {

    private lateinit var recyclerAdapter: RecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //activity?.requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        //activity?.supportActionBar?.hide(); //hide the title bar
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_pantries_list, container, false)
        val recyclerView: RecyclerView = root.findViewById(R.id.recyclerView)

        val globalData = activity?.applicationContext as ShopIST
        if (globalData.pantries.isEmpty()) {
            globalData.startUp()
        }
        recyclerAdapter = RecyclerAdapter(globalData.pantries, requireActivity())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = recyclerAdapter

        root.findViewById<Button>(R.id.newPantryButton).setOnClickListener{ onNewPantry() }
        root.findViewById<Button>(R.id.dummyLocationButton).setOnClickListener{ dummyMap() }

        return root
    }

    override fun onResume() {
        updateData()
        super.onResume()
    }

    private fun updateData() {
        val globalData = activity?.applicationContext as ShopIST
        recyclerAdapter.list = globalData.pantries
        recyclerAdapter.notifyDataSetChanged()
    }

    // companion object {
    //     /**
    //      * Use this factory method to create a new instance of
    //      * this fragment using the provided parameters.
    //      *
    //      * @param param1 Parameter 1.
    //      * @param param2 Parameter 2.
    //      * @return A new instance of fragment PantriesList.
    //      */
    //     @JvmStatic
    //     fun newInstance(param1: String, param2: String) =
    //         PantriesList().apply {
    //             arguments = Bundle().apply {
    //                 putString(ARG_PARAM1, param1)
    //                 putString(ARG_PARAM2, param2)
    //             }
    //         }
    // }

    private fun onNewPantry() {
        val intent = Intent(activity?.applicationContext, CreatePantryActivity::class.java)
        startActivity(intent)
        // TODO:
    }

    // FIXME: This whole code needs to be removed (including the button in layout)
    val REQUEST_GET_MAP_LOCATION = 0;
    fun dummyMap() {
        val intent = Intent(activity?.applicationContext, LocationPickerActivity::class.java)
        startActivityForResult(intent, REQUEST_GET_MAP_LOCATION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GET_MAP_LOCATION && resultCode == AppCompatActivity.RESULT_OK) {
            if (data !== null) {
                val lat = data.getDoubleExtra(LocationPickerActivity.LATITUDE, 0.0)
                val lon = data.getDoubleExtra(LocationPickerActivity.LONGITUDE, 0.0)
                Log.d(ShopIST.TAG, "Lat: $lat, Lon: $lon")
            }
        } else if (requestCode == REQUEST_GET_MAP_LOCATION && resultCode == AppCompatActivity.RESULT_CANCELED) {
            Log.d(ShopIST.TAG, "Location canceled")
        }
    }
}