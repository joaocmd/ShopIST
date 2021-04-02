package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

class Product(name: String) {
    // TODO: Save image, etc.
    var uuid: UUID = UUID.randomUUID()
    var name: String = name
    var barcode: String? = null
    var image: String? = null
    var stores: MutableSet<Store> = mutableSetOf()

    companion object {
        fun createProduct(p: ProductDto, stores: MutableMap<UUID, Store>): Product {
            val product = Product(p.name)
            product.uuid = p.uuid
            product.stores = p.stores.mapNotNull { uuid -> stores[uuid] }.toMutableSet()
            product.barcode = p.barcode
            return product
        }
    }
}