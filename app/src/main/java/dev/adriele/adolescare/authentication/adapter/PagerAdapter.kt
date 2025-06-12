package dev.adriele.adolescare.authentication.adapter

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private val fragments = mutableListOf<Fragment>()

    override fun getItemCount(): Int = fragments.size // Number of pages

    override fun createFragment(position: Int): Fragment = fragments[position]

    @SuppressLint("NotifyDataSetChanged")
    fun updateFragments(newFragments: List<Fragment>) {
        fragments.clear()
        fragments.addAll(newFragments)
        notifyDataSetChanged()
    }
}