package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

class Item(val product: Product,
           val pantryList: PantryList,
           var pantryQuantity: Int,
           var needingQuantity: Int,
           var cartQuantity: Int) {

    constructor(
        i: ItemDto,
        products: Map<UUID, Product>,
        pantryList: PantryList
    ): this(
        products[i.productUUID]!!,
        pantryList,
        i.pantryQuantity,
        i.needingQuantity,
        i.cartQuantity
    )
}