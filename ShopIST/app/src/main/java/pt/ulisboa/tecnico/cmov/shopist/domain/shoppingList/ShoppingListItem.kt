package pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList

import pt.ulisboa.tecnico.cmov.shopist.domain.Item
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.Product
import kotlin.math.max

class ShoppingListItem(val product: Product) {

   var items: MutableList<Item> = mutableListOf()
   val quantities: MutableMap<PantryList, Quantity> = mutableMapOf()
   val tempQuantities: MutableMap<PantryList, Quantity> = mutableMapOf()
   lateinit var shoppingList: ShoppingList

   constructor(product: Product, items: Collection<Item>, shoppingList: ShoppingList) : this(product) {
      items.forEach {
         this.items.add(it)
         this.quantities[it.pantryList] =
            Quantity(it.pantryQuantity, it.needingQuantity, it.cartQuantity)

         this.tempQuantities[it.pantryList] =
             Quantity(it.pantryQuantity, it.needingQuantity, it.cartQuantity)
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
      val quant = quantities[pantryList]
      if (quant!!.needing + quant.cart> tempQuantities[pantryList]!!.cart) {
         tempQuantities[pantryList]!!.cart = tempQuantities[pantryList]!!.cart + 1
      }
   }

   fun remove(pantryList: PantryList) {
      if (tempQuantities[pantryList]!!.cart > 0) {
         tempQuantities[pantryList]!!.cart = tempQuantities[pantryList]!!.cart - 1
      }
   }

   fun maximum(pantryList: PantryList) {
      val quant = quantities[pantryList]
      tempQuantities[pantryList]!!.cart = //tempQuantities[pantryList]!!.cart + 1
         quant!!.needing + quant.cart
   }

   fun minimum(pantryList: PantryList) {
      if (tempQuantities[pantryList]!!.cart > 0) {
         tempQuantities[pantryList]!!.cart = 0
      }
   }

   fun reset() {
      items.forEach {
         tempQuantities[it.pantryList]!!.needing = it.needingQuantity
         tempQuantities[it.pantryList]!!.cart = it.cartQuantity
      }
   }

   fun save() {
       items.forEach {
          val cartQuantity = tempQuantities[it.pantryList]!!.cart
          val cartVariation = cartQuantity - it.cartQuantity
          val needingQuantity = max(it.needingQuantity - cartVariation, 0)

          it.needingQuantity = needingQuantity
          it.cartQuantity = cartQuantity

          quantities[it.pantryList]!!.needing = needingQuantity
          quantities[it.pantryList]!!.cart = cartQuantity

          tempQuantities[it.pantryList]!!.needing = needingQuantity
          tempQuantities[it.pantryList]!!.cart = cartQuantity
       }
   }

   fun storeToPantry() {
      items.forEach {
         val cartQuantity = quantities[it.pantryList]!!.cart
         it.pantryQuantity += cartQuantity
         it.needingQuantity = max(it.needingQuantity - cartQuantity, 0)
         it.cartQuantity = 0
      }
   }
}
