package dev.adriele.adolescare.helpers

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dev.adriele.adolescare.helpers.enums.ModuleContentType
import dev.adriele.adolescare.helpers.enums.PDFModulesCategory
import dev.adriele.adolescare.R
import dev.adriele.adolescare.database.entities.LearningModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import androidx.core.graphics.createBitmap
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

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
            val constraints = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now()) // only allow past dates
                .build()

            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select your birthday")
                .setCalendarConstraints(constraints)
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

    fun setupDatePicker(btn: MaterialButton, title: String, disablePastDate: Boolean, fragmentActivity: FragmentActivity, callback: OnDatePickedCallback) {
        btn.setOnClickListener {
            val builder = MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)

            val today = MaterialDatePicker.todayInUtcMilliseconds()

            if (disablePastDate) {
                // Only allow dates from today onwards
                builder.setSelection(today)
                builder.setCalendarConstraints(
                    CalendarConstraints.Builder()
                        .setStart(today)
                        .build()
                )
            } else {
                // Only allow past and today’s date
                builder.setSelection(today)
                builder.setCalendarConstraints(
                    CalendarConstraints.Builder()
                        .setEnd(today)
                        .build()
                )
            }

            val picker = builder.build()
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

    fun setupStepper(totalSteps: Int, resources: Resources, context: Context, stepper: LinearLayout) {
        val inactiveSize = resources.getDimensionPixelSize(R.dimen.stepper_dot_inactive_size)
        val margin = resources.getDimensionPixelSize(R.dimen.margin_small)

        stepper.removeAllViews() // ✅ Clear existing dots before adding new ones

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
    fun updateStepper(currentStep: Int, totalSteps: Int, stepper: LinearLayout, count: TextView?, resources: Resources) {
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

    private fun animateDot(dot: View, active: Boolean, resources: Resources) {
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
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentCycleDate(): String {
        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentDateOnly(): String {
        val sdf = SimpleDateFormat("MMMM dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault())
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

    fun getTwoWeeksAgo(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -14) // Subtract 14 days
        val twoWeeksAgo = calendar.time

        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(twoWeeksAgo)
    }

    fun copyAssetToCache(context: Context, assetPath: String): File? {
        val file = File(context.cacheDir, File(assetPath).name)

        if (!file.exists()) {
            context.assets.open(assetPath).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }

        return file
    }

    fun generatePdfThumbnail(file: File, pageIndex: Int = 0): Bitmap? {
        if (!file.exists()) return null

        val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(fileDescriptor)
        val page = pdfRenderer.openPage(pageIndex)

        val width = page.width * 3
        val height = page.height * 3
        val bitmap = createBitmap(width, height)

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        page.close()
        pdfRenderer.close()
        fileDescriptor.close()

        return bitmap
    }

    fun enableFullscreen(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30+
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // API 24 to 29
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    fun loadLearningModules(context: Context): List<LearningModule> {
        val modules = mutableListOf<LearningModule>()
        val assetManager = context.assets
        val basePath = "modules/pdf"

        val folders = assetManager.list(basePath) ?: return emptyList()

        for (folder in folders) {
            val files = assetManager.list("$basePath/$folder")?.filter { it.endsWith(".pdf") } ?: continue

            for ((index, file) in files.withIndex()) {
                val title = file.removeSuffix(".pdf").replace('_', ' ').replaceFirstChar { it.uppercaseChar() }
                val enumCategory = PDFModulesCategory.entries.find {
                    it.name.equals(folder.uppercase().replace("-", "_"), ignoreCase = true)
                }?.category ?: folder.replace('_', ' ')

                val orderBy = if (title.equals(enumCategory, ignoreCase = true)) 0 else index + 1

                modules.add(
                    LearningModule(
                        id = "${folder}_$index",
                        title = title,
                        category = enumCategory,
                        contentType = ModuleContentType.PDF,
                        contentUrl = "$basePath/$folder/$file",
                        orderBy = orderBy
                    )
                )
            }
        }

        return modules
    }

    fun loadLearningVideos(context: Context): List<LearningModule> {
        val modules = mutableListOf<LearningModule>()
        val assetManager = context.assets
        val basePath = "modules/videos"

        val files = assetManager.list(basePath)?.filter { it.endsWith(".mp4") } ?: return emptyList()

        for ((index, file) in files.withIndex()) {
            val title = file.removeSuffix(".mp4")
            val formattedTitle = title.replace('_', ' ').replaceFirstChar { it.uppercaseChar() }
            val creditLink = videoCredits[title]

            modules.add(
                LearningModule(
                    id = "video_$index",
                    title = formattedTitle,
                    category = "Learning Videos",
                    contentType = ModuleContentType.VIDEO,
                    contentUrl = "$basePath/$file",
                    contentCreditsUrl = creditLink,
                    orderBy = index
                )
            )
        }

        return modules
    }

    private val videoCredits = mapOf(
        "how_do_contraceptives_work" to "https://youtu.be/Zx8zbTMTncs?si=9VEENxtA2XxJVaKK",
        "contraceptives_101" to "https://youtu.be/KyU880oHSxM?si=GA28M1_27hObceBg",
        "how_to_decide_which_birth_control_is_right_for_you" to "https://youtu.be/aWUJ3J0EV0U?si=NWY58R5299DbsWS1",
        "tips_for_safer_sex_and_pregnancy_prevention" to "https://youtu.be/fhl6SFBHgcs?si=lJzgI1qbg3SVzAbo",
        "what_is_emergency_contraception_the_morning_after_pill" to "https://youtu.be/Z1LTban3Z84?si=GPx8PW33nrjRciKp",
        "first_time_ganito_po_ang_tamang_paginom_ng_pills" to "https://youtu.be/-D0d14Tpa0A?si=1J6lsOm4uA8QS5hM",
        "paano_ang_tamang_paraan_ng_paggamit_ng_condom" to "https://youtu.be/wwk2RlpE-1Q?si=Y44kEOe3aSqf3P2b",
        "ito_ang_natural_at_epektibong_contraceptive_method" to "https://youtu.be/hEyjMgQsJaw?si=SOkKXcMIk3AHep5F",
        "bakit_pinipili_ng_ibang_kababaihan_ang_iud" to "https://youtu.be/7KQ6wKRgdlY?si=DYNxnLwPWAYJiGHc",
        "depo_injectable_o_dmpa_ano_nga_ba_ito" to "https://youtu.be/cQApY1AFAvU?si=q4Tn_cE-AiWJMEHK",
        "teenager_palang_ako_pwede_bang_gumamit_ng_contraception" to "https://youtu.be/njlFlmZ7PSs?si=3MZAhk8vMae0iC-y",
        "ano_po_ang_side_effects_ng_injectables_o_depo" to "https://youtu.be/X9Zg3aGCVSc?si=8fdOp6FWj4LNismn",
        "masakit_ba_magpalagay_ng_implant" to "https://youtu.be/ouzDUzYgk5g?si=0QurLUHSbXb-ZYIy"
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun buildTextIndexViaOcr(file: File): Map<Int, String> {
        val index = mutableMapOf<Int, String>()

        val renderer = PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val bitmap = createBitmap(page.width, page.height)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            val result = suspendCancellableCoroutine<String> { cont ->
                recognizer.process(image)
                    .addOnSuccessListener { cont.resume(it.text, null) }
                    .addOnFailureListener { cont.resumeWith(Result.success("")) }
            }

            index[i] = result
        }

        renderer.close()
        return index
    }

    fun findPagesWithText(query: String, index: Map<Int, String>): List<Int> {
        val lower = query.lowercase()
        return index.filter { it.value.lowercase().contains(lower) }.map { it.key }
    }

    fun getFileFromUri(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri) ?: throw IOException("Unable to open URI")
        val file = File(context.cacheDir, "temp_pdf_for_search.pdf")
        inputStream.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    fun saveOcrCache(context: Context, fileName: String, index: Map<Int, String>) {
        val gson = com.google.gson.Gson()
        val json = gson.toJson(index)
        val outFile = File(context.cacheDir, "ocr_$fileName.json")
        outFile.writeText(json)
    }

    fun loadOcrCache(context: Context, fileName: String): Map<Int, String>? {
        val gson = com.google.gson.Gson()
        val file = File(context.cacheDir, "ocr_$fileName.json")
        return if (file.exists()) {
            val json = file.readText()
            gson.fromJson(json, object : TypeToken<Map<Int, String>>() {}.type)
        } else null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun generateHighlightedBitmap(
        file: File,
        pageIndex: Int,
        query: String
    ): Bitmap = withContext(Dispatchers.IO) {
        val renderer = PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
        val page = renderer.openPage(pageIndex)

        val bitmap = createBitmap(page.width, page.height)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        renderer.close()

        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.YELLOW
            alpha = 120
        }

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val inputImage = InputImage.fromBitmap(bitmap.copy(Bitmap.Config.ARGB_8888, true), 0)

        val visionText = suspendCancellableCoroutine<com.google.mlkit.vision.text.Text> { cont ->
            recognizer.process(inputImage)
                .addOnSuccessListener { cont.resume(it, null) }
                .addOnFailureListener { cont.resumeWith(Result.failure(it)) }
        }

        visionText.textBlocks.forEach { block ->
            if (block.text.contains(query, ignoreCase = true)) {
                block.boundingBox?.let { canvas.drawRect(it, paint) }
            }
        }

        bitmap
    }

    fun getPdfPageCount(context: Context, assetPath: String): Int {
        return try {
            val file = copyAssetToCache(context, assetPath)
            val renderer = PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
            val pageCount = renderer.pageCount
            renderer.close()
            pageCount
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

}