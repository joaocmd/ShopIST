package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

class Product(name: String) {
    var uuid: UUID = UUID.randomUUID()
    var name: String = name
    var barcode: String? = null
    // TODO: Construct images from dto
    var images: MutableList<String> = mutableListOf()
    var stores: MutableSet<Store> = mutableSetOf()
    var isShared = false

    companion object {
        fun createProduct(p: ProductDto, stores: MutableMap<UUID, Store>): Product {
            val product = Product(p.name)
            product.uuid = p.uuid
            product.stores = p.stores.mapNotNull { uuid -> stores[uuid] }.toMutableSet()
            product.barcode = p.barcode
            product.isShared = p.isShared
            product.images = p.images
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
            p1.images = update.images
            return p1
        }
    }

    fun addImage(name: String) {
        images.add(name)
    }

    fun getLastImageIndex(): Int {
        return images.size - 1
    }
}