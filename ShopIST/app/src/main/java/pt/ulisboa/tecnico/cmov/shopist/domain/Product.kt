package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

class Product(name: String) {
    // TODO: Save image
    var uuid: UUID = UUID.randomUUID()
    var name: String = name
    var barcode: String? = null
    var image: String? = null
    var stores: MutableSet<Store> = mutableSetOf()
    var isShared = false

    companion object {
        fun createProduct(p: ProductDto, stores: MutableMap<UUID, Store>): Product {
            val product = Product(p.name)
            product.uuid = p.uuid
            product.stores = p.stores.mapNotNull { uuid -> stores[uuid] }.toMutableSet()
            product.barcode = p.barcode
            product.isShared = p.isShared
            return product
        }

        fun updateProduct(p1: Product?, update: ProductDto, stores: MutableMap<UUID, Store>): Product {
            if (p1 === null) {
                return createProduct(update, stores)
            }
            p1.name = update.name
            p1.barcode = update.barcode
            p1.stores = update.stores.mapNotNull { uuid -> stores[uuid] }.toMutableSet()
            p1.isShared = update.isShared
            return p1
        }
    }
}