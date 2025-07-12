package dev.adriele.adolescare.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import dev.adriele.adolescare.R
import dev.adriele.adolescare.helpers.enums.SymptomCategory
import dev.adriele.adolescare.model.SymptomsActivitiesQ

class SymptomsActivitiesAdapter(
    private val items: List<SymptomsActivitiesQ>,
    private val selectedOptions: Map<String, List<String>>, // preselected items
    private val isViewOnly: Boolean,
    private val listener: (category: String, selectedChips: List<String>) -> Unit
) : RecyclerView.Adapter<SymptomsActivitiesAdapter.SymptomsViewHolder>() {

    private val selectedItemsMap = mutableMapOf<String, MutableList<String>>()

    inner class SymptomsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tv_category)
        val chipGroup: ChipGroup = view.findViewById(R.id.chip_group)
        val main: MaterialCardView = view.findViewById(R.id.main)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymptomsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.symptoms_activity_layout, parent, false)
        return SymptomsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SymptomsViewHolder, position: Int) {
        val context = holder.itemView.context

        val item = items[position]
        val categoryEnum = SymptomCategory.valueOf(item.category)
        val displayCategory = context.getString(categoryEnum.labelRes)

        val preselected = selectedOptions[categoryEnum.name] ?: emptyList()
        val selectedChips = mutableListOf<String>().apply { addAll(preselected) }
        selectedItemsMap[categoryEnum.name] = selectedChips

        holder.tvCategory.text = displayCategory
        holder.chipGroup.removeAllViews()

        if (isViewOnly) {
            holder.main.cardElevation = 0f
            holder.tvCategory.textSize = 12f
        }

        Log.e("PRE_SELECTED_LIST", Gson().toJson(preselected))

        categoryEnum.options.forEach { optionEnum ->
            val optionKey = optionEnum.name
            val optionLabel = context.getString(optionEnum.resId)
            val isSelected = preselected.contains(optionKey)

            if (isViewOnly && !isSelected) {
                Log.w("CHIP_SKIPPED", "Skipping $optionKey in view-only mode")
                return@forEach
            }

            val chip = Chip(holder.chipGroup.context).apply {
                text = optionLabel
                isCheckable = true
                isChecked = isSelected
                isEnabled = !isViewOnly
                isClickable = !isViewOnly
                textSize = if(isViewOnly) 12f else 14f

                chipIcon = ContextCompat.getDrawable(context, R.drawable.round_check_20)
                chipIconTint = holder.itemView.context.getColorStateList(R.color.buttonTextColor)
                isChipIconVisible = isChecked
                setTextColor(resources.getColor(R.color.buttonTextColor, null))
                chipBackgroundColor = holder.itemView.context.getColorStateList(android.R.color.transparent)
                chipStrokeColor = holder.itemView.context.getColorStateList(R.color.buttonColor)
            }

            val shapeAppearanceModel = chip.shapeAppearanceModel
                .toBuilder()
                .setAllCornerSizes(62f)
                .build()

            chip.shapeAppearanceModel = shapeAppearanceModel

            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedChips.add(optionKey)
                    chip.chipIcon = ContextCompat.getDrawable(context, R.drawable.round_check_20)
                } else {
                    selectedChips.remove(optionKey)
                    chip.chipIcon = null
                }
                chip.isChipIconVisible = isChecked
                listener(categoryEnum.name, selectedChips)
            }

            holder.chipGroup.addView(chip)
        }
    }

    override fun getItemCount(): Int = items.size
}
