package com.udacity.project4.locationreminders.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem


const val TAG = "cy.gf.utils"
const val GEOFENCE_RADIUS_IN_METERS = 100f


//GeofencingClient
fun createGeofenceRequest(reqId: String, lat: Double, lng: Double): GeofencingRequest {
    val geofence = Geofence.Builder()
        .setRequestId(reqId)
        .setCircularRegion(lat, lng, GEOFENCE_RADIUS_IN_METERS)
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
        .build()
    return GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        .addGeofence(geofence)
        .build()
}

fun createGeofencePendingIntent(context: Context): PendingIntent{
    val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
    intent.action = GeofenceBroadcastReceiver.ACTION_GEOFENCE_EVENT
    val geofencePendingIntent = PendingIntent.getBroadcast(
        context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
    )
    return geofencePendingIntent
}

@SuppressLint("MissingPermission")
fun GeofencingClient.addGeofenceForReminder(reminder: ReminderDataItem){
    Log.i(TAG, "addGeofenceForReminder start")
    val request = createGeofenceRequest(reminder.id, reminder.latitude!!, reminder.longitude!!)
    val pendingIntent = createGeofencePendingIntent(this.applicationContext)
    this.addGeofences(request,pendingIntent)?.run {
        addOnSuccessListener {
            Log.i(TAG, "Successfully add reminder geofence(${reminder.id})")
        }
        addOnFailureListener{
            Log.e(TAG, "Failed to add reminder geofence(${reminder.id})")
        }
    }
}

