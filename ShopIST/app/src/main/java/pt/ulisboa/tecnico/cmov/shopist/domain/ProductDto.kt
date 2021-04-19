package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

data class ProductDto(val uuid: UUID, val name: String) {
    var barcode: String? = null
    var images: MutableList<String>? = mutableListOf()
    var stores: MutableSet<UUID> = mutableSetOf()
    var isShared = false
    var prices: MutableMap<UUID, Number>? = mutableMapOf()

    constructor(p: Product) : this(p.uuid, p.name) {
        stores = p.stores.map { s -> s.uuid }.toMutableSet()
        barcode = p.barcode
        isShared = p.isShared
        images = p.images
        prices = p.prices.map { it.key.uuid to it.value }.toMap().toMutableMap()
    }
}