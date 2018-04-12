package com.zhangls.android.attendance.http

import com.zhangls.android.attendance.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * 初始化 Retrofit
 *
 * @author zhangls
 */
object ApiService {

    /**
     * Debug Base URL
     */
    private const val BASE_URL_DEBUG = "http://dsn.apizza.cc/mock/f88de14344a7d841979dc7f145640430/"
    /**
     * Release Base URL
     */
    private const val BASE_URL_RELEASE = "http://dsn.apizza.cc/mock/f88de14344a7d841979dc7f145640430/"
    /**
     * URL
     */
    private val BASE_URL = if (BuildConfig.DEBUG) BASE_URL_DEBUG else BASE_URL_RELEASE
    /**
     * 默认请求超时时间
     */
    private const val DEFAULT_TIMEOUT = 10

    val default: Retrofit
        get() {
            val builder = OkHttpClient.Builder()
            builder.connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            if (BuildConfig.DEBUG) {
                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.BODY
                builder.addInterceptor(logging)
            }
            return Retrofit.Builder()
                    .client(builder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl(BASE_URL)
                    .build()
        }
}