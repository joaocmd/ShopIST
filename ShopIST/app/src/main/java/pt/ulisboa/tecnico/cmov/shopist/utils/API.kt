package pt.ulisboa.tecnico.cmov.shopist.utils

import android.content.Context
import android.util.Log
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
import pt.ulisboa.tecnico.cmov.shopist.domain.prices.RequestPricesListDto
import java.util.*


class API constructor(context: Context) {
    private val queue: RequestQueue = Volley.newRequestQueue(context.applicationContext)
    private val baseURL = context.resources.getString(R.string.api_base_url)
    private val directionsURL = context.resources.getString(R.string.directions_api_url)
    private val bingKey = context.resources.getString(R.string.bing_maps_key)

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
    }

    fun getUpdateDto(received: String): BigBoyDto {
        return Gson().fromJson(received, BigBoyDto::class.java)
    }

    fun postProduct(product: Product,
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
                onSuccessListener(response)
            },
            {
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
        queue.add(stringRequest)
    }

    fun postStore(store: Store,
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
                onSuccessListener(response)
            },
            {
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
        queue.add(stringRequest)
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
                val receivedDto = Gson().fromJson(response, BigBoyDto::class.java)

                onSuccessListener(receivedDto)
            },
            {
                onErrorListener(it)
            })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
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
                onSuccessListener(response)
            },
            {
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
        queue.add(stringRequest)
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
        queue.add(stringRequest)
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
                onSuccessListener(response)
                Log.d(TAG, response)
            },
            {
                onErrorListener(it)
                Log.d(TAG, it.toString())
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
        queue.add(stringRequest)
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
                onSuccessListener(response)
                Log.d(TAG, response)
            },
            {
                onErrorListener(it)
                Log.d(TAG, it.toString())
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
        queue.add(stringRequest)

    }

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
                val result = Gson().fromJson(response, Map::class.java) as Map<String, Double>
                onSuccessListener(result)
            },
            {
                onErrorListener(it)
                Log.d(TAG, it.toString())
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
        queue.add(stringRequest)

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
                onSuccessListener(response)
                Log.d(TAG, response)
            },
            {
                onErrorListener(it)
                Log.d(TAG, it.toString())
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
        queue.add(stringRequest)
    }

    fun getListPrices(
        products: List<Product>,
        location: LatLng,
        onSuccessListener: (response: Map<String, Double>) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
    ) {
        val url = "$baseURL/prices/get/"

        val productBarcodes = products.mapNotNull { it.barcode }
        val sentDto = RequestPricesListDto(productBarcodes, location)

        // Request a string response from the provided URL.
        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                val result = Gson().fromJson(response, Map::class.java) as Map<String, Double>
                onSuccessListener(result)
            },
            {
                onErrorListener(it)
                Log.d(TAG, it.toString())
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
        queue.add(stringRequest)

    }
}