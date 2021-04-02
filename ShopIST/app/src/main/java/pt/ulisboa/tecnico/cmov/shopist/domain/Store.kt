package pt.ulisboa.tecnico.cmov.shopist.domain

import com.google.android.gms.maps.model.LatLng
import java.util.*

class Store(var title: String, var location: LatLng) {
    var uuid: UUID = UUID.randomUUID()
    // TODO: Items are calculated when the shopping list is opened
    var items: Map<Product, Item> = mapOf()

    companion object {
        fun createStore(s: StoreDto): Store {
            val store = Store(s.title, s.location)
            store.uuid = s.uuid
            return store
        }
    }

}