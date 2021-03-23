package pt.ulisboa.tecnico.cmov.shopist.domain

import android.app.Application

class ShopIST : Application() {
    companion object {
        const val TAG = "ShopIST"
    }

    private val allPantry: MutableList<PantryList> = mutableListOf()

    val pantries: Array<PantryList>
        get() = this.allPantry.toTypedArray()

    fun addPantryList(pantryList: PantryList) {
        allPantry.add(pantryList)
    }

    fun getPantryList(idx: Int): PantryList {
        // if (idx < 0 || idx >= allPantry.size) {
        // }
        return allPantry[idx]
    }

    // For testing purposes
    fun startUp() {
        val pantry1 = PantryList("My Testing Pantry")
        pantry1.addProduct(Product("1-Product1"))
        pantry1.addProduct(Product("1-Product2"))
        pantry1.addProduct(Product("1-Product3"))
        addPantryList(pantry1)

        val pantry2 = PantryList("My Second Testing Pantry")
        pantry2.addProduct(Product("2-Product1"))
        pantry2.addProduct(Product("2-Product2"))
        pantry2.addProduct(Product("2-Product3"))
        addPantryList(pantry2)
    }
}