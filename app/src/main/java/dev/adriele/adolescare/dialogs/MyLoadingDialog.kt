package dev.adriele.adolescare.dialogs

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.Window
import android.widget.FrameLayout
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView
import dev.adriele.adolescare.R

class MyLoadingDialog(private val context: Context) {
    private var dialog: Dialog? = null

    fun show(message: String = "Loading...") {
        // Create and set up the dialog
        dialog = Dialog(context)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false) // Prevent user from dismissing

        // Inflate custom layout for the progress indicator
        val view = LayoutInflater.from(context).inflate(
            R.layout.dialog_progress,
            FrameLayout(context),
            false
        )
        val messageText = view.findViewById<MaterialTextView>(R.id.progress_message)
        val progressIndicator =
            view.findViewById<CircularProgressIndicator>(R.id.circular_progress)

        messageText.text = message
        progressIndicator.isIndeterminate = true

        // Set the custom view
        dialog?.setContentView(view)
        // Make background transparent
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
    }
}