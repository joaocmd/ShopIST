package pt.ulisboa.tecnico.cmov.shopist.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.*
import pt.ulisboa.tecnico.cmov.shopist.domain.beacon.BeaconEventDto
import pt.ulisboa.tecnico.cmov.shopist.domain.beacon.RequestEstimateDto
import pt.ulisboa.tecnico.cmov.shopist.domain.prices.AddPriceDto
import pt.ulisboa.tecnico.cmov.shopist.domain.prices.PriceLocationDto
import pt.ulisboa.tecnico.cmov.shopist.domain.prices.RequestPricesByLocationDto
import pt.ulisboa.tecnico.cmov.shopist.domain.prices.RequestPricesByProductDto
import java.io.ByteArrayOutputStream
import java.util.*


class API constructor(context: Context) {
    private val queue: RequestQueue = Volley.newRequestQueue(context.applicationContext)
    private val baseURL = context.resources.getString(R.string.api_base_url)
    private val directionsURL = context.resources.getString(R.string.directions_api_url)
    private val bingKey = context.resources.getString(R.string.bing_maps_key)
    private val globalData = context as ShopIST

    companion object {
        @Volatile
        private var INSTANCE: API? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: API(context).also {
                    INSTANCE = it
                }
            }

        const val TAG = "${ShopIST.TAG}.api"
        const val TIMEOUT = 3000 // ms
        const val MAX_RETRIES = 1
    }

    private fun setConnection(error: VolleyError?) {
        if (error == null) {
            globalData.isAPIConnected = true
        } else globalData.isAPIConnected = error !is TimeoutError
    }

    private fun getUpdateDto(received: String): BigBoyDto {
        return Gson().fromJson(received, BigBoyDto::class.java)
    }

    private fun setRetryPolicy(request: StringRequest): StringRequest {
        request.retryPolicy = DefaultRetryPolicy(
            TIMEOUT,
            MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        return request
    }

    fun ping() {
        val url = "$baseURL/ping/"

        // Request a string response from the provided URL.
        val stringRequest = object : StringRequest(
            Method.GET, url,
            {
                setConnection(null)
            },
            {
                setConnection(it)
            }) {}

        // Add the request to the RequestQueue.
        stringRequest.retryPolicy = DefaultRetryPolicy(
            1000, // ms
            1,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(stringRequest)
    }

    fun postProduct(
        product: Product,
        onSuccessListener: (response: String) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        val url = "$baseURL/products/${product.uuid}"

        // Prepare the dto
        val sentDto = ProductDto(product)

        // Request a string response from the provided URL.
        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                setConnection(null)
                onSuccessListener(response)
            },
            {
                setConnection(it)
                onErrorListener(it)
            }) {
            override fun getBody(): ByteArray {
                super.getBody()
                return Gson().toJson(sentDto).toByteArray()
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        // Add the request to the RequestQueue.
        queue.add(setRetryPolicy(stringRequest))
    }

    fun postStore(
        store: Store,
        onSuccessListener: (response: String) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        val url = "$baseURL/stores/${store.uuid}"

        // Prepare the dto
        val sentDto = StoreDto(store)

        // Request a string response from the provided URL.
        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                setConnection(null)
                onSuccessListener(response)
            },
            {
                setConnection(it)
                onErrorListener(it)
            }) {
            override fun getBody(): ByteArray {
                super.getBody()
                return Gson().toJson(sentDto).toByteArray()
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        // Add the request to the RequestQueue.
        queue.add(setRetryPolicy(stringRequest))
    }

    fun getPantry(
        pantryId: UUID,
        onSuccessListener: (response: BigBoyDto) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        val url = "$baseURL/pantries/$pantryId"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                setConnection(null)
                val receivedDto = getUpdateDto(response)
                onSuccessListener(receivedDto)
            },
            {
                setConnection(it)
                onErrorListener(it)
            })

        // Add the request to the RequestQueue.
        queue.add(setRetryPolicy(stringRequest))
    }

    fun updatePantry(pantry: PantryList) {
        if (!pantry.isShared) {
            return
        }
        postNewPantry(pantry, {
            Log.d(ShopIST.TAG, "Pantry sent for update")
        }, {
            Log.d(ShopIST.TAG, "Could not send pantry")
        })
    }

    fun postNewPantry(
        pantry: PantryList,
        onSuccessListener: (response: String) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        val url = "$baseURL/pantries/" + pantry.uuid.toString()

        // Prepare the dto
        val sentDto = BigBoyDto(pantry)

        // Request a string response from the provided URL.
        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                setConnection(null)
                onSuccessListener(response)
            },
            {
                setConnection(it)
                onErrorListener(it)
            }) {
                override fun getBody(): ByteArray {
                    super.getBody()
                    return Gson().toJson(sentDto).toByteArray()
                }

                override fun getBodyContentType(): String {
                    return "application/json"
                }
            }

        // Add the request to the RequestQueue.
        queue.add(setRetryPolicy(stringRequest))
    }

    private fun LatLng.toApiString(): String {
        return "${this.latitude},${this.longitude}"
    }

    fun getRouteTime(
        orig: LatLng,
        dest: LatLng,
        onSuccessListener: (response: Long) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        val url = "${directionsURL}/Driving?wp.0=${orig.toApiString()}&wp.1=${dest.toApiString()}&key=$bingKey"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                val responseObj = JsonParser.parseString(response).asJsonObject
                val duration = responseObj
                    .getAsJsonArray("resourceSets")[0].asJsonObject
                    .getAsJsonArray("resources")[0].asJsonObject
                    .get("travelDuration").asLong

                onSuccessListener(duration)
            },
            {
                onErrorListener(it)
            })

        // Add the request to the RequestQueue.
        queue.add(setRetryPolicy(stringRequest))
    }

    fun beaconEnter(
        beaconName: String,
        nItems: Int,
        token: UUID,
        onSuccessListener: (response: String) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        val url = "$baseURL/beacons/enter/$beaconName"

        val sentDto = BeaconEventDto(token.toString(), nItems)

        // Request a string response from the provided URL.
        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                setConnection(null)
                onSuccessListener(response)
            },
            {
                setConnection(it)
                onErrorListener(it)
            }) {
            override fun getBody(): ByteArray {
                super.getBody()
                return Gson().toJson(sentDto).toByteArray()
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        // Add the request to the RequestQueue.
        queue.add(setRetryPolicy(stringRequest))
    }

    fun beaconLeave(
        beaconName: String,
        token: UUID,
        onSuccessListener: (response: String) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        val url = "$baseURL/beacons/leave/$beaconName"

        val sentDto = BeaconEventDto(token.toString(), null)

        // Request a string response from the provided URL.
        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                setConnection(null)
                onSuccessListener(response)
            },
            {
                setConnection(it)
                onErrorListener(it)
            }) {
            override fun getBody(): ByteArray {
                super.getBody()
                return Gson().toJson(sentDto).toByteArray()
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        // Add the request to the RequestQueue.
        queue.add(setRetryPolicy(stringRequest))

    }

    @Suppress("UNCHECKED_CAST")
    fun beaconEstimates(
        stores: List<Store>,
        onSuccessListener: (response: Map<String, Double>) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        val url = "$baseURL/beacons/estimates"

        val storesMap = stores.filter { it.location != null }.map { it.uuid.toString() to it.location!! }.toMap()
        val sentDto = RequestEstimateDto(storesMap)

        // Request a string response from the provided URL.
        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                setConnection(null)
                val result = Gson().fromJson(response, Map::class.java) as Map<String, Double>
                onSuccessListener(result)
            },
            {
                setConnection(it)
                onErrorListener(it)
            }) {
            override fun getBody(): ByteArray {
                super.getBody()
                return Gson().toJson(sentDto).toByteArray()
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        // Add the request to the RequestQueue.
        queue.add(setRetryPolicy(stringRequest))

    }

    fun submitPriceProduct(
        price: Number,
        product: Product,
        place: Locatable,
        onSuccessListener: (response: String) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        if (product.barcode === null || place.location === null) {
            return
        }

        val url = "$baseURL/prices/add/"
        val sentDto = AddPriceDto(product.barcode!!, place.location!!, price)

        // Request a string response from the provided URL.
        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                setConnection(null)
                onSuccessListener(response)
            },
            {
                setConnection(it)
                onErrorListener(it)
            }) {
            override fun getBody(): ByteArray {
                super.getBody()
                return Gson().toJson(sentDto).toByteArray()
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        // Add the request to the RequestQueue.
        queue.add(setRetryPolicy(stringRequest))
    }

    @Suppress("UNCHECKED_CAST")
    fun getPricesForStore(
        products: List<Product>,
        location: LatLng,
        onSuccessListener: (response: Map<String, Double>) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        val url = "$baseURL/prices/location/"

        val productBarcodes = products.mapNotNull { it.barcode }
        val sentDto = RequestPricesByLocationDto(productBarcodes, location)

        // Request a string response from the provided URL.
        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                setConnection(null)
                val result = Gson().fromJson(response, Map::class.java) as Map<String, Double>
                onSuccessListener(result)
            },
            {
                setConnection(it)
                onErrorListener(it)
            }) {
            override fun getBody(): ByteArray {
                super.getBody()
                return Gson().toJson(sentDto).toByteArray()
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        // Add the request to the RequestQueue.
        queue.add(setRetryPolicy(stringRequest))

    }

    @Suppress("UNCHECKED_CAST")
    fun getPricesForProduct(
        product: Product,
        location: List<LatLng>,
        onSuccessListener: (response: List<PriceLocationDto>) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        if (product.barcode === null) {
            return
        }

        val url = "$baseURL/prices/barcode/"

        val sentDto = RequestPricesByProductDto(product.barcode!!, location)

        // Request a string response from the provided URL.
        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                setConnection(null)
                val result = Gson().fromJson(response, List::class.java) as List<PriceLocationDto>
                onSuccessListener(result)
            },
            {
                setConnection(it)
                onErrorListener(it)
            }) {
            override fun getBody(): ByteArray {
                super.getBody()
                return Gson().toJson(sentDto).toByteArray()
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        // Add the request to the RequestQueue.
        queue.add(setRetryPolicy(stringRequest))

    }

    fun postProductImage(
        product: Product,
        image: Bitmap,
        onSuccessListener: (response: String) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        if (product.barcode === null) {
            return
        }
        val url = "$baseURL/images/${product.barcode!!}/add/"

        val stream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val imageBytes = stream.toByteArray()
        val imageString: String = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                setConnection(null)
                onSuccessListener(response)
            },
            {
                setConnection(it)
                onErrorListener(it)
            }) {

            //adding parameters to send
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["image"] = imageString
                return params
            }
        }

        queue.add(setRetryPolicy(stringRequest))
    }

    fun getProductImage(
        id: UUID,
        onSuccessListener: (response: Bitmap) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        val url = "$baseURL/images/$id"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                setConnection(null)
                val imageBytes = Base64.decode(response, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                onSuccessListener(bitmap)
            },
            {
                setConnection(it)
                onErrorListener(it)
            })

        queue.add(setRetryPolicy(stringRequest))

    }

    @Suppress("UNCHECKED_CAST")
    fun getProductImages(
        product: Product,
        onSuccessListener: (response: List<String>) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        if (product.barcode === null) {
            return
        }

        val url = "$baseURL/products/${product.barcode}/images/"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                setConnection(null)
                val result = Gson().fromJson(response, List::class.java) as List<String>
                onSuccessListener(result)
            },
            {
                setConnection(it)
                onErrorListener(it)
            })

        // Add the request to the RequestQueue.
        queue.add(setRetryPolicy(stringRequest))

    }

    fun submitProductOrder(storeLocation: LatLng, order: List<String>) {
        val request = JsonObject()
        request.addProperty("location", storeLocation.toApiString())
        request.add("order", JsonArray())
        order.forEach {
            request.asJsonArray.add(it)
        }

        val url = "$baseURL/ordering/submit/"
        val stringRequest = object : StringRequest(
            Method.POST, url,
            { setConnection(null) },
            { setConnection(it) }
        ){
            override fun getBody(): ByteArray {
                super.getBody()
                return Gson().toJson(request).toByteArray()
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        queue.add(setRetryPolicy(stringRequest))
    }

    @Suppress("UNCHECKED_CAST")
    fun getProductOrder(
        storeLocation: LatLng,
        products: List<Product>,
        onSuccessListener: (response: List<String>) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        val request = JsonObject()
        request.addProperty("location", storeLocation.toApiString())
        request.add("order", JsonArray())
        products.forEach {
            it.barcode?.let { barcode ->
                request.asJsonArray.add(barcode)
            }
        }

        val url = "$baseURL/ordering/"
        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                val result = Gson().fromJson(response, List::class.java) as List<String>
                setConnection(null)
                onSuccessListener(result)
            },
            {
                setConnection(it)
                onErrorListener(it)
            }
        ) {
            override fun getBody(): ByteArray {
                super.getBody()
                return Gson().toJson(request).toByteArray()
            }

                override fun getBodyContentType(): String {
                    return "application/json"
                }
        }

        queue.add(setRetryPolicy(stringRequest))
    }
}