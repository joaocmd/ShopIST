package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

class PantryList(val title: String) {

    var uuid = UUID.randomUUID()
    var items: MutableList<Item> = mutableListOf()
    // TODO: Get a location
    var location: String = ""

    constructor(p: PantryListDto, products: Map<UUID, Product>) : this(p.title) {
        uuid = p.uuid
        items = p.items.map { i -> Item(i, products) }.toMutableList()
    }

    fun addItem(item: Item) {
        items.add(item)
    }

    fun hasProduct(product: Product): Boolean {
        val found = items.filter { item -> item.product == product }
        return found.isNotEmpty()
    }

    fun getItem(uuid: UUID) : Item {
        return items.first { i -> i.product.uuid == uuid }
    }
}

