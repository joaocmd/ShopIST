package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

data class ItemDto(val productUUID: UUID, val pantryQuantity: Int) {
    constructor(item: Item): this(item.product.uuid, item.pantryQuantity)
}