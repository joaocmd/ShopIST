package pt.ulisboa.tecnico.cmov.shopist.utils.cache

import java.io.File

class CacheItem(val file: File, val local: Boolean) {
    fun size(): Int {
        if (local) {
            // users' local files really don't matter for cache space
            return 0
        } else {
            return (file.length()/1024).toInt()
        }
    }

    fun delete() {
        if (!local) {
            file.delete()
        }
    }
}