package pt.ulisboa.tecnico.cmov.shopist

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST


class LocationPickerActivity : AppCompatActivity(),
    OnMapReadyCallback,
    OnMyLocationButtonClickListener {

    companion object {
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        private const val TAG = "shopist.MapsActivity"
        const val LATITUDE = "$TAG.LAT"
        const val LONGITUDE = "$TAG.LON"
    }

    private lateinit var map: GoogleMap
    private var locationPermissionGranted = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // TODO: Try to make the default location somewhere according to the country?
    private val defaultLocation = LatLng(38.73795412879115, -9.137816238437372)
    private var lastKnownLocation: Location? = null
    private var selectedMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Add a marker in IST and move the camera
        // map.addMarker(MarkerOptions().position(mDefaultLocation).title("Marker in IST"))
        // map.moveCamera(CameraUpdateFactory.newLatLng(mDefaultLocation))

        // Enable the zoom controls for the map
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true

        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener {
            getDeviceLocation()
        }
        map.setOnMapClickListener {
            if (selectedMarker === null) {
                selectedMarker = map.addMarker(
                    MarkerOptions().position(it).title("Chosen location")
                )
                selectedMarker!!.isDraggable = true
            } else {
                selectedMarker!!.position = it
            }
        }

        // map.moveCamera(CameraUpdateFactory.newLatLng(mDefaultLocation))

        // Prompt the user for permission.
        getLocationPermission()
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        locationPermissionGranted = false
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            getDeviceLocation()
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                }
            }
        }
    }

    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        // Got last known location. In some rare situations this can be null.
                        if (location !== null) {
                            lastKnownLocation = location
                            val lat = location.latitude
                            val lon = location.longitude
                            Log.d(ShopIST.TAG, "Lat: $lat, Lon: $lon")

                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(lat, lon), DEFAULT_ZOOM.toFloat()
                                )
                            )
                        } else {
                            Log.d(ShopIST.TAG, "Null location")
                        }
                    }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message!!)
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        getDeviceLocation()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    fun onSaveLocationButton(view: View) {
        if (selectedMarker !== null) {
            val lat = selectedMarker!!.position.latitude
            val lon = selectedMarker!!.position.longitude
            Log.d(ShopIST.TAG, "Selected location - Lat: $lat, Lon: $lon")

            // Return this to activity who started this one
            setResult(
                RESULT_OK,
                Intent().putExtra(LATITUDE, lat).putExtra(LONGITUDE, lon)
            )
            finish()
        } else {
            Toast.makeText(applicationContext, "First select a location.", Toast.LENGTH_SHORT).show()
        }
    }

    fun onCancelButton(view: View) {
        setResult(
            RESULT_CANCELED
        )
        finish()
    }
}