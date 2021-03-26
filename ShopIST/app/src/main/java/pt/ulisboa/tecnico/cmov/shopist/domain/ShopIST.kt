package pt.ulisboa.tecnico.cmov.shopist.domain

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class ShopIST : Application() {
    companion object {
        const val TAG = "shopist.domain.ShopIST"
        const val FILENAME_DATA = "data.json"
        val LINE_SEP = System.getProperty("line.separator")
    }

    private var allPantry: MutableList<PantryList> = mutableListOf()

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

    fun startUp() {
        // Load previous data
        loadPersistent()

        // For testing purposes
        if (allPantry.size == 0) {
            val pantry1 = PantryList("My Testing Pantry")
            pantry1.addProduct(Product("1-Product1", 1))
            pantry1.addProduct(Product("1-Product2", 2))
            pantry1.addProduct(Product("1-Product3", 3))
            addPantryList(pantry1)

            val pantry2 = PantryList("My Second Testing Pantry")
            pantry2.addProduct(Product("2-Product1", 4))
            pantry2.addProduct(Product("2-Product2", 6))
            pantry2.addProduct(Product("2-Product3", 6))
            addPantryList(pantry2)
        }
    }

    private fun loadPersistent(): Boolean {
        var fis: FileInputStream? = null
        var scanner: Scanner? = null
        val sb = StringBuilder()
        var result = true
        try {
            fis = openFileInput(FILENAME_DATA)
            // scanner does mean one more object, but it's easier to work with
            scanner = Scanner(fis)
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine())
            }
            Toast.makeText(this, "File read", Toast.LENGTH_SHORT).show()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "File not found", e)
            result = false
        } finally {
            if (fis != null) {
                try {
                    fis.close()
                    scanner?.close()
                    result = result && true
                } catch (e: IOException) {
                    Log.d(TAG, "Close error.")
                }
            }
        }
        // Try to build file
        try {
            val pantriesDto = Gson().fromJson(sb.toString(), Array<PantryDto>::class.java)
            allPantry = pantriesDto.map { p ->
                PantryList.createPantry(
                    p.title,
                    p.products
                )
            } as MutableList<PantryList>
        } catch (e: Exception) {
            Log.d(TAG, "Can't read data file.")
        }
        return result
    }

    fun savePersistent() {
        // Get array of pantries
        val pantries = allPantry.map { p -> PantryDto(p.title, p.products) }
        val json = Gson().toJson(pantries)

        var fos: FileOutputStream? = null
        try {
            fos = openFileOutput(FILENAME_DATA, MODE_PRIVATE)
            fos.write(json.toByteArray())
            // Toast.makeText(this, "File written", Toast.LENGTH_SHORT).show()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "File not found", e)
        } catch (e: IOException) {
            Log.e(TAG, "IO problem", e)
        } finally {
            try {
                fos!!.close()
            } catch (e: IOException) {
                Log.d(TAG, "Close error.")
            }
        }
    }
}