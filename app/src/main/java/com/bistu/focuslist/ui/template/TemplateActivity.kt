package com.bistu.focuslist.ui.template

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bistu.focuslist.R
import com.bistu.focuslist.databinding.ActivityTemplateListBinding
import com.google.android.material.snackbar.Snackbar

class TemplateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTemplateListBinding
    private val viewModel: TemplateViewModel by viewModels()
    private lateinit var adapter: TemplateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTemplateListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.online_templates)

        adapter = TemplateAdapter { template ->
            viewModel.importTemplate(template)
        }
        binding.recyclerTemplates.layoutManager = LinearLayoutManager(this)
        binding.recyclerTemplates.adapter = adapter

        binding.btnRetry.setOnClickListener { viewModel.refresh() }

        viewModel.uiState.observe(this) { render(it) }
    }

    private fun render(state: TemplateUiState) {
        binding.progress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.btnRetry.isEnabled = !state.isLoading
        adapter.submitList(state.templates)
        binding.textStatus.text = if (state.fromNetwork) {
            getString(R.string.template_source_online)
        } else {
            getString(R.string.template_source_local)
        }
        if (state.message.isNotBlank()) {
            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
