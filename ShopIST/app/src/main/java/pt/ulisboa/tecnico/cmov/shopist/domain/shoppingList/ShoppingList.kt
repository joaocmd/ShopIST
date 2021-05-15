package pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList

import pt.ulisboa.tecnico.cmov.shopist.domain.Item
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.Product
import pt.ulisboa.tecnico.cmov.shopist.domain.Store
import java.util.*

class ShoppingList() {

    lateinit var items: MutableList<ShoppingListItem>
    var store: Store? = null

    constructor(store: Store, allPantries: Collection<PantryList>) : this() {
        this.store = store
        val tempItems: MutableMap<Product, MutableList<Item>> = mutableMapOf()
        for (pantry in allPantries) {
            for (item in pantry.items) {
                if (item.product.hasStore(store.uuid) &&
                    (item.needingQuantity > 0 || item.cartQuantity > 0)) {
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
            items[item.key] = ShoppingListItem(item.key, item.value, this)
        }
        this.items = items.values.sortedBy { it.product.name }.toMutableList()
    }

    fun removeItem(uuid: UUID) {
        val item = items.find {
            it.product.uuid == uuid
        }

        item?.let {
            items.remove(it)

            store?.let { store ->
                it.items.forEach { pantryItem ->
                    pantryItem.product.removeStore(store.uuid)
                }
            }
        }
    }

    fun saveToPantries() {
        items.forEach { it.storeToPantry() }
    }

    fun getPantries(): List<PantryList> {
        return items.map { it.items.map { i -> i.pantryList } }.flatten().toSet().toList()
    }

    fun getTotalCartQuantity(): Int {
        return items.sumBy { item ->
            item.quantities.values.sumBy { q ->
                q.cart
            }
        }
    }
}