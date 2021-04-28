package pt.ulisboa.tecnico.cmov.shopist.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast
import pt.ulisboa.tecnico.cmov.shopist.SideMenuNavigation
import pt.ulisboa.tecnico.cmov.shopist.domain.ShopIST

class QueueBroadcastReceiver(activity: SideMenuNavigation) : BroadcastReceiver() {

    companion object {
        const val TAG = "${ShopIST.TAG}.queueBroadcastReceiver"
    }

    private val mActivity: SideMenuNavigation = activity

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION == action) {
            val state = intent.getIntExtra(SimWifiP2pBroadcast.EXTRA_WIFI_STATE, -1)
            if (state == SimWifiP2pBroadcast.WIFI_P2P_STATE_ENABLED) {
                Log.d(TAG, "WiFi Direct enabled")
            } else {
                Log.d(TAG, "WiFi Direct disabled")
            }
        } else if (SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION == action) {
            Log.d(TAG, "Peer list changed")
            mActivity.getPeers()
        }
    }
}