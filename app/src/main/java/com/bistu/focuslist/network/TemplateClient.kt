package com.bistu.focuslist.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 学习模板网络客户端。
 *
 * 当前 BASE_URL 作为课程演示占位地址，后续可替换为 GitHub Pages、Gitee Pages
 * 或学校服务器上的静态 JSON 地址。请求失败时模板仓库会回退到本地模板。
 */
object TemplateClient {

    private const val BASE_URL = "https://example.com/"

    private val okHttp: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    val api: TemplateApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TemplateApi::class.java)
    }
}
