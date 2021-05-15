package pt.ulisboa.tecnico.cmov.shopist.domain

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.net.*
import android.util.Log
import android.widget.Toast
import com.android.volley.VolleyError
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList.ShoppingList
import pt.ulisboa.tecnico.cmov.shopist.domain.shoppingList.ShoppingListItem
import pt.ulisboa.tecnico.cmov.shopist.ui.dialogs.PromptMessage
import pt.ulisboa.tecnico.cmov.shopist.utils.*
import pt.ulisboa.tecnico.cmov.shopist.utils.cache.LruDiskCache
import java.io.*
import java.util.*


class ShopIST : Application() {
    companion object {
        fun createUri(pantryList: PantryList): String {
            return "https://shopist/pantry/${pantryList.uuid}"
        }

        fun createUri(product: Product): String {
            return "https://shopist/product/${product.uuid}"
        }

        const val TAG = "shopist.domain.ShopIST"
        const val FILENAME_DATA = "data.json"
        const val FILENAME_DEVICE_ID = "id.json"
        const val OPEN_AUTO_MAX_DISTANCE = 50
        const val IMAGE_EXTENSION = ".png"
        const val IMAGE_FOLDER = "photos"
        const val LOCAL_IMAGE_FOLDER = "local_photos"

        const val IMAGE_CACHE_SIZE = 10 * 1024 // 10 MiB (cache size in KiB)
    }

    private var firstTime = true

    var languageSettings = Language()
    var promptSettings = UserPromptStorage()
    lateinit var deviceId: UUID
        private set

    private var _currentLocation: LatLng? = null
    var currentLocation: LatLng?
        get() {
            return _currentLocation
        }
        set(value) {
            _currentLocation = value
            updateDrivingTimes()
        }

    private var allPantries: MutableMap<UUID, PantryList> = mutableMapOf()
    private var allProducts: MutableMap<UUID, Product> = mutableMapOf()
    private var allStores: MutableMap<UUID, Store> = mutableMapOf()
    private var defaultStore: Store? = null

    var currentShoppingList: ShoppingList? = null
    var currentShoppingListItem: ShoppingListItem? = null
    var pantryToOpen: PantryList? = null
    var productToOpen: Product? = null
    var isAPIConnected = false
    var hasWiFi = false
    var callbackDataSetChanged: (() -> Unit)? = null

    val pantries: Array<PantryList>
        get() = this.allPantries.values.sortedBy { it.name }.toTypedArray()

    val stores: Array<Store>
        get() = this.allStores.values.sortedBy { it.name }.toTypedArray()

    val imageCache: LruDiskCache = LruDiskCache(IMAGE_CACHE_SIZE, this)

    // LinkedHashSet keeps order of insertion, re-adding an element does not push it to the end
    val productOrder: HashSet<String> = LinkedHashSet()

    fun addPantryList(pantryList: PantryList) {
        allPantries[pantryList.uuid] = pantryList
    }

    fun getPantryList(uuid: UUID): PantryList {
        return allPantries[uuid]!!
    }

    fun deletePantryList(pantryList: PantryList) {
        allPantries.remove(pantryList.uuid)
    }

    fun loadProduct(
        uuid: UUID, onSuccessListener: (response: UUID) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        if (allProducts.containsKey(uuid)) {
            onSuccessListener(uuid)
            return
        } else {
            API.getInstance(applicationContext).getProduct(uuid, {
                addProduct(Product.createProduct(it, allStores))
                onSuccessListener(it.uuid)
            }, onErrorListener)
        }
    }

