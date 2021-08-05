package com.udacity;

import android.app.DownloadManager

// dirty methods :P
fun getStatusString(status: Int): String{
    return when(status){
        DownloadManager.STATUS_PENDING -> "Pending"
        DownloadManager.STATUS_RUNNING -> "Running"
        DownloadManager.STATUS_PAUSED -> "Paused"
        DownloadManager.STATUS_SUCCESSFUL -> "Success"
        DownloadManager.STATUS_FAILED -> "Failed"
        else -> "Unknown status"
    }
}
fun getReasonString(reason: Int): String{
    return when(reason){
        // pauses
        DownloadManager.PAUSED_WAITING_TO_RETRY -> "PAUSED_WAITING_TO_RETRY"
        DownloadManager.PAUSED_WAITING_FOR_NETWORK -> "PAUSED_WAITING_FOR_NETWORK"
        DownloadManager.PAUSED_QUEUED_FOR_WIFI -> "PAUSED_QUEUED_FOR_WIFI"
        DownloadManager.PAUSED_UNKNOWN -> "PAUSED_UNKNOWN"
        // error
        DownloadManager.ERROR_UNKNOWN -> "ERROR_UNKNOWN"
        DownloadManager.ERROR_FILE_ERROR -> "ERROR_FILE_ERROR"
        DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "ERROR_UNHANDLED_HTTP_CODE"
        DownloadManager.ERROR_HTTP_DATA_ERROR -> "ERROR_HTTP_DATA_ERROR"
        DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "ERROR_TOO_MANY_REDIRECTS"
        DownloadManager.ERROR_INSUFFICIENT_SPACE-> "ERROR_INSUFFICIENT_SPACE"
        DownloadManager.ERROR_DEVICE_NOT_FOUND -> "ERROR_DEVICE_NOT_FOUND"
        DownloadManager.ERROR_CANNOT_RESUME -> "ERROR_CANNOT_RESUME"
        DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "ERROR_FILE_ALREADY_EXISTS"
        else -> "Unknown reason"
    }
}