package com.udacity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        intent?.extras?.log()
        val id =intent.getLongExtra("download_id", -1)

        queryDownloadStatus(id)?.apply {
            file_name.text = this.title
            status.text    = getStatusString(this.statusCode)
        } ?: showError("File not found")

        btn_ok.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun showError(errMsg: String){
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(errMsg)
            .setNegativeButton("Cancel" ){ _,_ ->
                finish()
            }
            .setCancelable(false)
            .create()
            .show()
    }

}
