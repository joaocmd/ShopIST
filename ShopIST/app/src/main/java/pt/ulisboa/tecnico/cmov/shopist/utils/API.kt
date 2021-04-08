package pt.ulisboa.tecnico.cmov.shopist.utils

import android.content.Context
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.BigBoyDto
import pt.ulisboa.tecnico.cmov.shopist.domain.PantryList
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


class API constructor(context: Context) {
    private val queue: RequestQueue = Volley.newRequestQueue(context.applicationContext)
    private val baseURL = context.resources.getString(R.string.api_base_url)

    companion object {
        @Volatile
        private var INSTANCE: API? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: API(context).also {
                    INSTANCE = it
                }
            }
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
                // Log.d(ShopIST.TAG, receivedDto.toString())
            },
            {
                onErrorListener(it)
                // when (it.networkResponse.statusCode) {
                //     404 -> {
                //         Log.d(ShopIST.TAG, "Pantry not found")
                //     }
                // }
                // Log.d(ShopIST.TAG, it.toString())
            })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
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
                Log.d(ShopIST.TAG, response)
            },
            {
                onErrorListener(it)
                // when (it.networkResponse.statusCode) {
                //     400 -> {
                //         Log.d(ShopIST.TAG, "Error sending pantry")
                //     }
                // }
                Log.d(ShopIST.TAG, it.toString())
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