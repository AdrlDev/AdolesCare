package dev.adriele.adolescare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.adriele.adolescare.R
import dev.adriele.adolescare.model.CategoryModuleGroup
import kotlinx.coroutines.CoroutineScope

class PdfModulesAdapter(
    private val groupedModules: List<CategoryModuleGroup>,
    private val scope: CoroutineScope
) : RecyclerView.Adapter<PdfModulesAdapter.CategoryViewHolder>() {
    // Shared recycled view pool for all horizontal RecyclerViews
    private val sharedPool = RecyclerView.RecycledViewPool()

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tv_category)
        val rvList: RecyclerView = view.findViewById(R.id.rv_list)

        init {
            rvList.setRecycledViewPool(sharedPool)
            rvList.setHasFixedSize(true)
            rvList.layoutManager = LinearLayoutManager(
                view.context, LinearLayoutManager.HORIZONTAL, false
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pdf_modules_layout, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val group = groupedModules[position]
        holder.tvCategory.text = group.category

        // Set the adapter only if it's not already set or if data changed
        val currentAdapter = holder.rvList.adapter as? ModuleItemAdapter
        if (currentAdapter == null || currentAdapter.modules != group.modules) {
            holder.rvList.adapter = ModuleItemAdapter(group.modules, scope)
        }
    }

    override fun getItemCount(): Int = groupedModules.size
}
