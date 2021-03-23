package pt.ulisboa.tecnico.cmov.shopist.domain

class PantryList(title: String) {
    val title = title.capitalize()
    // TODO: Get a location
    var location: String = ""
    // FIXME:
    var products: MutableList<Product> = mutableListOf()

    fun addProduct(product: Product) {
        products.add(product)
    }
}
