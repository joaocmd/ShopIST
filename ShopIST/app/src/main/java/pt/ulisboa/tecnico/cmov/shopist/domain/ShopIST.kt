package pt.ulisboa.tecnico.cmov.shopist.domain

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.widget.Toast
import com.android.volley.VolleyError
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList.ShoppingList
import pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList.ShoppingListItem
import pt.ulisboa.tecnico.cmov.shopist.utils.API
import pt.ulisboa.tecnico.cmov.shopist.utils.cache.LruDiskCache
import java.io.*
import java.util.*

class ShopIST : Application() {
    companion object {
        fun createUri(pantryList: PantryList): String {
            return "https://shopist/${pantryList.uuid}"
        }

        const val TAG = "shopist.domain.ShopIST"
        const val FILENAME_DATA = "data.json"
        const val FILENAME_FIRST_TIME = "first.json"
        const val OPEN_AUTO_MAX_DISTANCE = 50
        const val IMAGE_EXTENSION = ".png"
        const val IMAGE_FOLDER = "photos"
        const val LOCAL_IMAGE_FOLDER = "local_photos"

        const val IMAGE_CACHE_SIZE = 10 * 1024; // 10 MiB (cache size in KiB)
    }

    private var firstTime = true

    private var _currentLocation: LatLng? = null
    var currentLocation: LatLng?
        get() {
            return _currentLocation
        }
        set(value) {
            _currentLocation = value
            updateDrivingTimes()
        }

    private fun updateDrivingTimes() {
        _currentLocation ?: return
        getAllLists().forEach {
            if (it.location != null) {
                API.getInstance(applicationContext).getRouteTime(
                    currentLocation!!,
                    it.location!!,
                    { time -> it.drivingTime = time },
                    { }
                )
            }
        }
        callbackDataSetChanged?.invoke()
    }

    private var allPantries: MutableMap<UUID, PantryList> = mutableMapOf()
    private var allProducts: MutableMap<UUID, Product> = mutableMapOf()
    private var allStores: MutableMap<UUID, Store> = mutableMapOf()
    private var defaultStore: Store? = null

    var currentShoppingListItem: ShoppingListItem? = null
    var pantryToOpen: PantryList? = null
    var isAPIConnected = false
    var callbackDataSetChanged: (() -> Unit)? = null

    val pantries: Array<PantryList>
        get() = this.allPantries.values.sortedBy { it.name }.toTypedArray()

    val stores: Array<Store>
        get() = this.allStores.values.sortedBy { it.name }.toTypedArray()

    val imageCache: LruDiskCache = LruDiskCache(IMAGE_CACHE_SIZE, this)

    fun addPantryList(pantryList: PantryList) {
        allPantries[pantryList.uuid] = pantryList
    }

    fun getPantryList(uuid: UUID): PantryList {
        return allPantries[uuid]!!
    }

    fun loadPantryList(uuid: UUID, onSuccessListener: (response: UUID) -> Unit,
                       onErrorListener: (error: VolleyError) -> Unit) {
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

    fun populateFromServer(dto: BigBoyDto) {
        // Set stores
        dto.stores.forEach { s -> allStores[s.uuid] = Store.createStore(s) }
        // dto.stores.forEach { s ->
        //     run {
        //         val s1 = allStores[s.uuid]
        //         allStores[s.uuid] = Store.updateStore(s1, s)
        //     }
        // }

        // Set products
        dto.products.forEach { p -> allProducts[p.uuid] = Product.createProduct(p, allStores)}
        // dto.products.forEach { p ->
        //     run {
        //         val p1 = allProducts[p.uuid]
        //         allProducts[p.uuid] = Product.updateProduct(p1, p, allStores)
        // }}

        // Set pantry
        allPantries[dto.pantry.uuid] = PantryList(dto.pantry, allProducts)
        // val pantry = allPantries[dto.pantry.uuid]
        // allPantries[dto.pantry.uuid] = PantryList.updatePantry(pantry, dto.pantry, allProducts)

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

    fun getClosestStore(currLocation: LatLng): Store? {
        val closestStore = stores
            .filter { it.location != null }
            .minByOrNull { it.getDistance(currLocation) }
        return if (closestStore === null || closestStore.getDistance(currLocation) > OPEN_AUTO_MAX_DISTANCE) null
        else closestStore
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

    fun getAllLists(): List<Locatable> {
        val res = mutableListOf<Locatable>()
        allPantries.forEach {
            res.add(it.value)
        }
        allStores.forEach {
            res.add(it.value)
        }
        return res
    }

    fun getImageFolder(): File {
        val cw = ContextWrapper(applicationContext)
        val folder = cw.getDir(IMAGE_FOLDER, Context.MODE_PRIVATE)
        if (!folder.exists()) {
            folder.mkdir()
        }
        return folder
    }

    fun getLocalImageFolder(): File {
        val cw = ContextWrapper(applicationContext)
        val folder = cw.getDir(LOCAL_IMAGE_FOLDER, Context.MODE_PRIVATE)
        if (!folder.exists()) {
            folder.mkdir()
        }
        return folder
    }

    //--------------

    fun startUp() {
        // Set if first time
        markFirstTime()

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
            pantry1.isShared = true
            API.getInstance(applicationContext).updatePantry(pantry1)
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
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "File not found")
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
            if (!firstTime) {
                Toast.makeText(applicationContext, getString(R.string.error_loading_file), Toast.LENGTH_SHORT).show()
            }
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

    fun markFirstTime() {
        try {
            openFileInput(FILENAME_FIRST_TIME)
            firstTime = false
            return
        } catch (e: FileNotFoundException) {
            firstTime = true
            Log.d(TAG, "First time opening app.")
        }

        val json = Gson().toJson("true")
        var fos: FileOutputStream? = null
        try {
            fos = openFileOutput(FILENAME_FIRST_TIME, MODE_PRIVATE)
            fos.write(json.toByteArray())
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
