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

    companion object {
        fun updateItem(i: Item?, itemDto: ItemDto, products: Map<UUID, Product>,
                       pantryList: PantryList): Item {
            if (i == null) {
                return Item(itemDto, products, pantryList)
            }
            i.pantryQuantity = itemDto.pantryQuantity
            i.cartQuantity = itemDto.cartQuantity
            i.needingQuantity = itemDto.pantryQuantity
            return i
        }
    }
}