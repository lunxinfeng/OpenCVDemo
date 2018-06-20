package com.lxf.ndkdemo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_service.*

class ServiceActivity : AppCompatActivity() {
    private val game = GameInfo()
    private var mediaResult = 0
    private var mediaIntent: Intent? = null
    private val REQUEST_MEDIA_PROJECTION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)

        val items = arrayOf("19", "13", "9")
        spinnerSize.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items)
        spinnerSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when (position) {
                    0 -> game.boardSize = 19
                    1 -> game.boardSize = 13
                    2 -> game.boardSize = 9
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        rb_start.setOnCheckedChangeListener { _, isChecked -> if (isChecked) game.type = 1 }
        rb_middle.setOnCheckedChangeListener { _, isChecked -> if (isChecked) game.type = 2 }
        rb_black.setOnCheckedChangeListener { _, isChecked -> if (isChecked) game.bw = 1 }
        rb_white.setOnCheckedChangeListener { _, isChecked -> if (isChecked) game.bw = 2 }
        rb_black2.setOnCheckedChangeListener { _, isChecked -> if (isChecked) game.nextBW = 1 }
        rb_white2.setOnCheckedChangeListener { _, isChecked -> if (isChecked) game.nextBW = 2 }
        btnStart.setOnClickListener { startIntent() }

        spinnerSize.setSelection(0, true)
        rb_start.isChecked = true
        rb_black.isChecked = true
        rb_black2.isChecked = true
    }

    private fun startIntent() {
        if (mediaIntent != null && mediaResult != 0) {
            (application as ShotApplication).result = mediaResult
            (application as ShotApplication).intent = mediaIntent
            val intent = Intent(applicationContext, MyService::class.java)
            intent.putExtra("gameInfo",game)
            startService(intent)
        } else {
            val mMediaProjectionManager = application.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION)
            (application as ShotApplication).setMediaProjectionManager(mMediaProjectionManager)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                when (requestCode) {
                    REQUEST_MEDIA_PROJECTION -> {
                        mediaResult = resultCode
                        mediaIntent = data
                        (application as ShotApplication).result = resultCode
                        (application as ShotApplication).intent = data
                        val intent = Intent(applicationContext, MyService::class.java)
                        intent.putExtra("gameInfo",game)
                        startService(intent)

                        finish()
                    }
                }
            }
        }
    }
}
