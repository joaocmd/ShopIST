package pt.ulisboa.tecnico.cmov.shopist.domain

import com.google.android.gms.maps.model.LatLng
import java.util.*

data class PantryListDto(val uuid: UUID, val name: String, val items: MutableList<ItemDto>, val location: LatLng?, val isShared: Boolean) {

    constructor(p: PantryList) : this(
        p.uuid,
        p.name,
        p.items.map { i -> ItemDto(i) }.toMutableList(),
        p.location,
        p.isShared
    )
}