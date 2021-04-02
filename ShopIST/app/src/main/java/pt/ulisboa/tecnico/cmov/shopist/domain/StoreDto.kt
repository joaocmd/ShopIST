package pt.ulisboa.tecnico.cmov.shopist.domain

import com.google.android.gms.maps.model.LatLng
import java.util.*

class StoreDto(val title: String, val location: LatLng?, val uuid: UUID) {
    constructor(s: Store) : this(s.title, s.location, s.uuid) {
    }
}