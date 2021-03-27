package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

class Product(name: String) {
    // TODO: Save barcode, etc.
    var uuid: UUID = UUID.randomUUID()
    var name: String = name
    var barcode: Int? = null
    var image: String? = null

    companion object {
        fun createProduct(p: ProductDto): Product {
            val product = Product(p.name)
            product.uuid = p.uuid
            return product
        }
    }
}