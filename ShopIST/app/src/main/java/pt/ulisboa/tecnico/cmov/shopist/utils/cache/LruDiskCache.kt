package pt.ulisboa.tecnico.cmov.shopist.utils.cache

import android.content.Context.MODE_PRIVATE
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import com.android.volley.VolleyError
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.utils.API
import java.io.*
import java.util.*
import kotlin.collections.LinkedHashMap

class LruDiskCache(maxSize: Int, val shopIST: ShopIST) : LruCache<UUID, CacheItem>(maxSize) {
    
    companion object {
        private const val TAG = "${ShopIST.TAG}.LruDiskCache"
    }

    /**
     * Returns the size of the file identified by key in KibiBytes
     */
    override fun sizeOf(key: UUID, value: CacheItem): Int {
        return value.size()
    }

    override fun entryRemoved(
        evicted: Boolean,
        key: UUID,
        oldValue: CacheItem,
        newValue: CacheItem?
    ) {
        oldValue.delete()
    }

    fun putImage(key: UUID, bitmap: Bitmap, local: Boolean)  {
        val file = if (local) {
            File(shopIST.getLocalImageFolder().absolutePath, "$key${ShopIST.IMAGE_EXTENSION}")
        } else {
            File(shopIST.getImageFolder().absolutePath, "$key${ShopIST.IMAGE_EXTENSION}")
        }

        FileOutputStream(file).use {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                this.put(key, CacheItem(file, local))
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
            }
        }
    }

    fun getAsImage(
		key: UUID,
		onSuccessListener: (image: Bitmap?) -> Unit,
        onErrorListener: (error: VolleyError) -> Unit
	) {
        val inCache = this.get(key)
        if (inCache != null) {
            onSuccessListener(BitmapFactory.decodeFile(inCache.file.absolutePath))
			return
        }

        // FIXME: these probably aren't needed because if they are present they are added on bootstrap
        var imagePath = File(shopIST.getLocalImageFolder().absolutePath, "$key${ShopIST.IMAGE_EXTENSION}")
        if (imagePath.exists()) {
            val imageBitmap = BitmapFactory.decodeFile(imagePath.absolutePath)
            this.put(key, CacheItem(imagePath, true))
            onSuccessListener(imageBitmap)
            return
        }

        imagePath = File(shopIST.getImageFolder().absolutePath, "$key${ShopIST.IMAGE_EXTENSION}")
        if (imagePath.exists()) {
            val imageBitmap = BitmapFactory.decodeFile(imagePath.absolutePath)
            this.put(key, CacheItem(imagePath, true))
            onSuccessListener(imageBitmap)
            return
        }

		// request from server
        API.getInstance(shopIST).getProductImage(key,
            { bitmap ->
                putImage(key, bitmap, false)
                onSuccessListener(bitmap)
            },
            { 	// If can't find on server and doesn't have locally
                onErrorListener(it)
            }
        )
    }

    fun saveSnapShot(context: ContextWrapper) {
        context.openFileOutput("cache", MODE_PRIVATE).use { fos -> ObjectOutputStream(fos).use {
            it.writeObject(this.snapshot())
        } }
    }

    @Suppress("UNCHECKED_CAST")
    fun bootstrapCache(context: ContextWrapper) {
        try {
            context.openFileInput("cache").use { fis ->
                ObjectInputStream(fis).use {
                    val map = it.readObject() as LinkedHashMap<UUID, CacheItem>
                    // start putting from oldest to newest
                    map.entries.reversed().forEach { (uuid, item) ->
                        if (item.file.exists()) {
                            this.put(uuid, item)
                        }
                    }
                }
            }
        } catch (ignored: FileNotFoundException) { /* ignored */ }
    }
}
