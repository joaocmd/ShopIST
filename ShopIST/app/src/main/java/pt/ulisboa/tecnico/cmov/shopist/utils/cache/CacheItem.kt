package pt.ulisboa.tecnico.cmov.shopist.utils.cache

import java.io.File
import java.io.Serializable

class CacheItem(val file: File, val local: Boolean) : Serializable {
    fun size(): Int {
        return if (local) {
            // users' local files really don't matter for cache space
            0
        } else {
            (file.length()/1024).toInt()
        }
    }

    fun delete() {
        if (!local) {
            file.delete()
        }
    }
}