package com.lxf.ndkdemo.net

import cn.izis.chessdesk.net.BASE_URL
import cn.izis.chessdesk.net.CONNECT_TIME_OUT
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.lxf.ndkdemo.bean.RequestModel
import com.lxf.ndkdemo.bean.ResponseModel
import com.lxf.ndkdemo.getGson
import com.lxf.ndkdemo.gsonToBean
import com.lxf.ndkdemo.gsonToList
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

/**
 * 网络接口
 * Created by lxf on 2016/10/19.
 */
interface Api {
    @POST("getdataserver")
    fun get(@Body requestModel: RequestModel): Observable<ResponseModel>

    @POST("postdataserver")
    fun post(@Body requestModel: RequestModel): Observable<ResponseModel>

    @Headers("Content-Type:text/xml;charset=utf-8", "SOAPAction:http://webservices.izis.cn/Move_GetDate")
    @POST("MyGoServicesPort")
    fun postSoap(@Body body: String): Observable<ResponseBody>

    @Streaming
    @GET
    fun down(@Url url: String): Observable<ResponseBody>
}


/**
 * 网络请求工具类
 * Created by lxf on 2016/10/19.
 */
class NetWork private constructor() {
    private var api: Api

    init {
        val client = OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.MILLISECONDS)
                .build()

        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()

        val retrofit = Retrofit.Builder()
                .client(client)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        api = retrofit.create(Api::class.java)
    }

    companion object {
        val instance: NetWork by lazy { NetWork() }
    }

    fun <T> getObject(code: String, info: String, userid: Int, cls: Class<T>): Observable<T> {

        val observable = api.get(RequestModel(code, info, "xxxx", userid))

        return observable
                .compose(handleObject(cls))
    }

    fun <T> getList(code: String, info: String, userid: Int, cls: Class<T>): Observable<List<T>> {

        val observable = api.get(RequestModel(code, info, "xxxx", userid))

        return observable
                .compose(handleList(cls))
    }

    fun <T> postObject(code: String, info: String, userid: Int, cls: Class<T>): Observable<T> {

        val observable = api.post(RequestModel(code, info, "xxxx", userid))

        return observable
                .compose(handleObject(cls))
    }

    fun <T> postList(code: String, info: String, userid: Int, cls: Class<T>): Observable<List<T>> {

        val observable = api.post(RequestModel(code, info, "xxxx", userid))

        return observable
                .compose(handleList(cls))
    }

    fun down(url: String): Observable<ResponseBody> {
        val client = OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.MILLISECONDS)
                .addNetworkInterceptor { chain ->
                    val originalResponse = chain.proceed(chain.request())
                    originalResponse
                            .newBuilder()
                            .body(FileResponseBody(originalResponse))//将自定义的ResposeBody设置给它
                            .build()
                }
                .build()

        val retrofit = Retrofit.Builder()
                .client(client)
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        val api = retrofit.create(Api::class.java)
        return api.down(url)
    }

    /**
     * 对结果进行预处理
     */
    private fun <T> handleObject(cls: Class<T>): ObservableTransformer<ResponseModel, T> {
        return ObservableTransformer { upstream ->
            upstream
                    .flatMap { responseModel ->
                        if (responseModel.error == 0) {
                            createObject(responseModel.data, cls)
                        } else {
                            Observable.error(ServerException(responseModel.msg))
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    /**
     * 对结果进行预处理
     */
    private fun <T> handleList(cls: Class<T>): ObservableTransformer<ResponseModel, List<T>> {
        return ObservableTransformer { upstream ->
            upstream
                    .flatMap { responseModel ->
                        if (responseModel.error == 0) {
                            createList(responseModel.data, cls)
                        } else {
                            Observable.error(ServerException(responseModel.msg))
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    /**
     * 创建成功的数据
     */
    private fun <T> createObject(data: String, cls: Class<T>): Observable<T> {
        return Observable.create { emitter ->
            try {
                emitter.onNext(getGson().gsonToBean(data, cls))
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    /**
     * 创建成功的数据
     */
    private fun <T> createList(data: String, cls: Class<T>): Observable<List<T>> {
        return Observable.create { emitter ->
            try {
                emitter.onNext(getGson().gsonToList(data, cls))
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }
}
