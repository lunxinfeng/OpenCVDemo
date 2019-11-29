package com.izis.yzext

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.accessibility.AccessibilityEvent
import com.izis.yzext.base.RxBus
import com.izis.yzext.base.RxEvent
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class ClickAccessibilityService : AccessibilityService() {

    private var disposable: Disposable? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate() {
        super.onCreate()
        subscribe()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun subscribe() {
        RxBus.getDefault().toObservable(RxEvent::class.java)
                .subscribe(object : Observer<RxEvent> {
                    override fun onComplete() {
                        subscribe()
                    }

                    override fun onSubscribe(d: Disposable?) {
                        disposable = d
                    }

                    override fun onNext(value: RxEvent) {
                        if (value.code == RxEvent.click) {
                            val s = value.`object`.toString()
                            val x = s.split(",")[0].toFloat()
                            val y = s.split(",")[1].toFloat()
                            clickOnScreen(x, y, null, null)
                        }
                    }

                    override fun onError(e: Throwable?) {
                        subscribe()
                    }
                })
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }

    override fun onInterrupt() {

    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        println(event)
        if (event != null){
            if (event.className.toString() in classNameList){
                className = event.className.toString()
            }
        }
    }


}