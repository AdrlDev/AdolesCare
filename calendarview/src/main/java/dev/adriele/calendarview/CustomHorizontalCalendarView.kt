package dev.adriele.calendarview

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.adriele.calendarview.adapters.CalendarAdapter
import java.util.*

class CustomHorizontalCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val adapter = CalendarAdapter()

    init {
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        setAdapter(adapter)
        updateWeek(Calendar.getInstance())
    }

    fun updateWeek(referenceDate: Calendar) {
        val weekDates = mutableListOf<Calendar>()
        val temp = referenceDate.clone() as Calendar

        // Set to start of the week (Sunday)
        temp.set(Calendar.DAY_OF_WEEK, temp.firstDayOfWeek)

        repeat(7) {
            val date = temp.clone() as Calendar
            weekDates.add(date)
            temp.add(Calendar.DAY_OF_MONTH, 1)
        }

        adapter.submitList(weekDates)
    }

    fun refreshToToday() {
        updateWeek(Calendar.getInstance())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        post {
            val layoutManager = layoutManager as? LinearLayoutManager ?: return@post
            val adapter = adapter

            // Measure total item width
            var totalItemWidth = 0
            for (i in 0 until adapter.itemCount) {
                val holder = adapter.createViewHolder(this, adapter.getItemViewType(i))
                adapter.onBindViewHolder(holder, i)

                holder.itemView.measure(
                    MeasureSpec.UNSPECIFIED,
                    MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.AT_MOST)
                )

                totalItemWidth += holder.itemView.measuredWidth
            }

            if (totalItemWidth <= measuredWidth) {
                // All items fit → disable scroll
                layoutManager.gravityCenter()
                overScrollMode = OVER_SCROLL_NEVER
                isNestedScrollingEnabled = false
                layoutManager.isItemPrefetchEnabled = false
            } else {
                layoutManager.gravityStart()
                // Items exceed view → allow scroll
                overScrollMode = OVER_SCROLL_ALWAYS
                isNestedScrollingEnabled = true
            }
        }
    }

    fun LinearLayoutManager.gravityCenter() {
        this.stackFromEnd = false
        this.reverseLayout = false
        this.isItemPrefetchEnabled = false
        this.scrollToPositionWithOffset(0, ((width - computeTotalItemWidth()) / 2))
    }

    fun LinearLayoutManager.gravityStart() {
        this.scrollToPositionWithOffset(0, 0)
    }

    fun RecyclerView.computeTotalItemWidth(): Int {
        val adapter = adapter ?: return 0
        var totalWidth = 0
        for (i in 0 until adapter.itemCount) {
            val holder = adapter.createViewHolder(this, adapter.getItemViewType(i))
            adapter.onBindViewHolder(holder, i)
            holder.itemView.measure(0, 0)
            totalWidth += holder.itemView.measuredWidth
        }
        return totalWidth
    }

}