package pt.ulisboa.tecnico.cmov.shopist.domain

class PantryList(title: String) {
    companion object {
        fun createPantry(title: String, products: MutableList<Product>): PantryList {
            val pantry = PantryList(title)
            pantry.products = products
            return pantry
        }
    }

    val title = title.capitalize()
    // TODO: Get a location
    var location: String = ""
    // FIXME:
    var products: MutableList<Product> = mutableListOf()

    fun addProduct(product: Product) {
        products.add(product)
    }


}

