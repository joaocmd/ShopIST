package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

data class ProductDto(val uuid: UUID, val name: String) {
    var barcode: Int? = null
    var image: String? = null
    var stores: MutableSet<UUID> = mutableSetOf()

    constructor(p: Product) : this(p.uuid, p.name) {
        stores = p.stores.map { s -> s.uuid }.toMutableSet()
    }
}