package pt.ulisboa.tecnico.cmov.shopist.domain

import com.google.android.gms.maps.model.LatLng
import java.util.*

class Store(val title: String, val location: LatLng) {
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

    fun getItems(allPantries: Collection<PantryList>): Map<Product, List<Item>> {
        val res = mutableMapOf<Product, MutableList<Item>>()
        for (pantry in allPantries) {
            for (item in pantry.items) {
                // TODO: Check when product does not have store
                if (item.product.stores.contains(this) || item.product.stores.isEmpty()) {
                    if (res.containsKey(item.product)) {
                        res[item.product]!!.add(item)
                    } else {
                        res[item.product] = mutableListOf(item)
                    }
                }
            }
        }

        return res
    }
}