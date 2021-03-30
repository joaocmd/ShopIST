package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

class Product(name: String) {
    // TODO: Save barcode, etc.
    var uuid: UUID = UUID.randomUUID()
    var name: String = name
    var barcode: Int? = null
    var image: String? = null
    // TODO: Add store on product creation interface
    var stores: MutableSet<Store> = mutableSetOf()

    companion object {
        fun createProduct(p: ProductDto, stores: MutableMap<UUID, Store>): Product {
            val product = Product(p.name)
            product.uuid = p.uuid
            product.stores = p.stores.mapNotNull { uuid -> stores[uuid] }.toMutableSet()
            return product
        }
    }
}