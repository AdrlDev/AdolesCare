package dev.adriele.calendarview.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dev.adriele.calendarview.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarAdapter : Adapter<CalendarAdapter.DayViewHolder>() {
    private var days: List<Calendar> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<Calendar>) {
        days = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount(): Int = days.size

    inner class DayViewHolder(itemView: View) : ViewHolder(itemView) {
        private val tvDayLetter: TextView = itemView.findViewById(R.id.tvDayLetter)
        private val tvDayNumber: TextView = itemView.findViewById(R.id.tvDayNumber)
        private val tvTodayLabel: TextView = itemView.findViewById(R.id.tvTodayLabel)

        fun bind(calendar: Calendar) {
            val isToday = calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

            val dayLetter = SimpleDateFormat("E", Locale.getDefault()).format(calendar.time).substring(0, 1)
            val dayNumber = calendar.get(Calendar.DAY_OF_MONTH).toString()

            tvDayLetter.text = dayLetter
            tvDayNumber.text = dayNumber

            if (isToday) {
                tvTodayLabel.visibility = View.VISIBLE
                tvDayNumber.setTextColor(ContextCompat.getColor(itemView.context, R.color.today)) // custom today color
                tvDayLetter.setTextColor(ContextCompat.getColor(itemView.context, R.color.not_today))
            } else {
                tvTodayLabel.visibility = View.INVISIBLE
                tvDayNumber.setTextColor(ContextCompat.getColor(itemView.context, R.color.not_today))
                tvDayLetter.setTextColor(ContextCompat.getColor(itemView.context, R.color.not_today))
            }
        }
    }
}