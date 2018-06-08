package com.lxf.ndkdemo.net

import android.content.Context
import com.lxf.ndkdemo.R
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import lxf.widget.WaitDialog
import java.lang.ref.WeakReference
import java.net.ConnectException
import java.net.SocketTimeoutException


/**
 * 用于在Http请求开始时，自动显示一个ProgressDialog
 * 在Http请求结束是，关闭ProgressDialog
 * 调用者自己对请求数据进行处理
 * Created by lxf on 2016/10/16.
 */
abstract class ProgressSubscriber<T> : Observer<T> {
    //    弱引用防止内存泄露
    private var mActivity: WeakReference<Context>? = null
    //    是否能取消请求
    private var cancel: Boolean = false
    //    是否显示请求框
    private var show: Boolean = false
    //    加载框
    private var waitDialog: WaitDialog? = null
    private var mDisposable: Disposable? = null

    /**
     * 默认显示且不可取消 加载框
     */
    constructor(context: Context, show: Boolean = true, cancel: Boolean = false, msg: String? = null) {
        this.mActivity = WeakReference(context)
        this.show = show
        this.cancel = cancel
        initProgressDialog(msg)
    }


    /**
     * 初始化加载框
     */
    private fun initProgressDialog(msg: String?) {
        val context = mActivity!!.get()
        if (waitDialog == null && context != null && show) {
            if (msg == null)
                waitDialog = WaitDialog(context, R.style.dialog)
            else
                waitDialog = WaitDialog(context, R.style.dialog, msg)
            waitDialog?.setCancelable(cancel)
            if (cancel) {
                waitDialog?.setOnCancelListener { onCancelProgress() }
            }
        }
    }


    /**
     * 显示加载框
     */
    private fun showProgressDialog() {
        val context = mActivity?.get()
        if (waitDialog == null || context == null) return
        if (!waitDialog!!.isShowing) {
            waitDialog!!.show()
        }
    }


    /**
     * 隐藏dialog，并解除订阅，防止内存泄漏
     */
    private fun dismissProgressDialog() {
        if (waitDialog != null && waitDialog!!.isShowing) {
            waitDialog!!.dismiss()
        }
        if (mDisposable != null && !mDisposable!!.isDisposed)
            mDisposable!!.dispose()
    }

    /**
     * 最先调用

     */
    override fun onSubscribe(d: Disposable) {
        mDisposable = d
        showProgressDialog()
    }

    override fun onNext(t: T) {
        _onNext(t)
    }

    override fun onComplete() {
        dismissProgressDialog()
    }

    /**
     * 对错误进行统一处理
     * 隐藏ProgressDialog
     */
    override fun onError(e: Throwable) {
        val context = mActivity!!.get() ?: return
        if (e is SocketTimeoutException) {
            _onError("网络连接超时")
        } else if (e is ConnectException) {
            _onError("网络连接超时")
        } else if (e is ServerException) {
            _onError(e.message)
        } else {
            _onError("错误：" + e.message)
        }
        dismissProgressDialog()
    }

    /**
     * 取消ProgressDialog的时候，取消对observable的订阅，同时也取消了http请求
     */
    private fun onCancelProgress() {
        if (mDisposable != null && !mDisposable!!.isDisposed)
            mDisposable!!.dispose()
    }

    abstract fun _onNext(t: T)

    abstract fun _onError(error: String?)
}