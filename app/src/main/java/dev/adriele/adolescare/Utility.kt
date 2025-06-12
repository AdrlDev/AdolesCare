package dev.adriele.adolescare

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import org.mindrot.jbcrypt.BCrypt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object Utility {
    interface TermsPrivacyClickListener {
        fun onTermsOfUseClicked()
        fun onPrivacyPolicyClicked()
    }

    interface LoginHereClickListener {
        fun onLoginClicked()
    }

    interface SignUpHereClickListener {
        fun onSignUpClicked()
    }

    interface OnDatePickedCallback {
        fun onDatePicked(formattedDate: String, computedResult: String)
    }

    object SecurityUtils {
        fun hashPasswordBcrypt(password: String): String {
            return BCrypt.hashpw(password, BCrypt.gensalt())
        }

        fun checkPassword(password: String, hashed: String): Boolean {
            return BCrypt.checkpw(password, hashed)
        }
    }

    object PreferenceManager {
        private const val PREF_NAME = "login_pref"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_REMEMBER_ME = "remember_me"

        fun saveLoginInfo(context: Context, username: String, password: String, remember: Boolean) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString(KEY_USERNAME, username)
                putString(KEY_PASSWORD, password) // Save hashed password if needed
                putBoolean(KEY_REMEMBER_ME, remember)
                apply()
            }
        }

        fun getSavedLoginInfo(context: Context): Triple<String?, String?, Boolean> {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val username = prefs.getString(KEY_USERNAME, null)
            val password = prefs.getString(KEY_PASSWORD, null)
            val remember = prefs.getBoolean(KEY_REMEMBER_ME, false)
            return Triple(username, password, remember)
        }

        fun clearLoginInfo(context: Context) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit { clear() }
        }
    }

    fun generateUID(): String {
        return UUID.randomUUID().toString()
    }

    fun setupTermOfUseText(tv: TextView, listener: TermsPrivacyClickListener) {
        val text = tv.text.toString()
        val spannable = SpannableString(text)

        // Tagalog equivalents (update if needed)
        val tagalogTerms = "Mga Tuntunin ng Paggamit"
        val tagalogPrivacy = "Patakaran sa Privacy"

        fun isTagalogPhrase(phrase: String): Boolean {
            return phrase == tagalogTerms || phrase == tagalogPrivacy
        }

        val termsStart = text.indexOf("Terms of use")
        val termsEnd = termsStart + "Terms of use".length

        val privacyStart = text.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length

        if (termsStart != -1 && !isTagalogPhrase("Terms of use")) {
            // Terms of use clickable span
            val termsClickable = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    listener.onTermsOfUseClicked()
                }
            }
            // Apply spans: clickable + bold + underline for Terms of use
            spannable.setSpan(termsClickable, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(Typeface.BOLD), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(UnderlineSpan(), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        if (privacyStart != -1 && !isTagalogPhrase("Privacy Policy")) {
            // Privacy Policy clickable span
            val privacyClickable = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    listener.onPrivacyPolicyClicked()
                }
            }
            // Apply spans: clickable + bold + underline for Privacy Policy
            spannable.setSpan(privacyClickable, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(Typeface.BOLD), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(UnderlineSpan(), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        tv.text = spannable
        tv.movementMethod = LinkMovementMethod.getInstance()
        tv.highlightColor = Color.TRANSPARENT // optional
    }

    fun setupAlreadyHaveAccountText(tv: TextView, listener: LoginHereClickListener) {
        val text = tv.text.toString()
        val spannable = SpannableString(text)

        // Tagalog equivalents (update if needed)
        val tagalogTerms = "Mag-log in dito"

        fun isTagalogPhrase(phrase: String): Boolean {
            return phrase == tagalogTerms
        }

        val loginStart = text.indexOf("Log in here")
        val loginEnd = loginStart + "Log in here".length

        if (loginStart != -1 && !isTagalogPhrase("Log in here")) {
            // Terms of use clickable span
            val termsClickable = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    listener.onLoginClicked()
                }
            }
            // Apply spans: clickable + bold + underline for Terms of use
            spannable.setSpan(termsClickable, loginStart, loginEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(Typeface.BOLD), loginStart, loginEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(UnderlineSpan(), loginStart, loginEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        tv.text = spannable
        tv.movementMethod = LinkMovementMethod.getInstance()
        tv.highlightColor = Color.TRANSPARENT // optional
    }

    fun setupDonatHaveAccountText(tv: TextView, listener: SignUpHereClickListener) {
        val text = tv.text.toString()
        val spannable = SpannableString(text)

        // Tagalog equivalents (update if needed)
        val tagalogTerms = "Mag-sign up dito"

        fun isTagalogPhrase(phrase: String): Boolean {
            return phrase == tagalogTerms
        }

        val loginStart = text.indexOf("Sign up here")
        val loginEnd = loginStart + "Sign up here".length

        if (loginStart != -1 && !isTagalogPhrase("Sign up here")) {
            // Terms of use clickable span
            val termsClickable = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    listener.onSignUpClicked()
                }
            }
            // Apply spans: clickable + bold + underline for Terms of use
            spannable.setSpan(termsClickable, loginStart, loginEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(Typeface.BOLD), loginStart, loginEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(UnderlineSpan(), loginStart, loginEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        tv.text = spannable
        tv.movementMethod = LinkMovementMethod.getInstance()
        tv.highlightColor = Color.TRANSPARENT // optional
    }

    fun setupDatePicker(editText: TextInputEditText, fragmentActivity: FragmentActivity, callback: OnDatePickedCallback) {
        editText.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select your birthday")
                .build()

            picker.show(fragmentActivity.supportFragmentManager, "DATE_PICKER")

            picker.addOnPositiveButtonClickListener { selection ->
                val date = Date(selection)
                val formatted = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)

                val age = computeAgeFromDate(date)
                callback.onDatePicked(formatted, age.toString())
            }
        }
    }

    fun setupDatePicker(btn: MaterialButton, title: String, fragmentActivity: FragmentActivity, callback: OnDatePickedCallback) {
        btn.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)
                .build()

            picker.show(fragmentActivity.supportFragmentManager, "DATE_PICKER")

            picker.addOnPositiveButtonClickListener { selection ->
                val date = Date(selection)
                val formatted = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)

                callback.onDatePicked(formatted, "")
            }
        }
    }

    private fun computeAgeFromDate(birthDate: Date): Int {
        val today = Calendar.getInstance()
        val birth = Calendar.getInstance().apply { time = birthDate }

        var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    fun showLoadingDialog(context: Context, message: String): Dialog {
        val dialog = Dialog(context)

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(48, 48, 48, 48)
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val progressBar = ProgressBar(context).apply {
            isIndeterminate = true
            indeterminateDrawable.setColorFilter(
                ContextCompat.getColor(context, R.color.buttonColor),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }

        val textView = TextView(context).apply {
            text = message
            setPadding(32, 0, 0, 0) // spacing between ProgressBar and text
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.textColor))
        }

        layout.addView(progressBar)
        layout.addView(textView)

        dialog.setContentView(layout)
        dialog.setCancelable(false)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.show()
        return dialog
    }

    fun showChangePasswordDialog(context: Context, onConfirm: (String, String) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_change_password, null)
        val usernameInput = dialogView.findViewById<TextInputEditText>(R.id.et_username)
        val newPasswordInput = dialogView.findViewById<TextInputEditText>(R.id.et_new_password)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Confirm", null) // Will override later for validation
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val confirmBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            confirmBtn.setOnClickListener {
                val username = usernameInput.text.toString()
                val newPassword = newPasswordInput.text.toString()

                if (username.isBlank() || newPassword.isBlank()) {
                    Toast.makeText(context, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
                } else {
                    onConfirm(username, newPassword)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    fun setupStepper(totalSteps: Int, resources: android.content.res.Resources, context: Context, stepper: LinearLayout) {
        val inactiveSize = resources.getDimensionPixelSize(R.dimen.stepper_dot_inactive_size)
        val margin = resources.getDimensionPixelSize(R.dimen.margin_small)

        for (i in 0 until totalSteps) {
            val dot = View(context).apply {
                Log.d("POSITION", i.toString())
                layoutParams = LinearLayout.LayoutParams(inactiveSize, inactiveSize).apply {
                    marginEnd = margin
                }
                setBackgroundResource(R.drawable.stepper_dot_inactive)
            }
            stepper.addView(dot)
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateStepper(currentStep: Int, totalSteps: Int, stepper: LinearLayout, count: TextView?, resources: android.content.res.Resources) {
        for (i in 0 until stepper.childCount) {
            val dot = stepper.getChildAt(i)

            if (i == currentStep) {
                // Animate the active dot: enlarge and change color
                animateDot(dot, active = true, resources)
            } else {
                // Animate the inactive dots: shrink and change color
                animateDot(dot, active = false, resources)
            }
        }
        count?.text = "Step ${currentStep + 1} of $totalSteps"
    }

    private fun animateDot(dot: View, active: Boolean, resources: android.content.res.Resources) {
        val activeSize = resources.getDimensionPixelSize(R.dimen.stepper_dot_active_size).toFloat()
        val inactiveSize = resources.getDimensionPixelSize(R.dimen.stepper_dot_inactive_size).toFloat()
        val targetSize = if (active) activeSize else inactiveSize // Active dot is larger
        val targetColor = if (active) "#0097DC".toColorInt() else "#989898".toColorInt()

        // Animate size change
        val sizeAnimator = ValueAnimator.ofFloat(dot.layoutParams.width.toFloat(), targetSize).apply {
            duration = 300 // Smooth animation duration
            addUpdateListener {
                val animatedSize = it.animatedValue as Float
                val layoutParams = dot.layoutParams
                layoutParams.width = animatedSize.toInt()
                layoutParams.height = animatedSize.toInt()
                dot.layoutParams = layoutParams
            }
        }

        val currentColor = ViewCompat.getBackgroundTintList(dot)?.defaultColor ?: Color.GRAY

        // Animate color change
        val colorAnimator = ObjectAnimator.ofArgb(
            dot.background.mutate(),
            "tint",
            currentColor,
            targetColor
        ).apply {
            duration = 300
        }

        // Start animations together
        AnimatorSet().apply {
            playTogether(sizeAnimator, colorAnimator)
            start()
        }
    }

    fun animateTypingDots(dot1: View, dot2: View, dot3: View) {
        val duration = 500L

        fun bounce(view: View, delay: Long) {
            ObjectAnimator.ofFloat(view, "translationY", 0f, -10f, 0f).apply {
                this.duration = duration
                this.startDelay = delay
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                start()
            }
        }

        bounce(dot1, 0)
        bounce(dot2, duration / 3)
        bounce(dot3, duration * 2 / 3)
    }

    fun getCurrentTime(): String {
        // Return current formatted date/time string, e.g. "10:45 AM"
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }

    @SuppressLint("SetTextI18n")
    fun TextView.animateTypingWithCursor(
        fullText: String,
        typingDelay: Long = 100L,
        cursorBlinkDelay: Long = 500L,
        showCursor: Boolean = true,
        onTypingComplete: (() -> Unit)? = null
    ): Job {
        var isCursorVisible = true
        text = ""
        val job = Job()
        val scope = CoroutineScope(Dispatchers.Main + job)

        scope.launch {
            // Typing animation
            for (i in fullText.indices) {
                text = fullText.substring(0, i + 1) + if (showCursor) "_" else ""
                delay(typingDelay)
            }

            onTypingComplete?.invoke() // ✅ Call after typing finishes

            if (showCursor) {
                // Blinking cursor loop
                while (isActive) {
                    text = if (isCursorVisible) "${fullText}_" else fullText
                    isCursorVisible = !isCursorVisible
                    delay(cursorBlinkDelay)
                }
            }
        }

        return job
    }
}