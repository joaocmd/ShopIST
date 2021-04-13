package pt.ulisboa.tecnico.cmov.shopist.domain

import com.google.android.gms.maps.model.LatLng
import java.util.*

class PantryList(var name: String) : Locatable {

    var uuid: UUID = UUID.randomUUID()
    private var _items: MutableList<Item> = mutableListOf()

    val items: MutableList<Item>
        get() = _items.sortedBy { it.product.name }.toMutableList()

    override var location: LatLng? = null
    override var drivingTime: Long? = null

    var isShared: Boolean = false

    constructor(title: String, location: LatLng?): this(title) {
        this.location = location
    }

    constructor(p: PantryListDto, products: Map<UUID, Product>) : this(p.name, p.location) {
        uuid = p.uuid
        _items = p.items.map { i -> Item(i, products, this) }.toMutableList()
        isShared = p.isShared
    }

    companion object {
        fun updatePantry(p1: PantryList?, update: PantryListDto, products: Map<UUID, Product>): PantryList {
            if (p1 === null) {
                return PantryList(update, products)
            }
            p1.name = update.name
            update.items.forEach {
                when (it.opType) {
                    // Add operation
                    (1) -> {
                        if (p1.hasProduct(it.productUUID)) {
                            val item = p1.getItem(it.productUUID)
                            Item.updateItem(item, it, products, p1)
                        } else {
                            p1.addItem(Item(it, products, p1))
                        }
                    }
                    // Remove operation
                    (-1) -> {
                        p1.removeItem(it.productUUID)
                    }
                }
            }
            p1.isShared = update.isShared
            p1.location = update.location
            return p1
        }
    }

    fun addItem(item: Item) {
        _items.add(item)
    }

    fun hasProduct(product: Product): Boolean {
        val found = _items.filter { item -> item.product == product }
        return found.isNotEmpty()
    }

    fun hasProduct(uuid: UUID): Boolean {
        return _items.find { i -> i.product.uuid == uuid } != null
    }

    fun getItem(uuid: UUID) : Item {
        return _items.first { i -> i.product.uuid == uuid }
    }

    fun removeItem(uuid: UUID) {
        this._items.removeIf {
            it.product.uuid == uuid
        }
    }
}

