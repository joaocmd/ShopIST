package pt.ulisboa.tecnico.cmov.shopist.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.ulisboa.tecnico.cmov.shopist.CreatePantryActivity
import pt.ulisboa.tecnico.cmov.shopist.LocationPickerActivity
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST

/**
 * A simple [Fragment] subclass.
 * Use the [PantriesList.newInstance] factory method to
 * create an instance of this fragment.
 */
class PantriesList : Fragment() {

    private lateinit var recyclerAdapter: PantriesListAdapter

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
        recyclerAdapter = PantriesListAdapter(globalData.pantries, requireActivity())
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

    inner class PantriesListAdapter(
        var list: Array<PantryList>,
        private val activity: FragmentActivity
    ) :
        RecyclerView.Adapter<PantriesListAdapter.ViewHolder>() {

        inner class ViewHolder(val view: View, val activity: FragmentActivity) : RecyclerView.ViewHolder(view) {
            private val textView: TextView = view.findViewById(R.id.rowText)

            fun bind(pantryList: PantryList) {
                textView.text = pantryList.title

                val cardView: View = view.findViewById(R.id.rowCard)
                cardView.setOnClickListener {
                    view.findNavController().navigate(
                        R.id.action_nav_list_pantries_to_nav_pantry,
                        bundleOf(Pantry.ARG_PANTRY_ID to pantryList.uuid.toString())
                    )
                }
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.recycler_view_row, viewGroup, false)

            return ViewHolder(view, activity)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(list[position])
        }

        override fun getItemCount() = list.size
    }
}