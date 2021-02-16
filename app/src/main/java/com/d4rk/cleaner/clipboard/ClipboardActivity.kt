package com.d4rk.cleaner.clipboard
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.d4rk.cleaner.R
import kotlinx.android.synthetic.main.activity_clipboard.*
import kotlinx.android.synthetic.main.activity_main.*
import java.text.NumberFormat
class ClipboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clipboard)
        AppCompatDelegate.setDefaultNightMode(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                } else {
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                }
        )
        setUpButtons()
        setUpService()
        setUpShortcut()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            cardService.visibility = View.GONE
        }
    }
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>, grantResults: IntArray
    ) {
        if (permissions.isNotEmpty() && permissions[0] == Manifest.permission.INSTALL_SHORTCUT &&
                grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            toast(R.string.clipboard_shortcut_have_permission)
        } else {
            toast(R.string.clipboard_shortcut_no_permission)
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
    private fun setUpButtons() {
        btnClean.setOnClickListener {
            clean()
        }
        btnContent.setOnClickListener {
            content()
        }
    }
    private fun setUpService() {
        fun updateServiceStatus(started: Boolean) {
            if (started) {
                textServiceStatus.text = getString(R.string.clipboard_status)
                        .format(getString(R.string.clipboard_status_running))
                btnServiceStart.text = getString(R.string.clipboard_service_stop)
            } else {
                textServiceStatus.text = getString(R.string.clipboard_status)
                        .format(getString(R.string.clipboard_status_stopped))
                btnServiceStart.text = getString(R.string.clipboard_service_start)
            }
        }
        val serviceOption = CleanService.getServiceOption(this)
        if (serviceOption == CleanService.SERVICE_OPTION_CLEAN) {
            radioBtnClean.isChecked = true
        } else if (serviceOption == CleanService.SERVICE_OPTION_CONTENT) {
            radioBtnReport.isChecked = true
        }
        groupServiceOptions.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioBtnClean) {
                CleanService.setServiceOption(this, CleanService.SERVICE_OPTION_CLEAN)
            } else if (checkedId == R.id.radioBtnReport) {
                CleanService.setServiceOption(this, CleanService.SERVICE_OPTION_CONTENT)
            }
        }
        val isServiceRunning = CleanService.isServiceRunning(this)
        updateServiceStatus(isServiceRunning)
        CleanService.setServiceStarted(this, isServiceRunning)
        btnServiceStart.setOnClickListener {
            if (CleanService.getServiceStarted(this@ClipboardActivity)) {
                CleanService.stop(this@ClipboardActivity)
                updateServiceStatus(false)
            } else {
                CleanService.start(this@ClipboardActivity)
                updateServiceStatus(true)
            }
        }
        fun updateCleanTimeoutText() {
            val timeout = serviceCleanTimeout
            textServiceCleanTimeout.text =
                    getString(R.string.clipboard_service_clean_timeout_template).format(
                            resources.getQuantityString(
                                    R.plurals.seconds,
                                    timeout,
                                    NumberFormat.getInstance().format(timeout)
                            )
                    )
        }
        updateCleanTimeoutText()
        textServiceCleanTimeout.setOnClickListener {
            requestInput(
                    R.string.clipboard_service_clean_timeout,
                    InputType.TYPE_CLASS_NUMBER
            ) {
                serviceCleanTimeout = it.toIntOrNull() ?: 0
                updateCleanTimeoutText()
            }
        }
    }
    @SuppressLint("InlinedApi")
    private fun setUpShortcut() {
        checkKeyword.setOnCheckedChangeListener { _, isChecked ->
            setUsingKeyword(isChecked)
            layoutKeywordSetting.visibility = if (isChecked) View.VISIBLE else View.GONE
            layoutMainScroll.postDelayed({
            }, 300)
        }
        fun ViewGroup.addKeywordView(keyword: String): View {
            val view = layoutInflater.inflate(R.layout.item_keyword, this, false)
            view.findViewById<TextView>(R.id.textKeywordContent).text = keyword
            view.findViewById<ImageButton>(R.id.imageKeywordRemove).run {
                setOnClickListener {
                    this@addKeywordView.removeView(view)
                }
                contentDescription = getString(R.string.clipboard_setting_keyword_remove).format(keyword)
            }
            addView(view)
            return view
        }
        fun LinearLayout.getKeywords(): Set<String> {
            val keywords = mutableListOf<String>()
            (0..childCount).forEach {
                getChildAt(it)?.findViewById<TextView>(R.id.textKeywordContent)
                        ?.text?.toString()?.let { keyword ->
                            keywords.add(keyword)
                        }
            }
            return keywords.toSet()
        }
        getNormalKeywords().forEach {
            layoutKeywordNormal.addKeywordView(it)
        }
        btnKeywordAddNormal.setOnClickListener {
            requestInput(R.string.clipboard_setting_keyword_normal_title) {
                layoutKeywordNormal.addKeywordView(it)
            }
        }
        getRegexKeywords().forEach {
            layoutKeywordRegex.addKeywordView(it)
        }
        btnKeywordAddRegex.setOnClickListener {
            requestInput(R.string.clipboard_setting_keyword_normal_title) {
                layoutKeywordRegex.addKeywordView(it)
            }
        }
        btnKeywordSave.setOnClickListener {
            setNormalKeywords(layoutKeywordNormal.getKeywords())
            setRegexKeywords(layoutKeywordRegex.getKeywords())
            toast(R.string.clipboard_setting_message_saved)
        }
    }
}