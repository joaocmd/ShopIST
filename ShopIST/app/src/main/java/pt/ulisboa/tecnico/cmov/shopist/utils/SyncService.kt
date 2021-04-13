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
    private var reconnectScheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private lateinit var ws: WebSocket
    private var isConnected = false
    private val binder = LocalBinder()

    private val scheduleTaskExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(5)
    private lateinit var tLogic: Runnable
    private lateinit var t: Thread


    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): SyncService = this@SyncService
    }

    private val SYNC_INTERVAL = 10L // seconds
    private val TIMEOUT = 5000 // milliseconds
    private val RETRY_INTERVAL = 5000 // milliseconds

    override fun onCreate() {

        val shopIst = (applicationContext as ShopIST)
        shopIst.pantries.forEach {
            if (it.isShared) {
                // Check for updates
                API.getInstance(applicationContext).getPantry(it.uuid, { result ->
                    shopIst.populateFromServer(result)
                    if (shopIst.callbackDataSetChanged !== null) {
                        shopIst.callbackDataSetChanged!!()
                    }
                }, {
                    // FIXME: handle gracefully
                })
            }
        }

        tLogic = Runnable {
            //Schedule a task to run every 5 seconds (or however long you want)
            scheduleTaskExecutor.scheduleAtFixedRate({
                shopIst.getAllLists().forEach {
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
                }

                API.getInstance(applicationContext).beaconEstimates(shopIst.stores.toList(), {
                    it.forEach { s ->
                        val store = shopIst.getStore(UUID.fromString(s.key))
                        store.queueTime = (s.value / 1000).toLong() // comes in milliseconds

                        if (shopIst.callbackDataSetChanged !== null) {
                            shopIst.callbackDataSetChanged!!()
                        }
                    }
                }, {
                    // FIXME: handle gracefully
                })
            }, 0, SYNC_INTERVAL, TimeUnit.SECONDS) // or .MINUTES, .HOURS etc.
        }

        t = Thread(tLogic, "SyncThread")
        t.start()

        /* ws = WebSocketFactory().createSocket(
            applicationContext.resources.getString(R.string.ws_url),
            TIMEOUT
        )

        // Register a listener to receive WebSocket events.
        ws.addListener(object : WebSocketAdapter() {
            override fun onConnectError(websocket: WebSocket?, exception: WebSocketException?) {
                super.onConnectError(websocket, exception)
                tryReconnect()
            }

            override fun onConnected(ws: WebSocket, headers: Map<String, List<String>>) {
                Log.d(ShopIST.TAG, "Connected")
                isConnected = true
            }

            override fun onTextMessage(ws: WebSocket, text: String) {
                Log.d(ShopIST.TAG, "Message received: $text")
                val dto = API.getInstance(applicationContext).getUpdateDto(text)
                val globalData = (applicationContext as ShopIST)
                globalData.populateFromServer(dto)
            }

            override fun onDisconnected(
                websocket: WebSocket?,
                serverCloseFrame: WebSocketFrame?,
                clientCloseFrame: WebSocketFrame?,
                closedByServer: Boolean
            ) {
                super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer)
                isConnected = false
                Log.d(ShopIST.TAG, "Disconnected!")
                tryReconnect()
            }
        })

        ws.connectAsynchronously() */
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun tryReconnect() {
        reconnectScheduler.shutdown()
        reconnectScheduler = Executors.newScheduledThreadPool(1)

        val connectRunnable = Runnable {
            try {
                Log.d(ShopIST.TAG, "Reconnecting...")
                ws = ws.recreate().connect()
                isConnected = true
                reconnectScheduler.shutdown()
            } catch (e: WebSocketException) {
                Log.d(ShopIST.TAG, "Can't connect to server")
            }
        }

        reconnectScheduler.scheduleWithFixedDelay(
            connectRunnable,
            0L,
            RETRY_INTERVAL.toLong(),
            TimeUnit.MILLISECONDS
        );
    }

    override fun onDestroy() {
        super.onDestroy()
        // ws.disconnect()
        scheduleTaskExecutor.shutdownNow()
        t.interrupt()
        t.join()
    }

}