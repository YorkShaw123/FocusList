package com.bistu.focuslist.ui.template

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bistu.focuslist.databinding.ItemTemplateBinding
import com.bistu.focuslist.network.StudyTemplate

class TemplateAdapter(
    private val onImport: (StudyTemplate) -> Unit
) : ListAdapter<StudyTemplate, TemplateAdapter.TemplateViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val binding = ItemTemplateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TemplateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TemplateViewHolder(
        private val binding: ItemTemplateBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(template: StudyTemplate) = with(binding) {
            textTitle.text = template.title
            textMeta.text = "${template.category} · ${template.estimatedDays} 天 · ${template.tasks.size} 项"
            textSource.text = template.source
            textDescription.text = template.description
            btnImport.setOnClickListener { onImport(template) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<StudyTemplate>() {
        override fun areItemsTheSame(oldItem: StudyTemplate, newItem: StudyTemplate): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: StudyTemplate, newItem: StudyTemplate): Boolean =
            oldItem == newItem
    }
}
