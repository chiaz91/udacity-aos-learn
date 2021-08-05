package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


const val NOTIFICATION_ID = 1001
class MainActivity : AppCompatActivity() {
    companion object {
        private const val URL1 = "https://github.com/bumptech/glide/archive/master.zip"
        private const val URL2 = "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL3 = "https://github.com/square/retrofit/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }
    private var downloadID: Long = 0
    private val notificationManager: NotificationManager by lazy {
        getSystemService(NotificationManager::class.java) as NotificationManager
    }
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            when(custom_button.loading){
                false -> download()
                true  -> queryDownloadStatus(downloadID) // for debug
            }
        }
        // maybe bind with Binding adapter better?
        rb_glide.tag = URL1
        rb_loadapp.tag = URL2
        rb_loadapp.tag = URL3
    }

    override fun onRestart() {
        super.onRestart()
        custom_button.loading = false
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.extras?.log()
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?:return

            if (id == downloadID){
                // show notification for complete
                // intent only tells task ended, but it may not be success
                // can be failed, paused or cancelled
                queryDownloadStatus(downloadID)
                notificationManager.run{
                    createChannel(CHANNEL_ID, getString(R.string.notification_name_downloads))
                    notifyDownloadComplete()
                }
                custom_button.loading = false
            }

        }
    }

    private fun download() {
        val radioOption = findViewById<RadioButton>(rg_downloads.checkedRadioButtonId)
        if (radioOption == null) {
            toast(getString(R.string.guide_empty_url))
            return
        }
        val request = DownloadManager.Request(Uri.parse((radioOption.tag as String)))
                .setTitle(radioOption.text)
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
//                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
//                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        custom_button.loading=true
    }


    // notification
    fun NotificationManager.createChannel(id:String, name:String){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true)
                enableVibration(true)
                description = "General channel"
                lightColor = Color.GREEN
            }
            createNotificationChannel(notificationChannel)
        }
    }

    fun NotificationManager.notifyDownloadComplete(){
        val contentIntent = Intent(applicationContext, DetailActivity::class.java)
            .putExtra("download_id", downloadID)
        pendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID).apply {
            priority = NotificationCompat.PRIORITY_HIGH
            setSmallIcon(R.drawable.ic_cloud_download)
            setContentTitle(applicationContext.getString(R.string.notification_title))
            setContentText(applicationContext.getString(R.string.notification_description))
            setContentIntent(pendingIntent)
            setAutoCancel(true)
            addAction(
                R.drawable.ic_folder,
                applicationContext.getString(R.string.notification_button),
                pendingIntent
            )
        }
        notify(NOTIFICATION_ID, builder.build())
    }


    // option menu
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when(item.itemId){
//            R.id.action_view_downloads -> promptViewDownload()
//        }
//        return super.onOptionsItemSelected(item)
//    }
}
