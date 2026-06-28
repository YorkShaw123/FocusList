# 默认混淆规则（本项目 release 未开启 minify，此处保留占位）
# 如需开启混淆，请补充对 Retrofit / Gson 数据模型的 keep 规则。
-keepattributes Signature
-keepattributes *Annotation*
# 保留网络数据模型，避免 Gson 反序列化字段被混淆
-keep class com.bistu.focuslist.network.** { *; }
