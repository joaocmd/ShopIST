package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

class PantryList(title: String) {
    companion object {
        fun createPantry(p: PantryDto, products: Map<UUID, Product>): PantryList {
            val pantry = PantryList(p.title)
            pantry.items = p.items.map { i -> Item.createItem(i, products) }.toMutableList()
            return pantry
        }
    }

    val title = title.capitalize()
    // TODO: Get a location
    var location: String = ""
    // FIXME:
    var items: MutableList<Item> = mutableListOf()

    fun addItem(item: Item) {
        items.add(item)
    }

    fun hasProduct(product: Product): Item? {
        val found = items.filter { item -> item.product == product }
        return if (found.isNotEmpty()) found[0] else null
    }
}

