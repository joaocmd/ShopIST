package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

class Item(val product: Product,
           var pantryQuantity: Int,
           var needingQuantity: Int,
           var cartQuantity: Int) {

    constructor(i: ItemDto, products: Map<UUID, Product>): this(
        products[i.productUUID]!!,
        i.pantryQuantity,
        i.needingQuantity,
        i.cartQuantity
    )
}