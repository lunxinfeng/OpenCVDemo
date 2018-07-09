package com.lxf.ndkdemo

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_service.*

const val PLATFORM_TX = "com.tencent.tmgp.ttwq"
const val PLATFORM_YC = "com.eweiqi.android"
const val PLATFORM_YK = "com.indeed.golinks"
const val PLATFORM_JJ = "com.r99weiqi.dvd"
const val PLATFORM_XB = "com.cngames.weiqi_shaoer_mobile"

class ServiceActivity : AppCompatActivity() {
    private var mediaResult = 0
    private var mediaIntent: Intent? = null
    private val REQUEST_MEDIA_PROJECTION = 1
    private var chessApps: ArrayList<ResolveInfo> = ArrayList()
    private var appsAdapter: AppsAdapter? = null

    companion object {
        var PLATFORM = PLATFORM_YK
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)

        getMyApps(packageManager)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        appsAdapter = AppsAdapter(chessApps, packageManager)
        recyclerView.adapter = appsAdapter
        appsAdapter?.setOnItemClickListener { resolveInfo ->
            val componentName = ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name)
            val intent = Intent()
            intent.component = componentName
            startActivity(intent)

            when(resolveInfo.activityInfo.packageName){
                PLATFORM_TX ->{
                    PLATFORM = PLATFORM_TX
                    MyService.statusH = 0
                    PaserUtil.thresh = 80
                }
                PLATFORM_YC ->{
                    PLATFORM = PLATFORM_YC
                    MyService.statusH = 0
                    PaserUtil.thresh = 80
                }
                PLATFORM_YK ->{
                    PLATFORM = PLATFORM_YK
                    MyService.statusH = ScreenUtil.getStatusBarHeight(this)
                    PaserUtil.thresh = 80
                }
                PLATFORM_JJ ->{
                    PLATFORM = PLATFORM_JJ
                    MyService.statusH = 0
                    PaserUtil.thresh = 80
                }
                PLATFORM_XB ->{
                    PLATFORM = PLATFORM_XB
                    MyService.statusH = 0
                    PaserUtil.thresh = 100
                }
            }
        }

        startIntent()
    }

    private fun startIntent() {
        if (mediaIntent != null && mediaResult != 0) {
            (application as ShotApplication).result = mediaResult
            (application as ShotApplication).intent = mediaIntent
            val intent = Intent(applicationContext, MyService::class.java)
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
                        startService(intent)
                    }
                }
            }
        }
    }

    private fun getMyApps(pm: PackageManager) {
        chessApps.clear()
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(intent, 0)

        for (info in apps) {
            Log.d("ServiceActivity", info.activityInfo.packageName)
            val packName = info.activityInfo.packageName
            if (packName == PLATFORM_TX
                    || packName == PLATFORM_YC
                    || packName == PLATFORM_YK
                    || packName == PLATFORM_JJ
                    || packName == PLATFORM_XB
                    ) {
                chessApps.add(info)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(applicationContext, MyService::class.java)
        stopService(intent)
    }
}
