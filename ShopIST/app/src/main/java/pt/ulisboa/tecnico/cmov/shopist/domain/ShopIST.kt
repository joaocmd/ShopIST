package pt.ulisboa.tecnico.cmov.shopist.domain

import android.app.Application
import android.util.Log
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

    private var allPantries: MutableMap<UUID, PantryList> = mutableMapOf()
    private var allProducts: MutableMap<UUID, Product> = mutableMapOf()
    private var allShoppingLists: MutableMap<UUID, ShoppingList> = mutableMapOf()

    val pantries: Array<PantryList>
        get() = this.allPantries.values.sortedBy { it.title }.toTypedArray()

    val shoppingLists: Array<ShoppingList>
        get() = this.allShoppingLists.values.sortedBy { it.title }.toTypedArray()

    fun addPantryList(pantryList: PantryList) {
        allPantries[pantryList.uuid] = pantryList
    }

    fun getPantryList(uuid: UUID): PantryList {
        return allPantries[uuid]!!
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

    fun addShoppingList(shoppingList: ShoppingList) {
        allShoppingLists[shoppingList.uuid] = shoppingList
    }

    fun getShoppingList(uuid: UUID): ShoppingList {
        return allShoppingLists[uuid]!!
    }

    //--------------

    fun startUp() {
        // Load previous data
        loadPersistent()

        // FIXME: Remove for production
        if (allPantries.size == 0) {
            val product1 = Product("Pasta de Dentes")
            val product2 = Product("Escova de Dentes")
            val product3 = Product("Baguette")
            val product4 = Product("Croissant de Chocolate")

            addProduct(product1)
            addProduct(product2)
            addProduct(product3)
            addProduct(product4)

            val pantry1 = PantryList("Dani's Pantry")
            pantry1.addItem(Item(product1, pantry1, 10, 0, 0))
            pantry1.addItem(Item(product2, pantry1, 2, 0, 0))
            addPantryList(pantry1)

            val pantry2 = PantryList("Joca's Pantry")
            pantry2.addItem(Item(product3, pantry2, 1, 1, 1))
            pantry2.addItem(Item(product4, pantry2, 2, 0, 2))
            addPantryList(pantry2)
        }
    }

    inner class ShopISTDto {
        var pantriesList: MutableList<PantryListDto> = mutableListOf()
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
            // Toast.makeText(this, "File read", Toast.LENGTH_SHORT).show()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "File not found", e)
            result = false
        } finally {
            if (fis != null) {
                try {
                    fis.close()
                    scanner?.close()
                } catch (e: IOException) {
                    Log.d(TAG, "Close error.")
                }
            }
        }
        // Try to build file
        try {
            val shopIstDto = Gson().fromJson(sb.toString(), ShopISTDto()::class.java)
            // TODO: Refactor this to use the proper class constructor
            // Set products
            shopIstDto.products.map { p -> allProducts[p.uuid] = Product.createProduct(p)}

            // Set pantries
            val pairs = shopIstDto.pantriesList
                .map { p -> Pair(p.uuid,
                    PantryList(p, allProducts)
                ) }
            allPantries = mutableMapOf(*pairs.toTypedArray())
        } catch (e: Exception) {
            // TODO: Detect if it is the first time using app, otherwise say that data was lost
            Log.d(TAG, "Can't read data file.")
        }
        return result
    }

    fun savePersistent() {
        // Get dto
        // TODO: Refactor this to create the DTO using the DTO constructor
        val shopIstDto = ShopISTDto()
        shopIstDto.pantriesList = allPantries.values.map { p -> PantryListDto(p) }.toMutableList()
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
