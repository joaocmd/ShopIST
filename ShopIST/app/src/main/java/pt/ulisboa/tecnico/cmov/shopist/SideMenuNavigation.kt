package pt.ulisboa.tecnico.cmov.shopist

import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Messenger
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList
import pt.inesc.termite.wifidirect.SimWifiP2pManager
import pt.inesc.termite.wifidirect.service.SimWifiP2pService
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.domain.Store
import pt.ulisboa.tecnico.cmov.shopist.ui.pantries.PantryUI
import pt.ulisboa.tecnico.cmov.shopist.ui.shoppings.ShoppingListUI
import pt.ulisboa.tecnico.cmov.shopist.utils.*
import java.util.*


class SideMenuNavigation : AppCompatActivity(), SimWifiP2pManager.PeerListListener {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var locationPermissionGranted: Boolean = false
    private lateinit var locationUtils: LocationUtils

    // WiFi Direct variables
    private var mManager: SimWifiP2pManager? = null
    private var mChannel: SimWifiP2pManager.Channel? = null
    private var mBound = false
    private var mReceiver: QueueBroadcastReceiver? = null

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

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_pantries_list,
                R.id.nav_stores_list
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Set broadcast receiver for WifiDirect
        val filter = IntentFilter()
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION)
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION)
        mReceiver = QueueBroadcastReceiver(this)
        registerReceiver(mReceiver, filter)

        locationUtils = LocationUtils(this)

        if (locationUtils.hasPermissions()) {
            getDeviceLocation()
        } else {
            locationUtils.requestPermissions()
        }
    }

    override fun onResume() {
        super.onResume()
        // Sync calling
        val syncIntent = Intent(applicationContext, SyncService::class.java)
        startService(syncIntent)

        // Start WiFi Direct
        val intent = Intent(applicationContext, SimWifiP2pService::class.java)
        bindService(intent, mConnection, BIND_AUTO_CREATE)
        mBound = true
    }

    override fun onPause() {
        super.onPause()
        val syncIntent = Intent(applicationContext, SyncService::class.java)
        stopService(syncIntent)

        if (mBound) {
            unbindService(mConnection)
            mBound = false
        }
    }

    private val mConnection: ServiceConnection = object : ServiceConnection {
        // callbacks for service binding, passed to bindService()
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mManager = SimWifiP2pManager(Messenger(service))
            mChannel = mManager!!.initialize(application, mainLooper, null)
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mManager = null
            mChannel = null
            mBound = false
        }
    }

    private fun receivedUriIntent(): Boolean {
        if (intent?.action == Intent.ACTION_VIEW) {
            try {
                val shopIst = (applicationContext as ShopIST)
                val uuid = UUID.fromString(intent.data.toString().split("/").last())

                // TODO: Set a load activity to do this
                shopIst.loadPantryList(uuid, {
                    Log.d(ShopIST.TAG, uuid.toString())
                    findNavController(R.id.nav_host_fragment).navigate(
                        R.id.nav_pantry,
                        bundleOf(
                            PantryUI.ARG_PANTRY_ID to uuid.toString()
                        )
                    )
                }, {
                    // TODO: Resource this string
                    Toast.makeText(
                        applicationContext,
                        "Cannot get pantry list.",
                        Toast.LENGTH_SHORT
                    ).show()
                })

                return true
            } catch (e: NoSuchElementException) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.unable_open_pantry),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: IllegalArgumentException) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.unable_open_pantry),
                    Toast.LENGTH_SHORT
                ).show()
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
            locationUtils.getNewLocation { location ->
                (applicationContext as ShopIST).currentLocation = location!!.toLatLng()
                openCorrespondingList(location.toLatLng())
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

    private var beaconToken: UUID? = null
    private var currentBeacon: String? = null

    fun getPeers() {
        if (mBound) {
            mManager!!.requestPeers(mChannel, this)
        }
    }

    /*
     * Termite listeners
     */
    override fun onPeersAvailable(peers: SimWifiP2pDeviceList) {
        // TODO: Ask teacher if there is a possibility of moving from a beacon to another, like ShopIST-001 and then ShopIST-002, without going without any beacon
        peers.deviceList.find { it.deviceName.startsWith("ShopIST-") }.let {
            if (it != null) {
                // enter beacon range
                currentBeacon = it.deviceName
                beaconToken = UUID.randomUUID()
                val nrItems = (applicationContext as ShopIST).pantries.sumBy {
                    pantry -> pantry.items.sumBy { i -> i.cartQuantity }
                }
                if (nrItems == 0) {
                    // Don't do anything if there are not items in cart
                    return
                }
                API.getInstance(applicationContext).beaconEnter(it.deviceName, nrItems, beaconToken!!, {
                    Log.d(ShopIST.TAG, "Beacon entered successfully!")
				}, {
                    Log.d(ShopIST.TAG, "Beacon not entered!")
				})
            } else if (currentBeacon != null) {
                // leave beacon range (had previously assigned beacon)
                API.getInstance(applicationContext).beaconLeave(currentBeacon!!, beaconToken!!, {
                    Log.d(ShopIST.TAG, "Beacon entered successfully!")
				}, {
                    Log.d(ShopIST.TAG, "Beacon not entered!")
				})
                currentBeacon = null
                beaconToken = null
            }
        }
    }
}