package dev.adriele.adolescare.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import dev.adriele.adolescare.fragments.PdfChaptersFragment
import dev.adriele.adolescare.fragments.PreviewPdfFragment
import dev.adriele.adolescare.helpers.contracts.OnUserInteractionListener

class PdfViewerPagerAdapter(
    activity: FragmentActivity,
    private val pdfUri: String,
    private val pdfCategory: String,
    private val userInteractionListener: OnUserInteractionListener
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PreviewPdfFragment.newInstance(pdfUri, userInteractionListener)
            1 -> PdfChaptersFragment.newInstance(pdfCategory)
            else -> PreviewPdfFragment.newInstance(pdfUri, userInteractionListener)
        }
    }
}
