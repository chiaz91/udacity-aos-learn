package com.udacity.project4.base

import android.content.pm.PackageManager

abstract class BasePermissionFragment: BaseFragment() {



    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // checking if all permissions granted
        var allGranted = true
        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED){
                allGranted = false
                return@forEach
            }
        }

        if (grantResults.isNotEmpty() && allGranted){
            onPermissionsGranted(requestCode, listOf(*permissions))
        } else {
            onPermissionsDenied(requestCode, listOf(*permissions))
        }
    }


    // true if any permission is required to show rationale
    fun shouldShowRequestPermissionsRationale(permissions: List<String>): Boolean{
        var shouldShow = false
        permissions.forEach {
            if (shouldShowRequestPermissionRationale(it)){
                shouldShow = true
                return@forEach
            }
        }
        return shouldShow
    }

    abstract fun onPermissionsGranted(requestCode: Int, perms: List<String>)
    abstract fun onPermissionsDenied(requestCode: Int, perms: List<String>)
}