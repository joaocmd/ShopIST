package pt.ulisboa.tecnico.cmov.shopist.ui.shoppings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
        const val GET_STORE_LOCATION = 0
        const val ARG_STORE_ID = "storeId"
    }

    private lateinit var root: View
    private var coords: LatLng? = null
    private var isDefaultStore = false
    private var editStore: Store? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val storeId = UUID.fromString(it.getString(ARG_STORE_ID))
            val globalData = requireActivity().applicationContext as ShopIST
            editStore = globalData.getStore(storeId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_stores_new, container, false)
        root.findViewById<View>(R.id.okButton).setOnClickListener { saveAndReturn() }
        root.findViewById<View>(R.id.chooseLocationButton).setOnClickListener { chooseLocation() }
        root.findViewById<View>(R.id.defaultStoreCheckBox).setOnClickListener { toggleDefaultStore() }

        if (editStore != null) {
            coords = editStore!!.location

            root.findViewById<EditText>(R.id.titleInput).setText(editStore!!.title)
            if (editStore!!.location != null) {
                root.findViewById<TextView>(R.id.locationMessage).text = getString(R.string.location_set)
            }
            root.findViewById<TextView>(R.id.textView).text = getString(R.string.edit_shopping)

            val globalData = requireActivity().applicationContext as ShopIST
            if (globalData.getDefaultStore() == editStore) {
                isDefaultStore = true
                root.findViewById<CheckBox>(R.id.defaultStoreCheckBox).isChecked = true
            }

            root.findViewById<Button>(R.id.okButton).text = getString(R.string.edit_shopping_complete)
        }
        return root
    }

    private fun saveAndReturn() {
        val globalData = requireActivity().applicationContext as ShopIST
        val title = root.findViewById<EditText>(R.id.titleInput).text.toString()

        if (title.length <= 1) {
            Toast.makeText(context, "First type a title.", Toast.LENGTH_SHORT).show()
            return
        }

        if (editStore == null) {
            val newShoppingList = Store(title)
            newShoppingList.location = coords
            globalData.addStore(newShoppingList)
            if (isDefaultStore) {
                globalData.setDefaultStore(newShoppingList)
            }
        } else {
            editStore!!.title = title
            editStore!!.location = coords
            if (isDefaultStore) {
                globalData.setDefaultStore(editStore!!)
            }
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
                root.findViewById<TextView>(R.id.locationMessage).text = getString(R.string.location_set)

                Log.d(ShopIST.TAG, "Received - Lat: $lat, Lon: $lon")
            }
        } else if (requestCode == GET_STORE_LOCATION && resultCode == AppCompatActivity.RESULT_CANCELED) {
            Log.d(ShopIST.TAG, "Location canceled")
            root.findViewById<TextView>(R.id.locationMessage).text = getString(R.string.location_not_set)
            coords = null
        }
    }
}