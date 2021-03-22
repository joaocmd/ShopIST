package pt.ulisboa.tecnico.cmov.shopist.domain

import android.app.Application

class ShopIST : Application() {
    private val allPantry: MutableList<PantryList> = mutableListOf()

    val pantries: Array<PantryList>
        get() = this.allPantry.toTypedArray()

    fun addPantryList(note: PantryList) {
        allPantry.add(note)
    }

    fun getPantryList(idx: Int): PantryList {
        return allPantry[idx]
    }
}