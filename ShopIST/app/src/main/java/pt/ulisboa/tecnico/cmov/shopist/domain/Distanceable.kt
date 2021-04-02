package pt.ulisboa.tecnico.cmov.shopist.domain

import android.location.Location
import android.location.LocationManager
import com.google.android.gms.maps.model.LatLng

interface Distanceable {
    val location: LatLng?

    fun getDistance(initialLoc: LatLng): Float {
        val loc1 = Location(LocationManager.GPS_PROVIDER)
        val loc2 = Location(LocationManager.GPS_PROVIDER)

        loc1.latitude = initialLoc.latitude
        loc1.longitude = initialLoc.longitude

        if (this.location != null) {
            loc2.latitude = this.location!!.latitude
            loc2.longitude = this.location!!.longitude
            return loc1.distanceTo(loc2)
        }
        return 0F
    }
}