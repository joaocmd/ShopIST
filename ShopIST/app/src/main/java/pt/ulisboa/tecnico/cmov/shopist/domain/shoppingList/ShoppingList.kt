package pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList

import pt.ulisboa.tecnico.cmov.shopist.domain.Item
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.Product
import pt.ulisboa.tecnico.cmov.shopist.domain.Store

class ShoppingList() {

    lateinit var items: List<ShoppingListItem>

    constructor(store: Store, allPantries: Collection<PantryList>) : this() {
        val tempItems: MutableMap<Product, MutableList<Item>> = mutableMapOf()
        for (pantry in allPantries) {
            for (item in pantry.items) {
                // TODO: Check when product does not have any store
                if (item.product.hasStore(store.uuid) && item.needingQuantity > 0) {
                    if (tempItems.containsKey(item.product)) {
                        tempItems[item.product]!!.add(item)
                    } else {
                        tempItems[item.product] = mutableListOf(item)
                    }
                }
            }
        }

        val items: MutableMap<Product, ShoppingListItem> = mutableMapOf()
        for (item in tempItems) {
            items[item.key] = ShoppingListItem(item.key, item.value)
        }
        this.items = items.values.sortedBy { it.product.name }
    }

    fun saveChanges() {
        items.forEach { it.save() }
    }

    fun getPantries(): List<PantryList> {
        return items.map { it.items.map { i -> i.pantryList } }.flatten().toSet().toList()
    }
}