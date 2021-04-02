package pt.ulisboa.tecnico.cmov.shopist.domain

import com.google.android.gms.maps.model.LatLng
import java.util.*

data class PantryListDto(val uuid: UUID, val title: String, val items: MutableList<ItemDto>, val location: LatLng?) {

    constructor(p: PantryList) : this(
        p.uuid,
        p.title,
        p.items.map { i -> ItemDto(i) }.toMutableList(),
        p.location
    )
}