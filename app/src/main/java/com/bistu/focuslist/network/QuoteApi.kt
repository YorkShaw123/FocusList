package com.bistu.focuslist.network

import retrofit2.http.GET
import retrofit2.http.Query

/** 一言接口定义。 */
interface QuoteApi {
    /**
     * 获取一条随机句子。
     * @param category 句子类型，i=诗词，k=哲学，d=文学 等；留空为全部
     * @param encode 返回格式，json
     */
    @GET("/")
    suspend fun getQuote(
        @Query("c") category: String = "i",
        @Query("encode") encode: String = "json"
    ): QuoteResponse
}
