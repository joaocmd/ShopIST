package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

class ShoppingList(val title: String) {

    var uuid: UUID = UUID.randomUUID()
    var products: MutableSet<Product> = mutableSetOf()

    var items: Map<Product, Item> = mapOf()

    // TODO: Get a location
    var location = ""

    fun getItems(allPantries: Collection<PantryList>): Map<Product, List<Item>> {
        val products = setOf<String>("Escova de Dentes", "Croissant de Chocolate")
        val res = mutableMapOf<Product, MutableList<Item>>()
        for (pantry in allPantries) {
            for (item in pantry.items) {
                if (products.contains(item.product.name)) {
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