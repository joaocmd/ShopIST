package pt.ulisboa.tecnico.cmov.shopist.domain.prices

import com.google.android.gms.maps.model.LatLng

class RequestPricesByLocationDto(var barcodes: List<String>, var location: LatLng)