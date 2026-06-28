package com.bistu.focuslist.ui.focus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bistu.focuslist.data.Repository
import com.bistu.focuslist.data.Task
import com.bistu.focuslist.network.RetrofitClient
import com.bistu.focuslist.util.Prefs
import kotlinx.coroutines.launch

/**
 * 专注页 ViewModel。
 * 1) 提供未完成任务列表（供选择本次专注关联的任务）；
 * 2) 通过网络获取“每日一言”，并做本地缓存与离线兜底。
 */
class FocusViewModel(app: Application) : AndroidViewModel(app) {

    val pendingTasks: LiveData<List<Task>> = Repository.get(app).observePendingTasks()

    private val _quote = MutableLiveData<QuoteUi>()
    val quote: LiveData<QuoteUi> = _quote

    init {
        val cached = Prefs.getLastQuote(app)
        _quote.value = if (cached.isNotBlank()) {
            QuoteUi(cached, Prefs.getLastQuoteFrom(app))
        } else {
            QuoteUi(DEFAULT_QUOTE, "古谚")
        }
        fetchQuote()
    }

    /** 拉取一条新的句子 */
    fun fetchQuote() {
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.quoteApi.getQuote()
                val text = resp.text?.takeIf { it.isNotBlank() }
                if (text != null) {
                    val from = resp.from?.takeIf { it.isNotBlank() } ?: ""
                    _quote.value = QuoteUi(text, from)
                    Prefs.saveQuote(getApplication(), text, from)
                }
            } catch (_: Exception) {
                // 网络失败：保留当前（缓存或默认）句子，不打断用户
            }
        }
    }

    data class QuoteUi(val text: String, val from: String)

    companion object {
        const val DEFAULT_QUOTE = "种一棵树最好的时间是十年前，其次是现在。"
    }
}
