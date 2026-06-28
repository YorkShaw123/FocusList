package com.bistu.focuslist.ui.tasks

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bistu.focuslist.R
import com.bistu.focuslist.databinding.FragmentTaskListBinding
import com.bistu.focuslist.service.FocusTimerService
import com.bistu.focuslist.ui.MainActivity
import com.bistu.focuslist.ui.addedit.AddEditTaskActivity
import com.bistu.focuslist.util.Prefs
import com.google.android.material.snackbar.Snackbar

/**
 * 待办列表页面。
 * 展示任务、勾选完成、滑动删除（可撤销）、点击编辑、一键开始专注。
 */
class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter

    // 启动“添加/编辑任务”页（列表通过 LiveData 自动刷新，无需处理返回值）
    private val addEditLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        binding.fabAdd.setOnClickListener {
            addEditLauncher.launch(Intent(requireContext(), AddEditTaskActivity::class.java))
        }

        binding.btnClearCompleted.setOnClickListener {
            viewModel.clearCompleted()
        }

        viewModel.tasks.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onToggle = { viewModel.toggleDone(it) },
            onClick = { task ->
                val intent = Intent(requireContext(), AddEditTaskActivity::class.java)
                    .putExtra(AddEditTaskActivity.EXTRA_TASK_ID, task.id)
                addEditLauncher.launch(intent)
            },
            onStartFocus = { task ->
                FocusTimerService.startFocus(
                    requireContext(),
                    Prefs.getFocusMinutes(requireContext()),
                    task.id,
                    task.title
                )
                (activity as? MainActivity)?.showFocusTab()
            }
        )
        binding.recyclerTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTasks.adapter = adapter

        // 左右滑动删除，支持撤销
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val task = adapter.currentList[position]
                viewModel.delete(task)
                Snackbar.make(binding.root, R.string.task_deleted, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo) { viewModel.insert(task) }
                    .show()
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.recyclerTasks)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
