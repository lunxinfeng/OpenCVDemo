package com.izis.yzext.net

import android.text.TextUtils
import android.util.Xml
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.izis.yzext.getGson
import com.izis.yzext.gsonToBean
import com.izis.yzext.gsonToList
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import retrofit2.Retrofit
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

/**
 * 基于soap协议的请求
 * Created by lxf on 2017/1/3.
 */
class NetWorkSoap private constructor() {
    private var api: Api

    init {
        val client = OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.MILLISECONDS)
                .build()

        val retrofit = Retrofit.Builder()
                .client(client)
                .baseUrl(BASE_URL_SOAP)
                .addConverterFactory(ToStringConverterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        api = retrofit.create(Api::class.java)
    }

    companion object {
        val METHOD_NAME = "Move_GetDate"
        val METHOD_NAME_UPDATE = "Move_Update"
        val instance: NetWorkSoap by lazy { NetWorkSoap() }
    }

    private fun getRequestData(type: String, num: String, msg: String, userId: String): String {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"http://webservices.izis.cn/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                "<SOAP-ENV:Body>" +
                "<ns1:" + type + ">" +
                "<arg0 xsi:type=\"xsd:string\">" +
                num +
                "</arg0>" +
                "<arg1 xsi:type=\"xsd:string\">" +
                msg +
                "</arg1>" +
                "<arg2 xsi:type=\"xsd:string\">0394cb6d-2575-4f73-92e9-6d402c39e20c</arg2>" +
                "<arg3 xsi:type=\"xsd:string\">" +
                userId +
                "</arg3>" +
                "</ns1:" +
                type +
                ">" +
                "</SOAP-ENV:Body>" +
                "</SOAP-ENV:Envelope>"
    }

    fun <T> postSoap(type: String, num: String, msg: String, userId: String, cls: Class<T>): Observable<T> {
        return api!!.postSoap(getRequestData(type, num, msg, userId))
                .map { responseBody ->
                    val `in` = responseBody.byteStream()
                    parserXml(`in`)
                }
                .flatMap(Function<String, ObservableSource<T>> { s ->
                    Observable.create { e ->
                        try {
                            e.onNext(getGson().gsonToBean(s, cls))
                            e.onComplete()
                        } catch (ex: Exception) {
                            e.onError(ServerException("解析错误"))
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun <T> postListSoap(type: String, num: String, msg: String, userId: String, cls: Class<T>): Observable<List<T>> {
        return api!!.postSoap(getRequestData(type, num, msg, userId))
                .map { responseBody ->
                    val `in` = responseBody.byteStream()
                    parserXml(`in`)
                }
                .flatMap { s ->
                    if (TextUtils.isEmpty(s))
                        Observable.error<List<T>>(ServerException("没有更多数据"))
                    else
                        Observable.create { e ->
                            try {
                                e.onNext(getGson().gsonToList(s, cls))
                                e.onComplete()
                            } catch (ex: Exception) {
                                e.onError(ServerException("解析错误"))
                            }
                        }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun parserXml(`in`: InputStream): String {
        val parser = Xml.newPullParser()
        var data = ""
        try {
            parser.setInput(`in`, "utf-8")
            var evenType = parser.eventType
            while (evenType != XmlPullParser.END_DOCUMENT) {
                when (evenType) {
                    XmlPullParser.START_DOCUMENT -> {
                    }
                    XmlPullParser.START_TAG -> {
                    }
                    XmlPullParser.TEXT -> data = parser.text
                    XmlPullParser.END_TAG -> {
                    }
                }
                parser.next()
                evenType = parser.eventType
            }

        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return data
    }

//    companion object {
//        @Volatile private var INSTANCE: NetWorkSoap? = null
//        val METHOD_NAME = "Move_GetDate"
//        val METHOD_NAME_UPDATE = "Move_Update"
//
//        /**
//         * 获取单例
//         */
//        val instance: NetWorkSoap
//            get() {
//                if (INSTANCE == null) {
//                    synchronized(NetWorkSoap::class.java) {
//                        if (INSTANCE == null) {
//                            INSTANCE = NetWorkSoap()
//                        }
//                    }
//                }
//                return INSTANCE
//            }
//    }

}
