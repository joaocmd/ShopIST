package pt.ulisboa.tecnico.cmov.shopist

import android.app.ActionBar
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.android.volley.TimeoutError
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import pt.ulisboa.tecnico.cmov.shopist.domain.Language
import pt.ulisboa.tecnico.cmov.shopist.domain.Languages
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.ui.pantries.PantryUI
import pt.ulisboa.tecnico.cmov.shopist.utils.API
import pt.ulisboa.tecnico.cmov.shopist.utils.LocaleHelper
import pt.ulisboa.tecnico.cmov.shopist.utils.LocationUtils
import pt.ulisboa.tecnico.cmov.shopist.utils.toLatLng
import java.util.*

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var locationUtils: LocationUtils
    private lateinit var progressCircle: ProgressBar

    private var hasPantryToOpen = false
    private var hasProductToOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        setContentView(R.layout.activity_splash_screen)

        progressCircle = findViewById(R.id.progressBar)
        progressCircle.visibility = View.VISIBLE

        receivedUriIntent()

        API.getInstance(applicationContext).ping()

        val globalData = applicationContext as ShopIST
        if (globalData.pantries.isEmpty()) {
            globalData.startUp()
        }

        // Get local cache files to the cache
        globalData.getAllProducts().forEach {
            it.images.forEach { id ->
                globalData.imageCache.startupImage(UUID.fromString(id))
            }
        }

        globalData.pantries.forEach {
            if (it.isShared) {
                // Check for updates
                API.getInstance(applicationContext).getPantry(it.uuid, { result ->
                    globalData.populateFromServer(result)
                    globalData.callbackDataSetChanged?.invoke()
                }, {
                })
            }
        }

        LocaleHelper.setLocale(baseContext)
        // Set current language
        val currentLang = globalData.getLang()

        // Get translations at start
        globalData.getAllProducts().forEach { p ->
            p.getText(currentLang, applicationContext) {
                p.translatedText = it
                p.hasTranslated = true
            }
        }
    }

    override fun onResume() {
        super.onResume()

        locationUtils = LocationUtils(this)

        if (locationUtils.hasPermissions()) {
            getDeviceLocation()
        } else {
            locationUtils.requestPermissions()
            dismiss()
        }
    }

    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            locationUtils.getNewLocation { location ->
                (applicationContext as ShopIST).currentLocation = location!!.toLatLng()
                if (!hasPantryToOpen && !hasProductToOpen) dismiss()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message!!)
        }
    }

    private fun receivedUriIntent(): Boolean {
        if (intent?.action == Intent.ACTION_VIEW) {
            try {
                val shopIst = (applicationContext as ShopIST)

                val urlArgs = intent.data.toString().split("/")
                if (urlArgs.size != 5) {
                    return false
                }

                when (urlArgs[3]) {
                    "product" -> {
                        val uuid = UUID.fromString(urlArgs[4])
                        hasProductToOpen = true
                        shopIst.loadProduct(uuid, {
                            shopIst.productToOpen = shopIst.getProduct(it)
                            dismiss()
                        }, {
                            Toast.makeText(applicationContext,
                                getString(R.string.cannot_get_product),
                                Toast.LENGTH_SHORT
                            ).show()
                            dismiss()
                        })
                        return true
                    }
                    "pantry" -> {
                        val uuid = UUID.fromString(urlArgs[4])
                        hasPantryToOpen = true

                        shopIst.loadPantryList(uuid, {
                            shopIst.pantryToOpen = shopIst.getPantryList(it)
                            dismiss()
                        }, {
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.cannot_get_pantry),
                                Toast.LENGTH_SHORT
                            ).show()
                            dismiss()
                        })

                        return true
                    }
                }
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

    private fun dismiss() {
        startActivity(Intent(this, SideMenuNavigation::class.java))
        finish()
    }
}