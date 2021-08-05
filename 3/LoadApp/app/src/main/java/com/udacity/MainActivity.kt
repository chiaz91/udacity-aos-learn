package com.udacity

import android.app.AlertDialog
import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
//            download()
            simulateLoading()
        }
    }

    fun showDialog(){
        AlertDialog.Builder(this).let{
            it.setTitle("download succ")
            it.setMessage("view downloads")
            it.setPositiveButton("view") { _, _ ->
                val viewDownloads = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                startActivity(viewDownloads)
            }
        }.show()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                val query = DownloadManager.Query()
                query.setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
                val manager = context!!.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val cursor: Cursor = manager.query(query)
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    val reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
                    Log.i("load.rec", "status $status, reason $reason")
                    Log.i("load.rec", "status ${getStatusString(status)}, reason ${getReasonString(reason)}")
                    if (status == DownloadManager.STATUS_SUCCESSFUL){
                        showDialog()
                    }
                } else{
                    Log.i("load.rec", "Failed to moveToFirst")
                }
            } else {
                Log.i("load.rec", "unrelated downloadId $id")
            }
        }
    }





    private fun download() {
        val request = DownloadManager.Request(Uri.parse(URL))
//                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
//                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }

    // test animation
    fun simulateLoading(){
        custom_button.setLoading(true)

        Handler(Looper.getMainLooper()).postDelayed({
            custom_button.setLoading(false)
        }, 3000)
    }

}
