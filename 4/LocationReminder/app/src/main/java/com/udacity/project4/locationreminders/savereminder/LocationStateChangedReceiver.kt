package com.udacity.project4.locationreminders.savereminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.util.Log

class LocationStateChangedReceiver(val callback: (Boolean) -> Unit ): BroadcastReceiver() {
    companion object{
        const val TAG = "cy.rcv.location"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (LocationManager.PROVIDERS_CHANGED_ACTION == intent.action){
            val locationMgr      = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled     = locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            Log.i(TAG, "GPS = $isGpsEnabled, Network = $isNetworkEnabled")
            if (isGpsEnabled || isNetworkEnabled) {
                Log.i(TAG, "location changed: location is enabled")
                callback(true)

            } else {
                Log.i(TAG, "location disabled ")
                callback(false)
            }
        }
    }
}
