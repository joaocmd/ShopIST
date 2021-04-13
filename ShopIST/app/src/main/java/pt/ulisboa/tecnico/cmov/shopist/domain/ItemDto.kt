package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

data class ItemDto(val productUUID: UUID,
                   val pantryQuantity: Int,
                   val needingQuantity: Int,
                   val cartQuantity: Int) {

    // 1 -> Add, -1 -> Remove
    var opType: Int = 1

    constructor(item: Item, opType: Int): this(
        item.product.uuid,
        item.pantryQuantity,
        item.needingQuantity,
        item.cartQuantity,
    ) {
        this.opType = opType
    }
}