package dev.adriele.adolescare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dev.adriele.adolescare.R
import dev.adriele.adolescare.model.SymptomsActivitiesQ

class SymptomsActivitiesAdapter(
    private val items: List<SymptomsActivitiesQ>,
    private val selectedOptions: Map<String, List<String>>, // preselected items
    private val isDisplayOnly: Boolean,
    private val onChipChecked: (category: String, selectedChips: List<String>) -> Unit
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
        val item = items[position]
        val preselected = selectedOptions[item.category] ?: emptyList()
        val selectedChips = mutableListOf<String>().apply { addAll(preselected) }
        selectedItemsMap[item.category] = selectedChips

        holder.tvCategory.text = item.category
        holder.chipGroup.removeAllViews()

        if(isDisplayOnly) {
            holder.main.cardElevation = 0f
        }

        item.choices.forEach { option ->
            val chip = Chip(holder.chipGroup.context).apply {
                text = option
                isCheckable = true
                isChecked = preselected.contains(option)
                isEnabled = !isDisplayOnly
                isClickable = !isDisplayOnly

                // Setup the icon when checked
                chipIcon = ContextCompat.getDrawable(context, R.drawable.round_check_20)
                chipIconTint = holder.itemView.context.getColorStateList(R.color.buttonTextColor)
                isChipIconVisible = isChecked // show if checked, hide if not
                setTextColor(resources.getColor(R.color.buttonTextColor, null)) // optional
                chipBackgroundColor = holder.itemView.context.getColorStateList(android.R.color.transparent)
                chipStrokeColor = holder.itemView.context.getColorStateList(R.color.buttonColor)
            }

            val shapeAppearanceModel = chip.shapeAppearanceModel
                .toBuilder()
                .setAllCornerSizes(62f)
                .build()

            chip.shapeAppearanceModel = shapeAppearanceModel

            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    selectedChips.add(option)
                    chip.chipIcon =
                        ContextCompat.getDrawable(holder.itemView.context, R.drawable.round_check_20)
                    chip.isChipIconVisible = true
                } else {
                    selectedChips.remove(option)
                    chip.chipIcon = null
                    chip.isChipIconVisible = false
                }

                onChipChecked(item.category, selectedChips)
            }

            holder.chipGroup.addView(chip)
        }
    }

    override fun getItemCount(): Int = items.size
}
