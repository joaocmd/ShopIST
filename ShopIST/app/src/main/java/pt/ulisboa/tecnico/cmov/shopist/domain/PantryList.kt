package pt.ulisboa.tecnico.cmov.shopist.domain

import com.google.android.gms.maps.model.LatLng
import java.util.*

class PantryList(var title: String) : Distanceable {

    var uuid: UUID = UUID.randomUUID()
    private var _items: MutableList<Item> = mutableListOf()

    val items: MutableList<Item>
        get() = _items.sortedBy { it.product.name }.toMutableList()

    override var location: LatLng? = null

    constructor(title: String, location: LatLng?): this(title) {
        this.location = location
    }

    constructor(p: PantryListDto, products: Map<UUID, Product>) : this(p.title, p.location) {
        uuid = p.uuid
        _items = p.items.map { i -> Item(i, products, this) }.toMutableList()
    }

    fun addItem(item: Item) {
        _items.add(item)
    }

    fun hasProduct(product: Product): Boolean {
        val found = _items.filter { item -> item.product == product }
        return found.isNotEmpty()
    }

    fun getItem(uuid: UUID) : Item {
        return _items.first { i -> i.product.uuid == uuid }
    }
}

