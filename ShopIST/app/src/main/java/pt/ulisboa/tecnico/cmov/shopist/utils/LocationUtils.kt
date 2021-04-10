package pt.ulisboa.tecnico.cmov.shopist.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng

fun Location.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}

class LocationUtils(val activity: Activity) {

    companion object {
        const val REQUEST_CODE = 312
        private var locationPermissionGranted = false
    }

    val fusedLocationClient: FusedLocationProviderClient = getFusedLocationProviderClient(activity)

    @SuppressLint("MissingPermission")
    fun getLastLocation(successListener: (Location?) -> Unit) {
        if (hasPermissions()) {
            fusedLocationClient.lastLocation.addOnSuccessListener(successListener)
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocationPolling(interval: Long, fastestInterval: Long, priority: Int, locationCallback: LocationCallback) {
        if (!hasPermissions()) {
            return
        }

        val locationRequest = LocationRequest.create().apply {
            this.interval = interval
            this.fastestInterval = fastestInterval
            this.priority = priority
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun requestPermissions() {
        if (hasPermissions()) {
            return
        }

        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CODE
        )
    }

    fun hasPermissions(): Boolean {
        if (locationPermissionGranted) {
            return true
        } else if (ContextCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            return true
        }
        return false
    }
}