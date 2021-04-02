package pt.ulisboa.tecnico.cmov.shopist.domain

import com.google.android.gms.maps.model.LatLng
import java.util.*

class Store(var title: String) {
    var uuid: UUID = UUID.randomUUID()
    var items: Map<Product, Item> = mapOf()
    var location: LatLng? = null

    constructor(title: String, location: LatLng): this(title) {
        this.location = location
    }

    companion object {
        fun createStore(s: StoreDto): Store {
            val store = Store(s.title)
            store.location = s.location
            store.uuid = s.uuid
            return store
        }
    }

}