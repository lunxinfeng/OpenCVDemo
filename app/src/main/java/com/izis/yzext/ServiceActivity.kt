package com.izis.yzext

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.izis.yzext.net.DOWNLOAD_URL
import com.izis.yzext.net.ProgressSubscriber
import com.izis.yzext.net.net_code_getServerCode
import com.izis.yzext.net.net_info_getServerCode
import com.izis.yzext.update.*
import kotlinx.android.synthetic.main.activity_service.*
import lxf.widget.util.SharedPrefsUtil
import java.io.File

const val PLATFORM_TX = "com.tencent.tmgp.ttwq"
const val PLATFORM_YC = "com.eweiqi.android"
const val PLATFORM_YK = "com.indeed.golinks"
//const val PLATFORM_JJ = "com.r99weiqi.dvd"
//const val PLATFORM_JJ = "com.weiqi99.www"
const val PLATFORM_XB = "com.cngames.weiqi_shaoer_mobile"
//const val PLATFORM_YZ = "cn.izis.mygo.mplat"

class ServiceActivity : AppCompatActivity() {
    private var mediaResult = 0
    private var mediaIntent: Intent? = null
    private val REQUEST_MEDIA_PROJECTION = 1
    private var chessApps = mutableListOf<ResolveInfo>()
    private var appsAdapter: AppsAdapter? = null
    private val times = listOf("300","500","800","1000","1500")

    companion object {
        @JvmStatic
        var PLATFORM = PLATFORM_YK
        @JvmStatic
        var boardId:String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service)

        setSupportActionBar(toolbar)
        toolbar.inflateMenu(R.menu.menu_init)

        if (Build.VERSION.SDK_INT >= 24){
            Settings.Secure.putString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,"com.izis.yzext/com.izis.yzext.ClickAccessibilityService")
            Settings.Secure.putInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED,1)
        }

//        textDes.text = Html.fromHtml(resources.getString(R.string.scription))

        if(SharedPrefsUtil.getValue(this,"first",true)){
            MaterialDialog(this)
                    .apply {
                        setActionButtonEnabled(WhichButton.POSITIVE,false)
                        setCancelable(false)
                        checkBoxPrompt(text = "我同意"){
                            setActionButtonEnabled(WhichButton.POSITIVE,it)
                        }
                    }
                    .title(text = "关于第三方对弈平台软件声明")
                    .message(res = R.string.des)
                    .positiveButton(text = "确定"){
                        SharedPrefsUtil.putValue(this,"first",false)
                        checkUpdate()
                    }
                    .negativeButton(text = "取消"){
                        finish()
                    }
                    .show()
        }else{
            checkUpdate()
        }

//        val yzUserName = intent.getStringExtra("userName")
//        val yzPassword = intent.getStringExtra("password")
        boardId = intent.getStringExtra("boardId")

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
//                    4 -> {
//                        platformConfig(PLATFORM_JJ)
//                        platform_name = "99围棋"
//                        platform_url = "http://www.izis.cn/GoWebService/jjwq.apk"
//                    }
                    5 -> {
                        platformConfig(PLATFORM_XB)
                        platform_name = "新博围棋"
                        platform_url = "http://www.izis.cn/GoWebService/xbwq.apk"
                    }
//                    6 -> {
//                        platformConfig(PLATFORM_YZ)
//                        platform_name = "隐智围棋"
//                        platform_url = "http://www.izis.cn/GoWebService/yzwq.apk"
//                    }
                }

                MaterialDialog(this)
                        .message(text = "未检测到$platform_name，是否下载安装？")
                        .positiveButton(text = "立即下载"){
                            val progressDialog = ProgressDialog(this)
                            progressDialog.show()
                            val url = platform_url
                            val apkPath = Environment.getExternalStorageDirectory().path + File.separator + "$platform_name.apk"
//                            UpdateManager.downloadApk(this,url,apkPath, CompositeDisposable())
                            MyIntentService.startUpdateService(this,url,apkPath,progressDialog)
                        }
                        .negativeButton(text = "暂不下载"){
                            finish()
                        }
                        .show()
                return@setOnItemClickListener
            }


            val componentName = ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name)
            val intent = Intent()
            intent.component = componentName
//            intent.putExtra("userName",yzUserName)
//            intent.putExtra("password",yzPassword)
            startActivity(intent)

            platformConfig(resolveInfo.activityInfo.packageName)
        }

        double_click_time = times[SharedPrefsUtil.getValue(this,"click_time",1)].toLong()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_init, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_time ->{
                MaterialDialog(this)
                        .title(text = "设置双击时间的有效间隔(ms)")
                        .listItemsSingleChoice(
                                initialSelection = SharedPrefsUtil.getValue(this,"click_time",2),
                                items = times
                        ){dialog, index, text ->
                            double_click_time = text.toLong()
                            SharedPrefsUtil.putValue(this,"click_time",index)
                            dialog.dismiss()
                        }
                        .show()
            }
            R.id.action_des ->{
                MaterialDialog(this)
                        .title(text = "关于第三方对弈平台软件声明")
                        .message(res = R.string.des)
                        .positiveButton(text = "确定")
                        .show()
            }
        }
        return super.onOptionsItemSelected(item)
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
                                    val progressDialog = ProgressDialog(this@ServiceActivity)
                                    progressDialog.show()
                                    val downUrl = String.format(DOWNLOAD_URL, serverVersionCode)
                                    val apkPath = Environment.getExternalStorageDirectory().path + File.separator + "yzExt.apk"
                                    MyIntentService.startUpdateService(this@ServiceActivity, downUrl, apkPath,progressDialog)
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
//                    || packName == PLATFORM_JJ
                    || packName == PLATFORM_XB
//                    || packName == PLATFORM_YZ
                    ) {
                chessApps.add(info)
            }
        }

        val hasYK = chessApps.any { it.activityInfo.packageName == PLATFORM_YK }
        val hasTX = chessApps.any { it.activityInfo.packageName == PLATFORM_TX }
        val hasYC = chessApps.any { it.activityInfo.packageName == PLATFORM_YC }
//        val hasJJ = chessApps.any { it.activityInfo.packageName == PLATFORM_JJ }
        val hasXB = chessApps.any { it.activityInfo.packageName == PLATFORM_XB }
//        val hasYZ = chessApps.any { it.activityInfo.packageName == PLATFORM_YZ }
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
//        if (!hasJJ){
//            val resolveInfo = ResolveInfo()
//            resolveInfo.match = 4
//            chessApps.add(0,resolveInfo)
//        }
        if (!hasXB){
            val resolveInfo = ResolveInfo()
            resolveInfo.match = 5
            chessApps.add(0,resolveInfo)
        }

//        if (!hasYZ){
//            val resolveInfo = ResolveInfo()
//            resolveInfo.match = 6
//            chessApps.add(0,resolveInfo)
//        }
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
//            PLATFORM_JJ -> {
//                PLATFORM = PLATFORM_JJ
//                MyService.statusH = 0
//                PaserUtil.thresh = 80
//            }
            PLATFORM_XB -> {
                PLATFORM = PLATFORM_XB
                MyService.statusH = 0
                PaserUtil.thresh = 100
            }
//            PLATFORM_YZ -> {
//                PLATFORM = PLATFORM_YZ
//                MyService.statusH = 0
//                PaserUtil.thresh = 80
//            }
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
