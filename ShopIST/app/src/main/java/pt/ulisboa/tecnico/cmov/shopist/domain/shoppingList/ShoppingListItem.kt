package pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList

import pt.ulisboa.tecnico.cmov.shopist.domain.Item
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.Product
import kotlin.math.max

class ShoppingListItem(val product: Product) {

   var items: MutableList<Item> = mutableListOf()
   val quantities: MutableMap<PantryList, Quantity> = mutableMapOf()
   lateinit var shoppingList: ShoppingList

   constructor(product: Product, items: Collection<Item>, shoppingList: ShoppingList) : this(product) {
      items.forEach {
         this.items.add(it)
         this.quantities[it.pantryList] = Quantity(it.pantryQuantity, it.needingQuantity, it.cartQuantity)
      }
      this.items = items.sortedBy { it.pantryList.name }.toMutableList()
      this.shoppingList = shoppingList
   }

   fun getAllQuantities(): Quantity {
      val quantity = Quantity(0, 0, 0)
      quantities.values.forEach {
          quantity.add(it)
      }
      return quantity
   }

   fun add(pantryList: PantryList) {
      quantities[pantryList]!!.cart = quantities[pantryList]!!.cart + 1
   }

   fun remove(pantryList: PantryList) {
      if (quantities[pantryList]!!.cart > 0) {
         quantities[pantryList]!!.cart = quantities[pantryList]!!.cart - 1
      }
   }

   fun save() {
       items.forEach {
          val cartQuantity = quantities[it.pantryList]!!.cart
          it.pantryQuantity += cartQuantity
          it.needingQuantity = max(it.needingQuantity - cartQuantity, 0)
          it.cartQuantity = 0
       }
   }
}
