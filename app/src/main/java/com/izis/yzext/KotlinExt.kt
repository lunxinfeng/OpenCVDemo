package com.izis.yzext

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Path
import android.os.Build
import android.os.Handler
import android.support.annotation.RequiresApi
import android.text.TextUtils
import android.widget.EditText
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.izis.yzext.net.NetWork
import com.izis.yzext.net.NetWorkSoap
import lxf.widget.util.SharedPrefsUtil
import java.text.SimpleDateFormat
import java.util.*

/**
 * kotlin扩展函数
 * Created by lxf on 2017/6/15.
 */
var double_click_time = 200L

fun getGson() = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()

fun Gson.gsonToString(any: Any) = this.toJson(any)

fun <T> Gson.gsonToBean(json: String, cls: Class<T>) = this.fromJson(json, cls)

fun <T> Gson.gsonToList(json: String, cls: Class<T>): List<T> {
    val mList = mutableListOf<T>()
    val array = JsonParser().parse(json).asJsonArray
    array.forEach { mList.add(this.fromJson(it, cls)) }
    return mList
}

fun <T> getObject(code: String, info: String, userId: Int, cls: Class<T>) = NetWork.instance.getObject(code, info, userId, cls)
fun <T> getList(code: String, info: String, userId: Int, cls: Class<T>) = NetWork.instance.getList(code, info, userId, cls)
fun <T> postObject(code: String, info: String, userId: Int, cls: Class<T>) = NetWork.instance.postObject(code, info, userId, cls)
fun <T> postList(code: String, info: String, userId: Int, cls: Class<T>) = NetWork.instance.postList(code, info, userId, cls)
fun <T> postSoap(type: String, code: String, info: String, userId: String, cls: Class<T>) = NetWorkSoap.instance.postSoap(type, code, info, userId, cls)
fun <T> postListSoap(type: String, code: String, info: String, userId: String, cls: Class<T>) = NetWorkSoap.instance.postListSoap(type, code, info, userId, cls)

fun putValue(context: Context, key: String, value: Any) {
    when (value) {
        is Int -> SharedPrefsUtil.putValue(context.applicationContext, key, value)
        is Boolean -> SharedPrefsUtil.putValue(context.applicationContext, key, value)
        is String -> SharedPrefsUtil.putValue(context.applicationContext, key, value)
    }
}

fun getValue(context: Context, key: String, value: Any): Any {
    when (value) {
        is Int -> return SharedPrefsUtil.getValue(context.applicationContext, key, value)
        is Boolean -> return SharedPrefsUtil.getValue(context.applicationContext, key, value)
        else -> return SharedPrefsUtil.getValue(context.applicationContext, key, value as String)
    }
}

fun removeValue(context: Context, key: String){
    SharedPrefsUtil.removeValue(context.applicationContext,key)
}

//fun getString(@StringRes res:Int):String = CDApplication.instance.applicationContext.getString(res)
/**
 * 返回文本框内容
 */
fun EditText.content(): String = this.text.toString()

fun Dialog.toast(msg: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this.context, msg, duration).show()
}

/**
 * 字符串是否为空
 */
fun isEmpty(s: String?) = TextUtils.isEmpty(s)

fun Activity.toast(msg:String) = Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()

/**
 * 被挤掉线重新登陆
 */
//fun Activity.reLoginWithDialog() = toast(getString(R.string.re_login_with_dialog))

///**
// * 连接电子棋盘
// */
//fun connectUSB(view: View, currBW:String, boardSize:Int) {
//    pl2303interface = Pl2303InterfaceUtilNew.initInterface(this, currBW, this)
//
//    val disposable = Observable.timer(300, TimeUnit.MILLISECONDS)
//            .doOnNext { pl2303interface?.callResume(view) }
//            .flatMap { Observable.timer(300, TimeUnit.MILLISECONDS) }
//            .subscribe { pl2303interface?.OpenUARTDevice(view, boardSize) }
//    addDisposable(disposable)
//}
//
///**
// * 订阅电子棋盘返回的信息
// */
//fun subscribeEvent() {
//    RxBus.getDefault().toObservable(LiveType::class.java)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(object : Observer<LiveType> {
//                override fun onSubscribe(d: Disposable) {
//                    addDisposable(d)
//                }
//
//                override fun onNext(value: LiveType) {
//                    handleEvent(value)
//                }
//
//                override fun onError(e: Throwable) {
//                    subscribeEvent()
//                }
//
//                override fun onComplete() {
//                    subscribeEvent()
//                }
//            })
//}

fun getCurrTime(pattern: String): String = SimpleDateFormat(pattern, Locale.CHINA).format(Date())

fun scanForActivity(cont: Context): Activity? {
    if (cont is Activity)
        return cont
    else if (cont is ContextWrapper)
        return scanForActivity(cont.baseContext)

    return null
}

@RequiresApi(Build.VERSION_CODES.N)
fun AccessibilityService.gestureOnScreen(
        path: Path,
        startTime:Long = 0,
        duration:Long = 100,
        callback: AccessibilityService.GestureResultCallback?,
        handler: Handler? = null
){
    val builder = GestureDescription.Builder()
    builder.addStroke(GestureDescription.StrokeDescription(path, startTime, duration))
    val gesture = builder.build()
    dispatchGesture(gesture, callback, handler)
}

@RequiresApi(Build.VERSION_CODES.N)
fun AccessibilityService.clickOnScreen(
        x:Float,
        y:Float,
        callback: AccessibilityService.GestureResultCallback?,
        handler: Handler? = null
){
    val p = Path()
    p.moveTo(x,y)
    gestureOnScreen(p,callback = callback,handler = handler)
}