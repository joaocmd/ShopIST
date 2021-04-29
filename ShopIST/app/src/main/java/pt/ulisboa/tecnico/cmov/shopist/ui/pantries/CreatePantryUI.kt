package pt.ulisboa.tecnico.cmov.shopist.ui.pantries

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.LatLng
import pt.ulisboa.tecnico.cmov.shopist.LocationPickerActivity
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.TopBarController
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.utils.API
import java.util.*

class CreatePantryUI : Fragment() {

    private lateinit var root: View
    private lateinit var editTitle: EditText
    private var coords: LatLng? = null
    private var editPantry: PantryList? = null

    companion object {
        const val GET_PANTRY_LOCATION = 1
        const val ARG_PANTRY_ID = "pantryId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val pantryId = UUID.fromString(it.getString(ARG_PANTRY_ID))
            val globalData = requireActivity().applicationContext as ShopIST
            editPantry = globalData.getPantryList(pantryId)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_pantries_new, container, false)
        editTitle = root.findViewById(R.id.pantryName)
        root.findViewById<View>(R.id.okButton) .setOnClickListener { createPantry() }
        root.findViewById<View>(R.id.chooseLocationButton) .setOnClickListener { chooseLocation() }
        root.findViewById<View>(R.id.deleteLocationButton) .setOnClickListener { deleteLocation() }

        if (editPantry != null) {
            root.findViewById<Button>(R.id.okButton).text = getString(R.string.edit_pantry_complete)
            root.findViewById<TextView>(R.id.textView).text = getString(R.string.edit_pantry)

            root.findViewById<EditText>(R.id.pantryName).setText(editPantry!!.name)

            if (editPantry!!.location != null) {
                root.findViewById<TextView>(R.id.locationMessage).text = getString(R.string.location_set)
                root.findViewById<Button>(R.id.chooseLocationButton).text = getString(R.string.choose_new_location)
                root.findViewById<Button>(R.id.deleteLocationButton).visibility = View.VISIBLE
                coords = editPantry!!.location!!
            }
        }

        return root
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(editPantry != null) {

            TopBarController.noOptionsMenu(
                menu,
                requireActivity(),
                getString(R.string.edit_pantry)
            )
        }
        else {
            TopBarController.noOptionsMenu(
                menu,
                requireActivity(),
                getString(R.string.create_pantry)
            )
        }
    }

    private fun deleteLocation() {
        coords = null
        root.findViewById<TextView>(R.id.locationMessage).text = getString(R.string.location_not_set)
        root.findViewById<Button>(R.id.deleteLocationButton).visibility = View.INVISIBLE
        root.findViewById<Button>(R.id.chooseLocationButton).text = getString(R.string.choose_location)
    }

    private fun createPantry() {
        val globalData = activity?.applicationContext as ShopIST

        val title = editTitle.text.toString()

        if (title.length <= 1) {
            Toast.makeText(context, "First type a title.", Toast.LENGTH_SHORT).show()
            return
        }

        if (editPantry == null) {
            val newPantry = PantryList(title)
            newPantry.location = coords
            globalData.addPantryList(newPantry)
        } else {
            editPantry!!.name = title
            editPantry!!.location = coords


            if (editPantry!!.isShared) {
                API.getInstance(requireContext()).updatePantry(editPantry!!)
            }
        }

        // Save data in file
        globalData.savePersistent()
        findNavController().popBackStack()
    }

    private fun chooseLocation() {
        val intent = Intent(activity?.applicationContext, LocationPickerActivity::class.java)
        startActivityForResult(intent, GET_PANTRY_LOCATION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GET_PANTRY_LOCATION && resultCode == AppCompatActivity.RESULT_OK) {
            if (data !== null) {
                val lat = data.getDoubleExtra(LocationPickerActivity.LATITUDE, 0.0)
                val lon = data.getDoubleExtra(LocationPickerActivity.LONGITUDE, 0.0)
                coords = LatLng(lat, lon)
                // Set UI
                root.findViewById<TextView>(R.id.locationMessage).text = getString(R.string.location_set)

                root.findViewById<Button>(R.id.chooseLocationButton).text = getString(R.string.choose_new_location)
                root.findViewById<Button>(R.id.deleteLocationButton).visibility = View.VISIBLE

                Log.d(ShopIST.TAG, "Received - Lat: $lat, Lon: $lon")
            }
        } else if (requestCode == GET_PANTRY_LOCATION && resultCode == AppCompatActivity.RESULT_CANCELED) {
            Log.d(ShopIST.TAG, "Location canceled")
            root.findViewById<TextView>(R.id.locationMessage).text = getString(R.string.location_not_set)
            coords = null
        }
    }
}