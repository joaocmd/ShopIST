package pt.ulisboa.tecnico.cmov.shopist.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonParser
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.*
import pt.ulisboa.tecnico.cmov.shopist.domain.beacon.BeaconEventDto
import pt.ulisboa.tecnico.cmov.shopist.domain.beacon.RequestEstimateDto
import pt.ulisboa.tecnico.cmov.shopist.domain.prices.AddPriceDto
import pt.ulisboa.tecnico.cmov.shopist.domain.prices.PriceLocationDto
import pt.ulisboa.tecnico.cmov.shopist.domain.prices.RequestPricesByLocationDto
import pt.ulisboa.tecnico.cmov.shopist.domain.prices.RequestPricesByProductDto
import pt.ulisboa.tecnico.cmov.shopist.domain.ratings.GetProductRatingDto
import pt.ulisboa.tecnico.cmov.shopist.domain.ratings.GetProductRatingResponseDto
import pt.ulisboa.tecnico.cmov.shopist.domain.ratings.SubmitProductRatingDto
import pt.ulisboa.tecnico.cmov.shopist.domain.sorting.SubmitOrderDto
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*
import javax.net.ssl.SSLHandshakeException


class API constructor(context: Context) {
    private val queue: RequestQueue = Volley.newRequestQueue(context.applicationContext)
    private val baseURL = context.resources.getString(R.string.api_base_url)
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

    private fun getUpdateDto(received: String): PantryUpdateDto {
        return Gson().fromJson(received, PantryUpdateDto::class.java)
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

                // Verify if error if from SSL
                if (it is NoConnectionError && it.cause is SSLHandshakeException) {
                    Toast.makeText(
                        globalData,
                        globalData.resources.getString(R.string.server_security_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
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

    fun getProduct(
        productId: UUID,
        onSuccessListener: (response: ProductDto) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        val url = "$baseURL/products/$productId"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                setConnection(null)
                val receivedDto = Gson().fromJson(response, ProductDto::class.java)
                onSuccessListener(receivedDto)
            },
            {
                setConnection(it)
                onErrorListener(it)
            })

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
        onSuccessListener: (response: PantryUpdateDto) -> Unit,
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
        val sentDto = PantryUpdateDto(pantry)

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

    fun deletePantry(
        pantry: PantryList,
        onSuccessListener: () -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        val url = "$baseURL/pantries/${pantry.uuid}"

        val stringRequest = StringRequest(
            Request.Method.DELETE, url,
            {
                setConnection(null)
                onSuccessListener()
            },
            {
                setConnection(it)
                onErrorListener(it)
            })

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
        val url = "$baseURL/driving?orig=${orig.toApiString()}&dest=${dest.toApiString()}"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val duration = JsonParser.parseString(response).asJsonObject
                        .get("duration").asLong
                    onSuccessListener(duration)
                } catch (e: Exception) {
                    // ...
                }
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

    @Suppress("UNCHECKED_CAST") // Map
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

    @Suppress("UNCHECKED_CAST") // Map
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
                val result = Gson().fromJson(response, Array<PriceLocationDto>::class.java).asList()
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
        id: UUID,
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
                params["id"] = id.toString()
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
                val result = Gson().fromJson(response, Array<String>::class.java).toList()
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

        val sentDto = SubmitOrderDto(storeLocation, order)

        val url = "$baseURL/ordering/submit/"
        val stringRequest = object : StringRequest(
            Method.POST, url,
            { setConnection(null) },
            { setConnection(it) }
        ){
            override fun getBody(): ByteArray {
                super.getBody()
                return Gson().toJson(sentDto).toByteArray()
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        queue.add(setRetryPolicy(stringRequest))
    }

    fun getProductOrder(
        storeLocation: LatLng,
        products: List<Product>,
        onSuccessListener: (response: List<String>) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        val sentDto = SubmitOrderDto(storeLocation, products.mapNotNull { it.barcode })

        val url = "$baseURL/ordering/"
        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                val result = Gson().fromJson(response, Array<String>::class.java).toList()
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
                return Gson().toJson(sentDto).toByteArray()
            }

                override fun getBodyContentType(): String {
                    return "application/json"
                }
        }

        queue.add(setRetryPolicy(stringRequest))
    }

    //-----
    fun translate(
        text: String,
        sourceLang: String,
        targetLang: String,
        onSuccessListener: (response: String) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        val url = "$baseURL/translation?q=$text&source=$sourceLang&target=$targetLang"
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                setConnection(null)
                onSuccessListener(response)
            },
            {
                setConnection(it)
                onErrorListener(it)
            }
        )

        queue.add(setRetryPolicy(stringRequest))
    }

    fun getProductRating(
        barcode: String,
        deviceId: UUID,
        onSuccessListener: (rating: HashMap<Int, Int>, personalRating: Int?) -> Unit) {
        val sentDto = GetProductRatingDto(barcode, deviceId)

        val url = "$baseURL/ratings/"
        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                val result = Gson().fromJson(response, GetProductRatingResponseDto::class.java)
                setConnection(null)
                onSuccessListener(result.ratings, result.personalRating)
            },
            { setConnection(it) }
        ) {
            override fun getBody(): ByteArray {
                super.getBody()
                return Gson().toJson(sentDto).toByteArray()
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        queue.add(setRetryPolicy(stringRequest))
    }

    fun submitProductRating(barcode: String, deviceId: UUID, rating: Int?, onSuccessListener: () -> Unit) {
        val sentDto = SubmitProductRatingDto(barcode, deviceId, rating)

        val url = "$baseURL/ratings/submit/"
        val stringRequest = object : StringRequest(
            Method.POST, url,
            {
                setConnection(null)
                onSuccessListener()
            },
            { setConnection(it) }
        ) {
            override fun getBody(): ByteArray {
                super.getBody()
                return Gson().toJson(sentDto).toByteArray()
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        queue.add(setRetryPolicy(stringRequest))
    }

}