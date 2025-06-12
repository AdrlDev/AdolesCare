package dev.adriele.adolescare.authentication.adapter

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private val fragments = mutableListOf<Fragment>()
    private val fragmentIds = mutableListOf<Long>()

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]

    override fun getItemId(position: Int): Long {
        // Unique ID for each fragment instance
        return fragmentIds.getOrElse(position) { position.toLong() }
    }

    override fun containsItem(itemId: Long): Boolean {
        return fragmentIds.contains(itemId)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateFragments(newFragments: List<Fragment>) {
        fragments.clear()
        fragments.addAll(newFragments)

        // Update the fragment IDs to force recreation
        fragmentIds.clear()
        fragmentIds.addAll(newFragments.map { System.nanoTime() }) // Or use hash or index

        notifyDataSetChanged()
    }
}