package com.bistu.focuslist.network

import com.google.gson.annotations.SerializedName

/**
 * “一言（Hitokoto）”接口返回的数据模型。
 * 接口地址：https://v1.hitokoto.cn/
 * 返回示例：{"hitokoto":"...","from":"...","from_who":"..."}
 */
data class QuoteResponse(
    @SerializedName("hitokoto") val text: String?,
    @SerializedName("from") val from: String?,
    @SerializedName("from_who") val fromWho: String?
)
