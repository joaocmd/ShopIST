package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

data class ProductDto(val name: String, val uuid: UUID) {
    var barcode: Int? = null
    var image: String? = null

    constructor(p: Product) : this(p.name, p.uuid) {
    }
}