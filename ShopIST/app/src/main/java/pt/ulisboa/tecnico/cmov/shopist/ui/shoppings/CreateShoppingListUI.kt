package pt.ulisboa.tecnico.cmov.shopist.ui.shoppings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
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
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.domain.Store
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [CreateShoppingListUI.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateShoppingListUI : Fragment() {

    companion object {
        const val GET_STORE_LOCATION = 0;
    }

    private lateinit var root: View
    private lateinit var coords: LatLng
    private var isDefaultStore = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_stores_new, container, false)
        root.findViewById<View>(R.id.okButton).setOnClickListener { saveAndReturn() }
        root.findViewById<View>(R.id.chooseLocationButton).setOnClickListener { chooseLocation() }
        root.findViewById<View>(R.id.defaultStoreCheckBox).setOnClickListener { toggleDefaultStore() }

        return root
    }

    private fun saveAndReturn() {
        val globalData = requireActivity().applicationContext as ShopIST
        val title = root.findViewById<EditText>(R.id.titleInput).text.toString()

        if (title.length <= 1) {
            Toast.makeText(context, "First type a title.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!this::coords.isInitialized) {
            Toast.makeText(context, "First select a location.", Toast.LENGTH_SHORT).show()
            return
        }

        val newShoppingList = Store(title, coords)
        globalData.addStore(newShoppingList)
        if (isDefaultStore) {
            globalData.setDefaultStore(newShoppingList)
        }
        globalData.savePersistent()
        findNavController().popBackStack()
    }

    private fun chooseLocation() {
        val intent = Intent(activity?.applicationContext, LocationPickerActivity::class.java)
        startActivityForResult(intent, GET_STORE_LOCATION)
    }

    private fun toggleDefaultStore() {
        isDefaultStore = !isDefaultStore
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GET_STORE_LOCATION && resultCode == AppCompatActivity.RESULT_OK) {
            if (data !== null) {
                val lat = data.getDoubleExtra(LocationPickerActivity.LATITUDE, 0.0)
                val lon = data.getDoubleExtra(LocationPickerActivity.LONGITUDE, 0.0)
                coords = LatLng(lat, lon)
                // Set UI
                root.findViewById<TextView>(R.id.locationMessage).text = getString(R.string.shopping_location_set)
                val button = root.findViewById<Button>(R.id.okButton)
                button.isClickable = true
                button.isEnabled = true

                Log.d(ShopIST.TAG, "Received - Lat: $lat, Lon: $lon")
            }
        } else if (requestCode == GET_STORE_LOCATION && resultCode == AppCompatActivity.RESULT_CANCELED) {
            Log.d(ShopIST.TAG, "Location canceled")
            root.findViewById<TextView>(R.id.locationMessage).text = getString(R.string.shopping_location_not_set)
            root.findViewById<Button>(R.id.okButton).isClickable = false
            root.findViewById<Button>(R.id.okButton).isEnabled = false
        }
    }
}