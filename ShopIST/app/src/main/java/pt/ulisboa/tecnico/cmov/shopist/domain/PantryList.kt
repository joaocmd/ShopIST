package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

class PantryList(val title: String) {

    var uuid: UUID = UUID.randomUUID()
    private var _items: MutableList<Item> = mutableListOf()

    val items: MutableList<Item>
        get() = _items.sortedBy { it.product.name }.toMutableList()
    // TODO: Get a location
    var location: String = ""

    constructor(p: PantryListDto, products: Map<UUID, Product>) : this(p.title) {
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

