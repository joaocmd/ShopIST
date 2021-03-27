package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

data class ProductDto(val uuid: UUID, val name: String) {
    var barcode: Int? = null
    var image: String? = null

    constructor(p: Product) : this(p.uuid, p.name) {
    }
}