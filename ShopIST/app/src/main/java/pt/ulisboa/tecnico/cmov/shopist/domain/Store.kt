package pt.ulisboa.tecnico.cmov.shopist.domain

import com.google.android.gms.maps.model.LatLng
import java.util.*

class Store(var name: String) : Locatable {
    var uuid: UUID = UUID.randomUUID()
    var items: Map<Product, Item> = mapOf()

    override var location: LatLng? = null
    override var drivingTime: Long? = null

    constructor(title: String, location: LatLng): this(title) {
        this.location = location
    }

    companion object {
        fun createStore(s: StoreDto): Store {
            val store = Store(s.name)
            store.location = s.location
            store.uuid = s.uuid
            return store
        }
    }

}