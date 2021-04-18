package pt.ulisboa.tecnico.cmov.shopist.domain.prices

import com.google.android.gms.maps.model.LatLng

class AddPriceDto(var barcode: String, var location: LatLng, var price: Number) {
}