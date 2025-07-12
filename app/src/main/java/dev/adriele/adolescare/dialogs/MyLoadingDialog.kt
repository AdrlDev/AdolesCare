package dev.adriele.adolescare.dialogs

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.WindowManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView
import dev.adriele.adolescare.R

class MyLoadingDialog(private val context: Context) {
    private var dialog: Dialog? = null

    fun show(message: String = "Loading...") {
        if (dialog?.isShowing == true) return

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null)
        val messageText = view.findViewById<MaterialTextView>(R.id.progress_message)
        val progressIndicator = view.findViewById<CircularProgressIndicator>(R.id.circular_progress)

        messageText.text = message
        progressIndicator.isIndeterminate = true

        dialog = MaterialAlertDialogBuilder(context)
            .setView(view)
            .setCancelable(false)
            .create()

        // Make dialog background transparent
        dialog?.setOnShowListener {
            val window = dialog?.window
            window?.setBackgroundDrawableResource(android.R.color.transparent)

            // Set width to half of screen
            val metrics = context.resources.displayMetrics
            val width = (metrics.widthPixels * 0.5).toInt() // 50% of screen width
            window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }
        dialog?.show()
    }

    fun dismiss() {
        if (dialog?.isShowing == true) {
            dialog?.dismiss()
        }
    }
}