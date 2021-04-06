package pt.ulisboa.tecnico.cmov.shopist.domain

import com.google.android.gms.maps.model.LatLng
import java.util.*

class StoreDto(val name: String, val location: LatLng?, val uuid: UUID) {
    constructor(s: Store) : this(s.name, s.location, s.uuid) {
    }
}