package pt.ulisboa.tecnico.cmov.shopist.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.CreatePantryActivity
import pt.ulisboa.tecnico.cmov.shopist.LocationPickerActivity
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.utils.RecyclerAdapter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PantriesList.newInstance] factory method to
 * create an instance of this fragment.
 */
class PantriesList : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //activity?.requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        //activity?.supportActionBar?.hide(); //hide the title bar
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pantries_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PantriesList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PantriesList().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    override fun onResume() {
        initListPantries()
        super.onResume()
    }

    private fun initListPantries() {
        // FIXME: activity => view
        val recyclerView = activity?.findViewById<RecyclerView>(R.id.recyclerView)
        val globalData = activity?.applicationContext as ShopIST
        if (globalData.pantries.isEmpty()) {
            globalData.startUp()
        }
        val adapter = RecyclerAdapter(globalData.pantries)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = adapter
    }

    fun onNewPantry(view: View) {
        val intent = Intent(activity?.applicationContext, CreatePantryActivity::class.java)
        startActivity(intent)
        // TODO:
    }

    // FIXME: This whole code needs to be removed (including the button in layout)
    val REQUEST_GET_MAP_LOCATION = 0;
    fun dummyMap(view: View) {
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