    fun loadPantryList(
        uuid: UUID, onSuccessListener: (response: UUID) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
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

    fun populateFromServer(dto: PantryUpdateDto) {
        // Set stores
        dto.stores.forEach { s ->
            run {
                val s1 = allStores[s.uuid]
                allStores[s.uuid] = Store.updateStore(s1, s)
            }
        }

        // Set products
        dto.products.forEach { p ->
            run {
                val p1 = allProducts[p.uuid]
                val product = Product.updateProduct(p1, p, allStores)
                val lang = getLang()
                product.getText(lang, applicationContext) {
                    product.translatedText = it
                    product.hasTranslatedToLanguage = lang
                    callbackDataSetChanged?.invoke()
                }
                allProducts[p.uuid] = product
        }}

        // Set pantry
        val pantry = allPantries[dto.pantry.uuid]
        allPantries[dto.pantry.uuid] = PantryList.updatePantry(pantry, dto.pantry, allProducts)

        savePersistent()
    }

    fun getCurrentDeviceLocation(act : Activity, callback: (() -> Unit)? = null){
        (LocationUtils(act)).getNewLocation { location ->
            currentLocation = location!!.toLatLng()
            callback?.invoke()
        }
    }

    fun addProduct(product: Product) {
        allProducts[product.uuid] = product
    }

    fun removeProduct(uuid: UUID) {
        val product = getAllProducts().find {
            it.uuid == uuid
        }

        product?.let {
            // Remove product from pantries
            pantries.forEach { pantry ->
                if (pantry.hasProduct(product)) {
                    pantry.removeItem(uuid)
                }
            }

            // Also remove from the current shopping list
            currentShoppingListItem?.shoppingList?.removeItem(it.uuid)

            // Remove product
            allProducts.remove(uuid)
        }
    }

    fun getAllProducts(): List<Product> {
        return allProducts.values.toList()
    }

    fun getProduct(uuid: UUID): Product? {
        return allProducts[uuid]
    }

    fun getProductByBarcode(barcode: String): Product? {
        return getAllProducts().find {
            it.barcode != null && it.barcode == barcode
        }
    }

    fun getPantriesWithProduct(uuid: UUID): List<PantryList> {
        return pantries.filter {
            it.hasProduct(uuid)
        }
    }

    fun getProductsWithStore(uuid: UUID): List<Product> {
        return getAllProducts().filter {
            it.hasStore(uuid)
        }
    }

    fun addStore(store: Store) {
        allStores[store.uuid] = store
    }

    fun removeStore(uuid: UUID) {
        // Remove from products
        getProductsWithStore(uuid).forEach {
            it.removeStore(uuid)
        }

        // Remove from global data
        allStores.remove(uuid)
    }

    fun getStore(uuid: UUID): Store {
        return allStores[uuid]!!
    }

    fun getClosestStoreTo(currLocation: LatLng): Store? {
        val closestStore = stores
            .filter { it.location != null }
            .minByOrNull { it.getDistance(currLocation) }
        return if (closestStore === null || closestStore.getDistance(currLocation) > OPEN_AUTO_MAX_DISTANCE) null
        else closestStore
    }

    private fun updateDrivingTimes() {
        _currentLocation ?: return
        getAllLists().filter { it.location != null }.forEach {
            API.getInstance(applicationContext).getRouteTime(
                currentLocation!!,
                it.location!!,
                { time ->
                    it.drivingTime = time
                    callbackDataSetChanged?.invoke()
                },
                { }
            )
        }
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
        res.addAll(allPantries.values)
        res.addAll(allStores.values)
        return res
    }

    fun getImageFolder(): File {
        val cw = ContextWrapper(applicationContext)
        val folder = File("${cw.cacheDir.absolutePath}/$IMAGE_FOLDER")
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

    fun getLang(): Languages {
        return when (LocaleHelper.getLanguage(baseContext)) {
            "EN" -> Languages.EN
            "PT" -> Languages.PT
            else -> Languages.EN
        }
    }

    private fun downloadFirstImageForProducts() {
        allProducts.values.filter { p -> p.isShared }.forEach { p ->
            if (!hasWiFi) return
            API.getInstance(this).getProduct(p.uuid, { pDto ->
                pDto.images?.let {
                    p.images = it

                    if (hasWiFi) {
                        imageCache.getAsImage(UUID.fromString(p.getLastImageId()), {}, {})
                    }
                }
            } ,{
                // Ignored
            })
        }
    }

    //--------------

    private fun setNetworkChangeCallback() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        connectivityManager?.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network : Network) {
                Log.d(TAG, "Has internet")
                hasWiFi = !connectivityManager.isActiveNetworkMetered
                val currentActive = connectivityManager.activeNetwork

                if (hasWiFi && currentActive != null) {
                    downloadFirstImageForProducts()
                }
            }

            override fun onLost(network : Network) {
                Log.d(TAG,"Lost a network")
                hasWiFi = !connectivityManager.isActiveNetworkMetered
                val currentActive = connectivityManager.activeNetwork
                if (currentActive == null) {
                    isAPIConnected = false
                }
            }
        })
    }

    fun startUp() {
        getDeviceId()
        setNetworkChangeCallback()

        // Load previous data
        if (!firstTime) {
            loadPersistent()
        }

        // FIXME: Remove for production
        if (allPantries.isEmpty()) {
            val store1 = Store("Bom Dia", LatLng(38.73361076643277, -9.142712429165842))
            val store2 = Store("Pingo Doce", LatLng(38.735076664409554, -9.14225209504366))
            val store3 = Store("Intermarche", LatLng(38.73595121972168, -9.141665026545525))

            addStore(store1)
            addStore(store2)
            addStore(store3)
            setDefaultStore(store2)

            // Products without barcode
            val product1 = Product("Pasta de Dentes", getLang())
            product1.stores.add(store2)

            val product2 = Product("Escova de Dentes", getLang())
            product2.stores.add(store2)
            product2.stores.add(store3)

            val product3 = Product("Baguette", getLang())
            product3.stores.add(store3)

            val product4 = Product("Croissant de Chocolate", getLang())
            product4.stores.add(store2)
            product4.stores.add(store3)

            addProduct(product1)
            addProduct(product2)
            addProduct(product3)
            addProduct(product4)

            // Product with barcode
            val productBar1 = Product("Boneco", getLang())
            productBar1.stores.add(store1)
            productBar1.barcode = "26907055"

            val productBar2 = Product("Par de porta-chaves", getLang())
            productBar2.stores.add(store1)
            productBar2.barcode = "8435460733861"

            addProduct(productBar1)
            addProduct(productBar2)

            val pantry1 = PantryList("Dani's Pantry")
            pantry1.uuid = UUID.fromString("fa999d5c-0f32-455f-a6ee-c38d680d1af8")
            pantry1.location = LatLng(38.73783576632948, -9.137839190661907)
            pantry1.addItem(Item(productBar1, pantry1, 10, 10, 0))
            pantry1.addItem(Item(productBar2, pantry1, 10, 10, 0))
            pantry1.share()
            // API.getInstance(applicationContext).updatePantry(pantry1)
            addPantryList(pantry1)

            val pantry2 = PantryList("Joca's Pantry")
            pantry2.location = LatLng(38.732010405640224, -9.142283610999584)
            pantry2.addItem(Item(product1, pantry1, 10, 4, 0))
            pantry2.addItem(Item(product2, pantry1, 2, 13, 0))
            pantry2.addItem(Item(product3, pantry2, 1, 23, 0))
            pantry2.addItem(Item(product4, pantry2, 2, 6, 0))
            addPantryList(pantry2)

            savePersistent()
        }
    }

    inner class ShopISTDto() {
        var pantriesList: MutableList<PantryListDto> = mutableListOf()
        var products: MutableList<ProductDto> = mutableListOf()
        var stores: MutableList<StoreDto> = mutableListOf()
        var defaultStoreId: UUID? = null
        var deviceId: UUID? = null
        var currentLang: String? = null
        var promptSettings: MutableMap<PromptMessage, Boolean>? = null

        constructor(shopIST: ShopIST) : this() {
            pantriesList = shopIST.allPantries.values.map { p -> PantryListDto(p) }.toMutableList()
            products = shopIST.allProducts.values.map { p ->
                val newP = ProductDto(p)
                if (newP.lang == null) {
                    newP.lang = getLang().language
                }
                newP
            }.toMutableList()
            stores = shopIST.allStores.values.map { s -> StoreDto(s) }.toMutableList()
            if (shopIST.defaultStore != null) {
                defaultStoreId = shopIST.defaultStore!!.uuid
            }
            currentLang = languageSettings.currentLanguage?.language
            promptSettings = shopIST.promptSettings.savedPrompts
        }
    }

    private fun populateShopIST(shopISTDto: ShopISTDto) {
        shopISTDto.currentLang?.let {
            languageSettings.currentLanguage = Language.languages[it]!!
        }

        shopISTDto.promptSettings?.let { settings ->
            this.promptSettings = UserPromptStorage(settings)
        }

        // Set stores
        shopISTDto.stores.forEach { s -> allStores[s.uuid] = Store.createStore(s) }

        // Set default store
        if (shopISTDto.defaultStoreId != null) {
            defaultStore = allStores[shopISTDto.defaultStoreId!!]
        }

        // Set products
        shopISTDto.products.forEach { p ->
            allProducts[p.uuid] = Product.createProduct(p, allStores)
        }

        // Set pantries
        val pairs = shopISTDto.pantriesList
            .map { p -> Pair(
                p.uuid,
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
            if (!firstTime) {
                Log.e(TAG, "File not found")
                result = false
            }
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
                Toast.makeText(
                    applicationContext,
                    getString(R.string.error_loading_file),
                    Toast.LENGTH_SHORT
                ).show()
            }
            Log.d(TAG, "Can't read data file.")
        }
        Log.d(TAG, "Application data loaded.")
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

    private fun getDeviceId() {
        try {
            val fis = openFileInput(FILENAME_DEVICE_ID)
            val scanner = Scanner(fis)
            val sb = StringBuilder()
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine())
            }
            deviceId = UUID.fromString(Gson().fromJson(sb.toString(), String::class.java))
            firstTime = false
            return
        } catch (e: FileNotFoundException) {
            firstTime = true
            deviceId = UUID.randomUUID()
            Log.d(TAG, "First time opening app.")
        }

        val json = Gson().toJson(deviceId)
        var fos: FileOutputStream? = null
        try {
            fos = openFileOutput(FILENAME_DEVICE_ID, MODE_PRIVATE)
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
