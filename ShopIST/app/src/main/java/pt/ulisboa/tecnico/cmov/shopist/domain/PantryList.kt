package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

class PantryList(val title: String) {
    companion object {
        fun createPantry(p: PantryListDto, products: Map<UUID, Product>): PantryList {
            val pantry = PantryList(p.title)
            pantry.items = p.items.map { i -> Item.createItem(i, products) }.toMutableList()
            return pantry
        }
    }

    // TODO: Get a location
    val uuid = UUID.randomUUID()

    var location: String = ""
    // FIXME:
    var items: MutableList<Item> = mutableListOf()

    fun addItem(item: Item) {
        items.add(item)
    }

    fun hasProduct(product: Product): Boolean {
        val found = items.filter { item -> item.product == product }
        return found.isNotEmpty()
    }
}

