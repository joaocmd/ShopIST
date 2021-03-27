package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

class Item(val product: Product,
           var pantryQuantity: Int,
           var needingQuantity: Int,
           var cartQuantity: Int) {
    companion object {
        fun createItem(i: ItemDto, products: Map<UUID, Product>): Item {
            return Item(products[i.productUUID]!!,
                    i.pantryQuantity,
                    i.needingQuantity,
                    i.cartQuantity)
        }
    }
}