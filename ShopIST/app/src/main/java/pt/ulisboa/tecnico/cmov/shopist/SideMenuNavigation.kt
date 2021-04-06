package pt.ulisboa.tecnico.cmov.shopist

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.domain.Store
import pt.ulisboa.tecnico.cmov.shopist.ui.pantries.PantryUI
import pt.ulisboa.tecnico.cmov.shopist.ui.shoppings.ShoppingListUI
import pt.ulisboa.tecnico.cmov.shopist.utils.API
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.NoSuchElementException


class SideMenuNavigation : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var locationPermissionGranted: Boolean = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val globalData = applicationContext as ShopIST
        if (globalData.pantries.isEmpty()) {
            globalData.startUp()
        }

        setContentView(R.layout.activity_side_menu_navigation)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_pantries_list,
                R.id.nav_stores_list
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        if (!receivedUriIntent() && ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            getDeviceLocation()
        }

        // FIXME: Remove this shit
        // API.getInstance(applicationContext).postNewPantry(globalData.pantries[0])
        API.getInstance(applicationContext).getPantry(globalData.pantries[0].uuid)
    }

    private fun receivedUriIntent(): Boolean {
        if (intent?.action == Intent.ACTION_VIEW) {
            try {
                val shopIst = (applicationContext as ShopIST)
                val uuid = UUID.fromString(intent.data.toString().split("/").last())
                shopIst.loadPantryList(uuid)
                navController.navigate(
                    R.id.nav_pantry,
                    bundleOf(
                        PantryUI.ARG_PANTRY_ID to uuid.toString()
                    )
                )
                return true
            } catch (e: NoSuchElementException) {
                Toast.makeText(applicationContext, getString(R.string.unable_open_pantry), Toast.LENGTH_SHORT).show()
            } catch (e: IllegalArgumentException) {
                Toast.makeText(applicationContext, getString(R.string.unable_open_pantry), Toast.LENGTH_SHORT).show()
            }
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.edit_item_menu, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationRequest = LocationRequest.create().apply {
                    interval = 250
                    fastestInterval = 0
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                // TODO: Set a load activity to do this
                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        locationResult ?: return
                        for (location in locationResult.locations){
                            // Got last known location. In some rare situations this can be null.
                            if (location !== null) {
                                val lat = location.latitude
                                val lon = location.longitude
                                Log.d(ShopIST.TAG, "Selected location - Lat: $lat, Lon: $lon")
                                openCorrespondingList(LatLng(lat, lon))
                                fusedLocationClient.removeLocationUpdates(this)
                                return
                            } else {
                                Log.d(ShopIST.TAG, "Null location")
                            }
                        }
                    }
                }
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message!!)
        }
    }

    private fun openCorrespondingList(currLocation: LatLng) {
        val globalData = applicationContext as ShopIST

        // Get all lists
        val closestList = globalData.getAllLists()
            .filter { it.location != null }
            .minByOrNull { it.getDistance(currLocation) }

        if (closestList != null && closestList.getDistance(currLocation) <= ShopIST.OPEN_AUTO_MAX_DISTANCE) {
            when (closestList) {
                is Store -> {
                    // FIXME: Try to add to backstack instead of navigating
                    navController.navigate(
                        R.id.nav_stores_list
                    )
                    navController.navigate(
                        R.id.nav_store_shopping_list,
                        bundleOf(
                            ShoppingListUI.ARG_STORE_ID to closestList.uuid.toString()
                        )
                    )
                }
                is PantryList -> {
                    navController.navigate(
                        R.id.nav_pantry,
                        bundleOf(
                            PantryUI.ARG_PANTRY_ID to closestList.uuid.toString()
                        )
                    )
                }
            }
        }
    }
}