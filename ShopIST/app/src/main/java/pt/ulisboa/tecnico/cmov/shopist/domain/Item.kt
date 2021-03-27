package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

class Item(prod: Product, initialQuant: Int) {
    val product: Product = prod
    var pantryQuantity: Int = initialQuant

    companion object {
        fun createItem(i: ItemDto, products: Map<UUID, Product>): Item {
            return Item(products[i.productUUID]!!, i.pantryQuantity)
        }
    }
}