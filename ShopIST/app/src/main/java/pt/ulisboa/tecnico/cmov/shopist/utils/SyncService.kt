package pt.ulisboa.tecnico.cmov.shopist.utils

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.neovisionaries.ws.client.*
import pt.ulisboa.tecnico.cmov.shopist.R
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class SyncService : Service() {

    private val scheduleTaskExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(5)
    private lateinit var tLogic: Runnable
    private lateinit var t: Thread

    private val SYNC_INTERVAL = 10L // seconds
    private val TIMEOUT = 5000 // milliseconds
    private val RETRY_INTERVAL = 5000 // milliseconds

    override fun onCreate() {


        tLogic = Runnable {
            //Schedule a task to run every 5 seconds (or however long you want)
            scheduleTaskExecutor.scheduleAtFixedRate({
                /* shopIst.getAllLists().forEach {
                    if (it.location != null && shopIst.currentLocation != null) {
                        API.getInstance(applicationContext).getRouteTime(
                            shopIst.currentLocation!!,
                            it.location!!,
                            { time ->
                                it.drivingTime = time
                                if (shopIst.callbackDataSetChanged !== null) {
                                    shopIst.callbackDataSetChanged!!()
                                }
                            },
                            {
                                // FIXME: handle gracefully
                            }
                        )
                    }
                }*/

                /* API.getInstance(applicationContext).beaconEstimates(shopIst.stores.toList(), {
                    it.forEach { s ->
                        val store = shopIst.getStore(UUID.fromString(s.key))
                        store.queueTime = (s.value / 1000).toLong() // comes in milliseconds

                        if (shopIst.callbackDataSetChanged !== null) {
                            shopIst.callbackDataSetChanged!!()
                        }
                    }
                }, {
                    // FIXME: handle gracefully
                }) */
            }, 0, SYNC_INTERVAL, TimeUnit.SECONDS) // or .MINUTES, .HOURS etc.
        }

        t = Thread(tLogic, "SyncThread")
        // t.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        scheduleTaskExecutor.shutdownNow()
        t.interrupt()
        t.join()
    }

    override fun onBind(intent: Intent?): IBinder? {
        throw NotImplementedError()
    }

}