package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

class Product(name: String) {
    var uuid: UUID = UUID.randomUUID()
    var name: String = name
    var barcode: String? = null
    var images: MutableList<String> = mutableListOf()
    var stores: MutableSet<Store> = mutableSetOf()
    var prices: MutableMap<Store, Number> = mutableMapOf()
    var isShared = false

    companion object {
        fun createProduct(p: ProductDto, stores: MutableMap<UUID, Store>): Product {
            val product = Product(p.name)
            product.uuid = p.uuid
            product.stores = p.stores.mapNotNull { uuid -> stores[uuid] }.toMutableSet()
            product.barcode = p.barcode
            product.isShared = p.isShared
            product.images = p.images
            p.prices.forEach {
                stores[it.key]?.let { s ->
                    product.prices[s] = it.value
                }
            }
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
            update.prices.forEach {
                stores[it.key]?.let { s ->
                    p1.prices[s] = it.value
                }
            }
            return p1
        }
    }

    fun addImage(name: String) {
        images.add(name)
    }

    fun getLastImageIndex(): Int {
        return images.size - 1
    }

    fun setPrice(store: Store, price: Number) {
        prices[store] = price
    }

    fun getPrice(store: Store): Number? {
        return prices[store]
    }
}