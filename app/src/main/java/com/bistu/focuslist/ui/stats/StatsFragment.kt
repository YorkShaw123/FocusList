package com.bistu.focuslist.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bistu.focuslist.R
import com.bistu.focuslist.databinding.FragmentStatsBinding
import com.bistu.focuslist.provider.TaskProvider
import com.google.android.material.snackbar.Snackbar

/**
 * 统计页面。
 * 展示今日 / 累计专注数据与最近记录；
 * 并提供一个“内容提供器查询”演示按钮，直观体现 ContentProvider 的使用。
 */
class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by viewModels()
    private lateinit var adapter: SessionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SessionAdapter()
        binding.recyclerSessions.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerSessions.adapter = adapter
        binding.recyclerSessions.isNestedScrollingEnabled = false

        viewModel.todayMinutes.observe(viewLifecycleOwner) {
            binding.textTodayMinutes.text = it.toString()
        }
        viewModel.todayCount.observe(viewLifecycleOwner) {
            binding.textTodayCount.text = it.toString()
        }
        viewModel.totalMinutes.observe(viewLifecycleOwner) {
            binding.textTotalMinutes.text = it.toString()
        }
        viewModel.totalCount.observe(viewLifecycleOwner) {
            binding.textTotalCount.text = it.toString()
        }
        viewModel.pendingCount.observe(viewLifecycleOwner) {
            binding.textPendingCount.text = it.toString()
        }
        viewModel.recentSessions.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.textNoSession.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.btnProviderDemo.setOnClickListener { queryViaProvider() }
    }

    /** 通过 ContentResolver 调用 TaskProvider 统计任务数量（演示内容提供器）。 */
    private fun queryViaProvider() {
        var total = 0
        var pending = 0
        var done = 0
        try {
            requireContext().contentResolver.query(
                TaskProvider.CONTENT_URI, arrayOf("isDone"), null, null, null
            )?.use { cursor ->
                val idx = cursor.getColumnIndex("isDone")
                while (cursor.moveToNext()) {
                    total++
                    if (idx >= 0 && cursor.getInt(idx) == 0) pending++ else done++
                }
            }
            val msg = getString(R.string.provider_demo_result, total, pending, done)
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
        } catch (e: Exception) {
            Snackbar.make(binding.root, "查询失败：${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
