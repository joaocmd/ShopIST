package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

data class ItemDto(val productUUID: UUID,
                   val pantryQuantity: Int,
                   val needingQuantity: Int,
                   val cartQuantity: Int) {
    constructor(item: Item): this(item.product.uuid,item.pantryQuantity, item.needingQuantity, item.cartQuantity)
}