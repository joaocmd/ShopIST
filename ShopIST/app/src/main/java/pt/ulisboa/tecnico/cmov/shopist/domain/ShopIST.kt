package pt.ulisboa.tecnico.cmov.shopist.domain

import android.app.Application
import android.util.Log
import com.android.volley.VolleyError
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList.ShoppingList
import pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList.ShoppingListItem
import pt.ulisboa.tecnico.cmov.shopist.utils.API
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.NoSuchElementException

class ShopIST : Application() {
    companion object {
        fun createUri(pantryList: PantryList): String {
            return "https://shopist/${pantryList.uuid}"
        }

        const val TAG = "shopist.domain.ShopIST"
        const val FILENAME_DATA = "data.json"
        const val OPEN_AUTO_MAX_DISTANCE = 50
    }

    private var allPantries: MutableMap<UUID, PantryList> = mutableMapOf()
    private var allProducts: MutableMap<UUID, Product> = mutableMapOf()
    private var allStores: MutableMap<UUID, Store> = mutableMapOf()
    private var defaultStore: Store? = null

    var currentShoppingListItem: ShoppingListItem? = null

    val pantries: Array<PantryList>
        get() = this.allPantries.values.sortedBy { it.name }.toTypedArray()

    val stores: Array<Store>
        get() = this.allStores.values.sortedBy { it.name }.toTypedArray()

    fun addPantryList(pantryList: PantryList) {
        allPantries[pantryList.uuid] = pantryList
    }

    fun getPantryList(uuid: UUID): PantryList {
        return allPantries[uuid]!!
    }

    fun loadPantryList(uuid: UUID, onSuccessListener: (response: UUID) -> Unit,
                       onErrorListener: (error: Exception) -> Unit) {
        if (allPantries.containsKey(uuid)) {
            onSuccessListener(uuid)
            return
        } else {
            API.getInstance(applicationContext).getPantry(uuid, {
                populateFromServer(it)
                onSuccessListener(it.pantry.uuid)
            }, onErrorListener)
        }
    }

    private fun populateFromServer(dto: BigBoyDto) {
        // Set stores
        dto.stores.forEach { s -> allStores[s.uuid] = Store.createStore(s) }

        // Set products
        dto.products.forEach { p -> allProducts[p.uuid] = Product.createProduct(p, allStores)}

        // Set pantry
        allPantries[dto.pantry.uuid] = PantryList(dto.pantry, allProducts)

        Log.d(TAG, dto.pantry.name)
        savePersistent()
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

    fun addStore(store: Store) {
        allStores[store.uuid] = store
    }

    fun getStore(uuid: UUID): Store {
        return allStores[uuid]!!
    }

    fun getShoppingList(uuid: UUID): ShoppingList {
        return ShoppingList(allStores[uuid]!!, allPantries.values)
    }

    fun setDefaultStore(store: Store) {
        defaultStore = store
    }

    fun getDefaultStore(): Store? {
        return defaultStore
    }

    fun getAllLists(): List<Distanceable> {
        val res = mutableListOf<Distanceable>()
        allPantries.forEach {
            res.add(it.value)
        }
        allStores.forEach {
            res.add(it.value)
        }
        return res
    }

    //--------------

    fun startUp() {
        // Load previous data
        loadPersistent()

        // FIXME: Remove for production
        if (allPantries.isEmpty()) {
            val store1 = Store("Bom Dia", LatLng(38.73361076643277,-9.142712429165842))
            val store2 = Store("Pingo Doce", LatLng(38.735076664409554,-9.14225209504366))
            val store3 = Store("Intermarche", LatLng(38.73595121972168,-9.141665026545525))

            addStore(store1)
            addStore(store2)
            addStore(store3)
            setDefaultStore(store2)

            val product1 = Product("Pasta de Dentes")
            product1.stores.add(store1)
            product1.stores.add(store2)

            val product2 = Product("Escova de Dentes")
            product2.stores.add(store2)
            product2.stores.add(store3)

            val product3 = Product("Baguette")
            product3.stores.add(store1)
            product3.stores.add(store3)

            val product4 = Product("Croissant de Chocolate")
            product4.stores.add(store1)
            product4.stores.add(store2)
            product4.stores.add(store3)

            addProduct(product1)
            addProduct(product2)
            addProduct(product3)
            addProduct(product4)

            val pantry1 = PantryList("Dani's Pantry")
            pantry1.location = LatLng(38.73783576632948, -9.137839190661907)
            pantry1.addItem(Item(product1, pantry1, 10, 0, 0))
            pantry1.addItem(Item(product2, pantry1, 2, 0, 0))
            addPantryList(pantry1)

            val pantry2 = PantryList("Joca's Pantry")
            pantry2.location = LatLng(38.732010405640224, -9.142283610999584)
            pantry2.addItem(Item(product3, pantry2, 1, 1, 1))
            pantry2.addItem(Item(product4, pantry2, 2, 0, 2))
            addPantryList(pantry2)
        }
    }

    inner class ShopISTDto() {
        var pantriesList: MutableList<PantryListDto> = mutableListOf()
        var products: MutableList<ProductDto> = mutableListOf()
        var stores: MutableList<StoreDto> = mutableListOf()
        var defaultStoreId: UUID? = null

        constructor(shopIST: ShopIST) : this() {
            pantriesList = shopIST.allPantries.values.map { p -> PantryListDto(p) }.toMutableList()
            products = shopIST.allProducts.values.map { p -> ProductDto(p) }.toMutableList()
            stores = shopIST.allStores.values.map { s -> StoreDto(s) }.toMutableList()
            if (shopIST.defaultStore != null) {
                defaultStoreId = shopIST.defaultStore!!.uuid
            }
        }
    }

    private fun populateShopIST(shopISTDto: ShopISTDto) {
        // Set stores
        shopISTDto.stores.forEach { s -> allStores[s.uuid] = Store.createStore(s) }

        // Set default store
        if (shopISTDto.defaultStoreId != null) {
            defaultStore = allStores[shopISTDto.defaultStoreId!!]
        }

        // Set products
        shopISTDto.products.forEach { p -> allProducts[p.uuid] = Product.createProduct(p, allStores)}

        // Set pantries
        val pairs = shopISTDto.pantriesList
            .map { p -> Pair(p.uuid,
                PantryList(p, allProducts)
            ) }
        allPantries = mutableMapOf(*pairs.toTypedArray())
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
            } else {
                return false
            }
        }
        // Try to build file
        try {
            val shopIstDto = Gson().fromJson(sb.toString(), ShopISTDto()::class.java)
            populateShopIST(shopIstDto)
        } catch (e: Exception) {
            // TODO: Detect if it is the first time using app, otherwise say that data was lost
            Log.d(TAG, "Can't read data file.")
        }
        return result
    }

    fun savePersistent() {
        // Get dto
        val shopIstDto = ShopISTDto(this)
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
