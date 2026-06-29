package com.bistu.focuslist.network

import retrofit2.http.GET

/** 在线学习任务模板接口。 */
interface TemplateApi {

    @GET("focuslist/templates.json")
    suspend fun getTemplates(): TemplateResponse
}
