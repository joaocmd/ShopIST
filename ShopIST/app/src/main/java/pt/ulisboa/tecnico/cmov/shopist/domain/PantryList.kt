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

            val previousItems = p1._items.toList()
            p1._items.clear()
            update.items.forEach {
                val existingItem = p1.itemsHasProduct(previousItems, it.productUUID)
                if (existingItem != null) {
                    Item.updateItem(existingItem, it, products, p1)
                } else {
                    p1.addItem(Item(it, products, p1))
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

    private fun itemsHasProduct(items: List<Item>, uuid: UUID): Item? {
        val found = items.filter { item -> item.product.uuid == uuid }
        return found.firstOrNull()
    }

    fun hasProduct(product: Product): Boolean {
        val found = _items.filter { item -> item.product == product }
        return found.isNotEmpty()
    }

    fun hasProduct(uuid: UUID): Boolean {
        return _items.find { i -> i.product.uuid == uuid } != null
    }

    fun getProducts(): List<Product> {
        return _items.map { it.product }
    }

    fun share() {
        this.isShared = true
        _items.forEach {
            it.product.isShared = true
            it.product.stores.forEach { s ->
                s.isShared = true
            }
        }
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

