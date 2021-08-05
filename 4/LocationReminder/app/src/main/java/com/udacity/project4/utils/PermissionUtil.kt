package com.udacity.project4.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.udacity.project4.REQ_PERMISSION_FOREGROUND_BACKGROUND_LOCATION
import com.udacity.project4.REQ_PERMISSION_FOREGROUND_LOCATION


private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q


fun Context.hasPermissions(permissions: Array<String>): Boolean{
    var allGranted = true

    permissions.forEach {
        if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED ){
            allGranted = false
            return@forEach
        }
    }
    return allGranted
}

fun Context.toSettingPage(){
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.fromParts("package", packageName, null)
    startActivity(intent)
}


fun Context.showPermissionDeniedDialog(rationale: String): Dialog{
    val dialog = AlertDialog.Builder(this)
        .setTitle("Permission Denied")
        .setMessage(rationale)
        .setPositiveButton("Setting"){ _,_ ->
            toSettingPage()
        }
        .setNegativeButton("Cancel", null)
        .create()
    dialog.show()
    return dialog
}

fun Fragment.hasForegroundLocationPermission(): Boolean{
    val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    return requireContext().hasPermissions(permissions)
}

@TargetApi(29)
fun Fragment.hasForegroundBackgroundLocationPermission(): Boolean{
    var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    if (runningQOrLater){
        permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
    }
    return requireContext().hasPermissions(permissions)
}

fun Fragment.requestForegroundLocationPermissions(){
    if (hasForegroundLocationPermission())
        return
    val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    val requestCode = REQ_PERMISSION_FOREGROUND_LOCATION
    requestPermissions(permissions, requestCode)
}

@TargetApi(29)
fun Fragment.requestForegroundBackgroundLocationPermissions(){
    if (hasForegroundLocationPermission())
        return
    var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    val resultCode = when {
        runningQOrLater -> {
            permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            REQ_PERMISSION_FOREGROUND_BACKGROUND_LOCATION
        }
        else -> REQ_PERMISSION_FOREGROUND_LOCATION
    }
    requestPermissions(permissions, resultCode)
}