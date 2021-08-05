package com.udacity;

import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


fun Activity.toast(message: String): Toast {
    val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
    toast.show()
    return toast
}

fun Context.queryDownloadStatus(id: Long): FileStatus?{
    try{
        val query = DownloadManager.Query()
        query.setFilterById(id)
        val manager = getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
        manager.query(query).run {
            if (moveToFirst()){
                log()
                val title = getString(getColumnIndex(DownloadManager.COLUMN_TITLE))
                val status = getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))
                val reason = getInt(getColumnIndex(DownloadManager.COLUMN_REASON))
                return FileStatus(title, status, reason)
            }
        }
    } catch (e: Exception){
        e.printStackTrace()
    }
    return null
}

// debug
fun LoadingButton.simulateLoading(){
    loading = true

    Handler(Looper.getMainLooper()).postDelayed({
        loading = false
    }, 10000)
}

fun Bundle.log() {
    Log.i("load.bundle", "{")
    keySet().forEach{key ->
        Log.i("load.bundle",  " $key: ${this[key].toString()}" )
    }
    Log.i("load.bundle", "}")
}

fun Cursor.log(){
    Log.i("load.cursor", "{")
    columnNames.forEach {col->
        try{
            val idx = getColumnIndex(col)
            Log.i("load.cursor", " $idx $col: ${getString(idx)}")
        } catch (e: Exception){}
    }
    Log.i("load.cursor", "}")
}

fun Activity.toViewDownload(){
    val viewDownloads = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
    startActivity(viewDownloads)
}

fun Activity.promptViewDownload(){
    val inputId = EditText(this)
    inputId.hint = "Enter download id"
    inputId.inputType = InputType.TYPE_CLASS_NUMBER

    AlertDialog.Builder(this)
        .setTitle("DEBUG")
        .setMessage("View download status on DetailActivity.")
        .setView(inputId)
        .setPositiveButton("View"){ _,_ ->
            try{
                var id = inputId.text.toString().toLong()

                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("download_id", id)
                startActivity(intent)
            }catch (e:Exception){
                toViewDownload( )
            }
        }
        .show()
}


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
