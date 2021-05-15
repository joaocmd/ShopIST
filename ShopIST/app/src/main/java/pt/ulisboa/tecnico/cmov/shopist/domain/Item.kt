package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

class Item(
    var product: Product,
    var pantryList: PantryList,
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
        0
    )

    companion object {
        fun updateItem(i: Item?, itemDto: ItemDto, products: Map<UUID, Product>,
                       pantryList: PantryList): Item {
            if (i == null) {
                return Item(itemDto, products, pantryList)
            }
            i.product = products[itemDto.productUUID]!!
            i.pantryList = pantryList
            i.pantryQuantity = itemDto.pantryQuantity
            // i.cartQuantity = itemDto.cartQuantity // Not updated since cart quantities are local
            i.needingQuantity = itemDto.pantryQuantity
            return i
        }
    }
}