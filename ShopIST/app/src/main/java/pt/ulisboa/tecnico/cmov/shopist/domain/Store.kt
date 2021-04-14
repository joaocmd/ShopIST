package pt.ulisboa.tecnico.cmov.shopist.domain

import com.google.android.gms.maps.model.LatLng
import pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList.ShoppingListItem
import java.util.*

class Store(var name: String) : Locatable {
    var uuid: UUID = UUID.randomUUID()
    var items: Map<Product, Item> = mapOf()

    override var location: LatLng? = null
    override var drivingTime: Long? = null
    var queueTime: Long? = null
    var isShared = false

    constructor(title: String, location: LatLng): this(title) {
        this.location = location
    }

    companion object {
        fun createStore(s: StoreDto): Store {
            val store = Store(s.name)
            store.location = s.location
            store.uuid = s.uuid
            store.isShared = s.isShared
            return store
        }

        fun updateStore(s1: Store?, update: StoreDto): Store {
            if (s1 == null) {
                return createStore(update)
            }
            s1.name = update.name
            s1.location = update.location
            s1.isShared = update.isShared
            return s1
        }
    }

    fun itemPercentage(allPantries: Collection<PantryList>): Float {
        val tempItems: MutableMap<Product, MutableList<Item>> = mutableMapOf()
        var max = 0f
        var counter = 0f
        for (pantry in allPantries) {
            for (item in pantry.items) {
                // TODO: Check when product does not have any store
                if(item.needingQuantity > 0) {
                    max++
                    if (item.product.stores.contains(this)) {
                        counter++
                    }
                }
            }
        }

        return counter / max
    }
}