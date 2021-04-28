package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

class Product(var name: String, originLang: Languages?): Translatable(name, originLang) {
    var uuid: UUID = UUID.randomUUID()
    var barcode: String? = null
    var images: MutableList<String> = mutableListOf()
    var stores: MutableSet<Store> = mutableSetOf()
    var prices: MutableMap<Store, Number> = mutableMapOf()
    var isShared = false

    companion object {
        fun createProduct(p: ProductDto, stores: MutableMap<UUID, Store>): Product {
            val product = Product(p.name, null)
            product.uuid = p.uuid
            product.stores = p.stores.mapNotNull { uuid -> stores[uuid] }.toMutableSet()
            p.barcode?.let {
                product.barcode = it
            }
            product.isShared = p.isShared
            p.images?.let {
                product.images = it
            }
            p.prices?.forEach {
                stores[it.key]?.let { s ->
                    product.prices[s] = it.value
                }
            }
            p.lang?.let {
                product.originLang = Language.languages[it]!!
            }
            return product
        }

        fun updateProduct(p1: Product?, update: ProductDto, stores: MutableMap<UUID, Store>): Product {
            if (p1 === null) {
                return createProduct(update, stores)
            }
            if (update.name != p1.name) {
                p1.hasTranslatedToLanguage = null
                p1.translatedText = ""
                p1.name = update.name
            }
            update.barcode?.let {
                p1.barcode = update.barcode
            }
            p1.stores = update.stores.mapNotNull { uuid -> stores[uuid] }.toMutableSet()
            p1.isShared = update.isShared
            update.images?.let {
                p1.images = it
            }
            update.prices?.forEach {
                stores[it.key]?.let { s ->
                    p1.prices[s] = it.value
                }
            }
            update.lang?.let {
                p1.originLang = Language.languages[it]!!
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

    fun getLastImageName(): String {
        return "${images[getLastImageIndex()]}${ShopIST.IMAGE_EXTENSION}"
    }

    fun getLastImageId(): String {
        return images[getLastImageIndex()]
    }

    fun setPrice(store: Store, price: Number) {
        prices[store] = price
    }

    fun getPrice(store: Store): Number? {
        return prices[store]
    }

    fun share() {
        isShared = true
    }

    fun clearStores() {
        stores = mutableSetOf()
    }

    fun addStore(store: Store) {
        stores.add(store)
    }

    fun hasStore(uuid: UUID): Boolean {
        return stores.filter { it.uuid == uuid }.size == 1
    }

    fun removeStore(uuid: UUID) {
        stores.removeIf {
            it.uuid == uuid
        }
    }

    fun setLang(lang: Languages) {
        this.originLang = lang
    }

    fun getTranslatedName(): String {
        return if (this.translatedText.isEmpty()) {
            this.originText
        } else {
            this.translatedText
        }
    }
}