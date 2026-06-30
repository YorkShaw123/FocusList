package com.bistu.focuslist.network

import okhttp3.OkHttpClient

/**
 * Retrofit 单例客户端。
 * 封装 OkHttp（含超时、日志），对外提供 QuoteApi。
 */
object RetrofitClient {

    private const val BASE_URL = "https://v1.hitokoto.cn/"

    private val okHttp: OkHttpClient by lazy {
        createJsonOkHttpClient(timeoutSeconds = 8)
    }

    val quoteApi: QuoteApi by lazy {
        createQuoteApi(BASE_URL)
    }

    internal fun createQuoteApi(
        baseUrl: String,
        okHttpClient: OkHttpClient = createJsonOkHttpClient(timeoutSeconds = 8)
    ): QuoteApi {
        return createJsonApi(baseUrl, okHttpClient)
    }
}
