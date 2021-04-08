package pt.ulisboa.tecnico.cmov.shopist.utils

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class SyncService : Service() {
    private val scheduleTaskExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(5)
    private lateinit var tLogic: Runnable
    private lateinit var t: Thread

    private val SYNC_INTERVAL = 10L // seconds

    override fun onCreate() {
        tLogic = Runnable {
            //Schedule a task to run every 5 seconds (or however long you want)
            scheduleTaskExecutor.scheduleAtFixedRate({
                Log.d(ShopIST.TAG, "Synchronize!")
                val shopIst = (applicationContext as ShopIST)
                shopIst.pantries.forEach {
                    if (it.isShared) {
                        // Check for updates
                        // TODO: Maybe we can know the version of the pantry list and only update it if there is a new version
                        API.getInstance(applicationContext).getPantry(it.uuid, { result ->
                            shopIst.populateFromServer(result)
                            if (shopIst.callbackDataSetChanged !== null) {
                                shopIst.callbackDataSetChanged!!()
                            }
                        }, {
                            // FIXME: wut
                        })
                    }
                }
            }, 0, SYNC_INTERVAL, TimeUnit.SECONDS) // or .MINUTES, .HOURS etc.
        }

        t = Thread(tLogic, "SyncThread")
        t.start()
    }

    override fun onBind(intent: Intent?): IBinder? {
        throw NotImplementedError()
    }

    override fun onDestroy() {
        super.onDestroy()
        scheduleTaskExecutor.shutdownNow()
        t.interrupt()
        t.join()
    }

}