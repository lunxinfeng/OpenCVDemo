package com.izis.yzext

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import com.izis.yzext.update.UpdateDialog
import com.izis.yzext.update.VersionMessage
import com.izis.yzext.net.DOWNLOAD_URL
import com.izis.yzext.net.ProgressSubscriber
import com.izis.yzext.net.net_code_getServerCode
import com.izis.yzext.net.net_info_getServerCode
import com.izis.yzext.update.MyIntentService
import com.izis.yzext.update.UpdateManager
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_service.*
import java.io.File

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

        checkUpdate()
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        appsAdapter = AppsAdapter(chessApps, packageManager)
        recyclerView.adapter = appsAdapter
        appsAdapter?.setOnItemClickListener { resolveInfo ->
            if (resolveInfo.activityInfo == null){
                var platform_name = ""
                var platform_url = ""
                when (resolveInfo.match) {
                    1 -> {
                        platformConfig(PLATFORM_YK)
                        platform_name = "弈客围棋"
                        platform_url = "http://www.izis.cn/GoWebService/yike.apk"
                    }
                    2 -> {
                        platformConfig(PLATFORM_TX)
                        platform_name = "腾讯围棋"
                        platform_url = "http://www.izis.cn/GoWebService/txwq.apk"
                    }
                    3 -> {
                        platformConfig(PLATFORM_YC)
                        platform_name = "弈城围棋"
                        platform_url = "http://www.izis.cn/GoWebService/ycwq.apk"
                    }
                    4 -> {
                        platformConfig(PLATFORM_JJ)
                        platform_name = "99围棋"
                        platform_url = "http://www.izis.cn/GoWebService/jjwq.apk"
                    }
                    5 -> {
                        platformConfig(PLATFORM_XB)
                        platform_name = "新博围棋"
                        platform_url = "http://www.izis.cn/GoWebService/xbwq.apk"
                    }
                }

                AlertDialog.Builder(this)
                        .setMessage("未检测到$platform_name，是否下载安装？")
                        .setNegativeButton("暂不下载"){dialog, _ -> dialog.dismiss() }
                        .setPositiveButton("立即下载"){dialog, _ ->
                            val url = platform_url
                            val apkPath = Environment.getExternalStorageDirectory().path + File.separator + "$platform_name.apk"
                            UpdateManager.downloadApk(this,url,apkPath, CompositeDisposable())
                            dialog.dismiss()
                        }
                        .show()
                return@setOnItemClickListener
            }


            val componentName = ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name)
            val intent = Intent()
            intent.component = componentName
            startActivity(intent)

            platformConfig(resolveInfo.activityInfo.packageName)
        }
    }

    override fun onResume() {
        super.onResume()
        getMyApps(packageManager)
        appsAdapter?.notifyDataSetChanged()
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

    private fun checkUpdate() {
        getObject(net_code_getServerCode, net_info_getServerCode(),0, VersionMessage::class.java)
                .subscribe(object : ProgressSubscriber<VersionMessage>(this, false, false) {
                    override fun _onNext(t: VersionMessage) {
                        println(t)
                        val packageInfo = packageManager.getPackageInfo(packageName, 0)
                        val localVersionCode = packageInfo.versionCode

                        val serverVersionCode = t.versionCode
                        val decription = t.app_describe
                        if (serverVersionCode > localVersionCode) {
                            val dialog = UpdateDialog(this@ServiceActivity, R.style.dialog, decription)
                            dialog.setClickListener(object : UpdateDialog.ClickListener {
                                override fun doUpdate() {
                                    toast("进入后台下载")
                                    val downUrl = String.format(DOWNLOAD_URL, serverVersionCode)
                                    val apkPath = Environment.getExternalStorageDirectory().path + File.separator + "yzExt.apk"
                                    MyIntentService.startUpdateService(this@ServiceActivity, downUrl, apkPath)
                                }

                                override fun doCancel() {
                                    startIntent()
                                }
                            })
                            dialog.show()
                        }else{
                            startIntent()
                        }
                    }

                    override fun _onError(error: String?) {
                        toast("获取最新版本号失败")
                    }
                })
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

        val hasYK = chessApps.any { it.activityInfo.packageName == PLATFORM_YK }
        val hasTX = chessApps.any { it.activityInfo.packageName == PLATFORM_TX }
        val hasYC = chessApps.any { it.activityInfo.packageName == PLATFORM_YC }
        val hasJJ = chessApps.any { it.activityInfo.packageName == PLATFORM_JJ }
        val hasXB = chessApps.any { it.activityInfo.packageName == PLATFORM_XB }
        if (!hasYK){
            val resolveInfo = ResolveInfo()
            resolveInfo.match = 1
            chessApps.add(0,resolveInfo)
        }
        if (!hasTX){
            val resolveInfo = ResolveInfo()
            resolveInfo.match = 2
            chessApps.add(0,resolveInfo)
        }
        if (!hasYC){
            val resolveInfo = ResolveInfo()
            resolveInfo.match = 3
            chessApps.add(0,resolveInfo)
        }
        if (!hasJJ){
            val resolveInfo = ResolveInfo()
            resolveInfo.match = 4
            chessApps.add(0,resolveInfo)
        }
        if (!hasXB){
            val resolveInfo = ResolveInfo()
            resolveInfo.match = 5
            chessApps.add(0,resolveInfo)
        }
    }

    private fun platformConfig(platform: String) {
        when (platform) {
            PLATFORM_TX -> {
                PLATFORM = PLATFORM_TX
                MyService.statusH = 0
                PaserUtil.thresh = 80
            }
            PLATFORM_YC -> {
                PLATFORM = PLATFORM_YC
                MyService.statusH = 0
                PaserUtil.thresh = 80
            }
            PLATFORM_YK -> {
                PLATFORM = PLATFORM_YK
                MyService.statusH = ScreenUtil.getStatusBarHeight(this)
                PaserUtil.thresh = 80
            }
            PLATFORM_JJ -> {
                PLATFORM = PLATFORM_JJ
                MyService.statusH = 0
                PaserUtil.thresh = 80
            }
            PLATFORM_XB -> {
                PLATFORM = PLATFORM_XB
                MyService.statusH = 0
                PaserUtil.thresh = 100
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent(applicationContext, MyService::class.java)
        stopService(intent)
        super.onBackPressed()
        println("ServiceActivity.onBackPressed")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("ServiceActivity.onDestroy")
        val intent = Intent(applicationContext, MyService::class.java)
        stopService(intent)
    }
}
