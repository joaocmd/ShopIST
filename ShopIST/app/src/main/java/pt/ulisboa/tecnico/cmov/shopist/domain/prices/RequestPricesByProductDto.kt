package pt.ulisboa.tecnico.cmov.shopist.domain.prices

import com.google.android.gms.maps.model.LatLng

class RequestPricesByProductDto(var barcode: String, var locations: List<LatLng>)