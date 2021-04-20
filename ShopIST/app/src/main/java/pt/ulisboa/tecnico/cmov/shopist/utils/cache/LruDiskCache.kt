package pt.ulisboa.tecnico.cmov.shopist.utils.cache

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import com.android.volley.VolleyError
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import pt.ulisboa.tecnico.cmov.shopist.utils.API
import java.io.File
import java.io.FileOutputStream
import java.util.*

class LruDiskCache(maxSize: Int, val shopIST: ShopIST) : LruCache<UUID, CacheItem>(maxSize) {
    
    companion object {
        private val TAG = LruDiskCache::class.qualifiedName
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
        val file = File(shopIST.getImageFolder().absolutePath, "$key${ShopIST.IMAGE_EXTENSION}")
        FileOutputStream(file).use {
            try {
                val fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
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

		val imagePath = File(shopIST.getLocalImageFolder().absolutePath, "$key${ShopIST.IMAGE_EXTENSION}")
        if (imagePath.exists()) {
            val imageBitmap = BitmapFactory.decodeFile(imagePath.absolutePath)
			putImage(key, imageBitmap, true)
            onSuccessListener(imageBitmap)
            return
        }

		// request from server
        API.getInstance(shopIST).getProductImage(key,
            {
                val bitmap = BitmapFactory.decodeByteArray(it.toByteArray(), 0, it.length)
                putImage(key, bitmap, false)
                onSuccessListener(bitmap)
            },
            { 	// If can't find on server and doesn't have locally
				onErrorListener(it)
			}
        )
  }
}