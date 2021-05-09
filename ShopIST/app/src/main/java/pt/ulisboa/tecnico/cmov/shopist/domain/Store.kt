package pt.ulisboa.tecnico.cmov.shopist.domain

import com.google.android.gms.maps.model.LatLng
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
        var max = 0f
        var counter = 0f
        for (pantry in allPantries) {
            for (item in pantry.items) {
                if(item.needingQuantity > 0) {
                    max++
                    if (item.product.hasStore(this.uuid)) {
                        counter++
                    }
                }
            }
        }

        return counter / max
    }

    fun itemQuantityTotal(allPantries: Collection<PantryList>): Number {
        var counter = 0
        for (pantry in allPantries) {
            for (item in pantry.items) {
                if(item.needingQuantity > 0) {
                    if (item.product.hasStore(this.uuid)) {
                        counter += item.needingQuantity
                    }
                }
            }
        }

        return counter
    }

    fun itemCheckoutTotal(allPantries: Collection<PantryList>): Number {
        var counter = 0
        for (pantry in allPantries) {
            for (item in pantry.items) {
                if(item.cartQuantity > 0) {
                    if (item.product.hasStore(this.uuid)) {
                        counter += item.cartQuantity
                    }
                }
            }
        }

        return counter
    }

    fun itemPriceTotal(allPantries: Collection<PantryList>): Number {
        var totalPrice : Double = 0.0
        for (pantry in allPantries) {
            for (item in pantry.items) {
                val price = item.product.prices[this]
                if(item.cartQuantity > 0 && price != null) {
                    totalPrice += item.cartQuantity.toDouble() * price.toDouble()
                }
            }
        }

        return totalPrice
    }
}