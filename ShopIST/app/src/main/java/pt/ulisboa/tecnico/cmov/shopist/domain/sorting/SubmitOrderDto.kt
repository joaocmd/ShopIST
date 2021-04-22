package pt.ulisboa.tecnico.cmov.shopist.domain.sorting

import com.google.android.gms.maps.model.LatLng

class SubmitOrderDto(val location: LatLng, val order: List<String>) {
}