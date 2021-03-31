package pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList

data class Quantity(var pantry: Int, var needing: Int, var cart: Int) {
    fun add(other: Quantity) {
        this.pantry += other.pantry
        this.needing += other.needing
        this.cart += other.cart
    }
}
