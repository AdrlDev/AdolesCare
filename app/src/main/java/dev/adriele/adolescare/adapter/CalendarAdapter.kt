package dev.adriele.adolescare.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import dev.adriele.adolescare.R
import dev.adriele.adolescare.databinding.MyCalendarViewBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarAdapter(
    private val context: Context,
    private val year: Int,
    private val periodDates: List<Calendar>
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private val months = (0..11).toList() // Jan to Dec

    inner class CalendarViewHolder(val binding: MyCalendarViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MyCalendarViewBinding.inflate(inflater, parent, false)
        return CalendarViewHolder(binding)
    }

    override fun getItemCount() = months.size

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val monthIndex = months[position]
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthIndex)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)
        holder.binding.tvMonthTitle.text = monthName

        holder.binding.gridDays.removeAllViews()

        // Get how many empty cells to add before the 1st of the month
        val startDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 (Sun) to 6 (Sat)

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val totalCells = 42 // 7 columns × 6 rows

        val typeface = ResourcesCompat.getFont(context, R.font.roboto)

        val today = Calendar.getInstance()

        for (i in 0 until totalCells) {
            val dayTextView = TextView(holder.itemView.context).apply {
                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }
                layoutParams = params
                gravity = Gravity.CENTER
                setPadding(8, 8, 8, 8)
                setTextColor(ContextCompat.getColor(context, R.color.textColor))
                textSize = 10f
                setBackgroundColor(ContextCompat.getColor(context, R.color.background))
                typeface
            }

            if (i >= startDayOfWeek && i < startDayOfWeek + daysInMonth) {
                val dayOfMonth = i - startDayOfWeek + 1
                dayTextView.text = dayOfMonth.toString()

                val dayCalendar = calendar.clone() as Calendar
                dayCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val thisDay = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, monthIndex)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }

                val isPeriodDate = periodDates.any {
                    it.get(Calendar.YEAR) == dayCalendar.get(Calendar.YEAR) &&
                            it.get(Calendar.MONTH) == dayCalendar.get(Calendar.MONTH) &&
                            it.get(Calendar.DAY_OF_MONTH) == dayCalendar.get(Calendar.DAY_OF_MONTH)
                }

                val isToday = thisDay.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        thisDay.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                        thisDay.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

                if (isPeriodDate) {
                    dayTextView.setBackgroundResource(R.drawable.day_selected_background)
                }
                // Highlight styles
                when {
                    isPeriodDate -> {
                        dayTextView.setBackgroundResource(R.drawable.day_selected_background) // Define your bg drawable
                        dayTextView.setTextColor(ContextCompat.getColor(context, R.color.buttonTextColor))
                    }
                    isToday -> {
                        dayTextView.setBackgroundResource(R.drawable.bg_day_today) // Define today background
                        dayTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    }
                    else -> {
                        dayTextView.setBackgroundResource(R.drawable.bg_day_normal) // Default
                        dayTextView.setTextColor(ContextCompat.getColor(context, R.color.textColor))
                    }
                }
            } else {
                dayTextView.text = ""
            }

            holder.binding.gridDays.addView(dayTextView)
        }
    }
}