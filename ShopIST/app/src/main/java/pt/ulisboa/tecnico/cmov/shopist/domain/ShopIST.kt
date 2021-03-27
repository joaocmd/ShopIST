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
    }

    private var allPantry: MutableList<PantryList> = mutableListOf()
    private var allProducts: MutableMap<UUID, Product> = mutableMapOf()

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

    fun addProduct(product: Product) {
        allProducts[product.uuid] = product
    }

    fun getAllProducts(): List<Product> {
        return allProducts.values.toList()
    }

    fun getProduct(uuid: UUID): Product? {
        return allProducts[uuid]
    }

    //--------------

    fun startUp() {
        // Load previous data
        loadPersistent()

        // For testing purposes
        if (allPantry.size == 0) {
            val product1 = Product("Pasta de Dentes")
            val product2 = Product("Escova de Dentes")
            val product3 = Product("Baguette")
            val product4 = Product("Croissant de Chocolate")

            addProduct(product1)
            addProduct(product2)
            addProduct(product3)
            addProduct(product4)

            val pantry1 = PantryList("Dani's Pantry")
            pantry1.addItem(Item(product1, 10))
            pantry1.addItem(Item(product2, 2))
            addPantryList(pantry1)

            val pantry2 = PantryList("Joca's Pantry")
            pantry2.addItem(Item(product3, 1))
            pantry2.addItem(Item(product4, 2))
            addPantryList(pantry2)
        }
    }

    inner class ShopISTDto {
        var pantries: MutableList<PantryDto> = mutableListOf()
        var products: MutableList<ProductDto> = mutableListOf()
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
            val shopIstDto = Gson().fromJson(sb.toString(), ShopISTDto()::class.java)
            // Set products
            shopIstDto.products.map { p -> allProducts[p.uuid] = Product.createProduct(p)}

            // Set pantries
            allPantry = shopIstDto.pantries.map { p -> PantryList.createPantry(p, allProducts) }.toMutableList()
        } catch (e: Exception) {
            // TODO: Detect if it is the first time using app, otherwise say that data was lost
            Log.d(TAG, "Can't read data file.")
        }
        return result
    }

    fun savePersistent() {
        // Get dto
        val shopIstDto = ShopISTDto()
        shopIstDto.pantries = allPantry.map { p -> PantryDto(p) }.toMutableList()
        shopIstDto.products = allProducts.values.map { p -> ProductDto(p) }.toMutableList()
        val json = Gson().toJson(shopIstDto)

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
