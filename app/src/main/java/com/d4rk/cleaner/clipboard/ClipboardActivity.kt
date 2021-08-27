package com.d4rk.cleaner.clipboard
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.provider.Settings
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.d4rk.cleaner.R
import com.d4rk.cleaner.databinding.ActivityClipboardBinding
import java.text.NumberFormat
class ClipboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClipboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClipboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!isNOrLater()) {
            binding.cardTile.visibility = View.GONE
        }
        setUpButtons()
        setUpService()
        setUpShortcut()
        setUpAssistant()
        setUpSetting()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.cardService.isGone = true
            val showServerCardKey = "show_service_card"
            val sp = getSafeSharedPreference()
            if (sp.getBoolean(showServerCardKey, false)) {
                binding.cardService.isVisible = true
            } else {
                var times = 0
                binding.imageTitle.setOnClickListener {
                    if (++times == 7) {
                        sp.edit {
                            putBoolean(showServerCardKey, true)
                        }
                        binding.cardService.isVisible = true
                    }
                }
            }
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
        binding.btnClean.setOnClickListener {
            clean()
        }
        binding.btnContent.setOnClickListener {
            content()
        }
    }
    private fun setUpService() {
        fun updateServiceStatus(started: Boolean) {
            if (started) {
                binding.textServiceStatus.text = getString(R.string.clipboard_status)
                    .format(getString(R.string.clipboard_status_running))
                binding.btnServiceStart.text = getString(R.string.clipboard_status_stopped)
            } else {
                binding.textServiceStatus.text = getString(R.string.clipboard_status_running)
                    .format(getString(R.string.clipboard_status_stopped))
                binding.btnServiceStart.text = getString(R.string.clipboard_service_start)
            }
        }
        val serviceOption = CleanService.getServiceOption(this)
        if (serviceOption == CleanService.SERVICE_OPTION_CLEAN) {
            binding.radioBtnClean.isChecked = true
        } else if (serviceOption == CleanService.SERVICE_OPTION_CONTENT) {
            binding.radioBtnReport.isChecked = true
        }
        binding.groupServiceOptions.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioBtnClean) {
                CleanService.setServiceOption(this, CleanService.SERVICE_OPTION_CLEAN)
            } else if (checkedId == R.id.radioBtnReport) {
                CleanService.setServiceOption(this, CleanService.SERVICE_OPTION_CONTENT)
            }
        }
        val isServiceRunning = CleanService.isServiceRunning(this)
        updateServiceStatus(isServiceRunning)
        CleanService.setServiceStarted(this, isServiceRunning)
        binding.btnServiceStart.setOnClickListener {
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
            binding.textServiceCleanTimeout.text =
                getString(R.string.clipboard_service_clean_timeout_template).format(
                    resources.getQuantityString(
                        R.plurals.seconds,
                        timeout,
                        NumberFormat.getInstance().format(timeout)
                    )
                )
        }
        updateCleanTimeoutText()
        binding.textServiceCleanTimeout.setOnClickListener {
            requestInput(
                R.string.clipboard_service_clean_timeout,
                InputType.TYPE_CLASS_NUMBER
            ) {
                serviceCleanTimeout = it.toIntOrNull() ?: 0
                updateCleanTimeoutText()
            }
        }
    }
    private fun setUpShortcut() {
        fun checkAndRequestShortcutPermission(): Boolean {
            return if (!isOOrLater() && isKitkatOrLater() &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.INSTALL_SHORTCUT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.INSTALL_SHORTCUT), 0
                )
                false
            } else {
                true
            }
        }
        binding.btnShortcutClean.setOnClickListener {
            if (checkAndRequestShortcutPermission()) {
                createCleanShortcut()
            }
        }
        binding.btnShortcutContent.setOnClickListener {
            if (checkAndRequestShortcutPermission()) {
                createContentShortcut()
            }
        }
    }
    private fun setUpAssistant() {

        binding.btnOpenAssistantSettings.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))
            } catch (e: Exception) {
                // Ignore
            }
        }

        if (assistantAction == ACTION_CLEAN) {
            binding.ratioAssistantClean.isChecked = true
        } else {
            binding.ratioAssistantContent.isChecked = true
        }
        binding.ratioGroupAssistant.setOnCheckedChangeListener { _, checkedId ->
            assistantAction =
                if (checkedId == R.id.ratioAssistantClean) ACTION_CLEAN else ACTION_CONTENT
        }
    }
    private fun setUpSetting() {
        if (getUsingKeyword()) {
            binding.checkKeyword.isChecked = true
            binding.layoutKeywordSetting.visibility = View.VISIBLE
        }
        binding.checkKeyword.setOnCheckedChangeListener { _, isChecked ->
            setUsingKeyword(isChecked)
            binding.layoutKeywordSetting.visibility = if (isChecked) View.VISIBLE else View.GONE
            binding.layoutMainScroll.postDelayed({
                binding.layoutMainScroll.fullScroll(View.FOCUS_DOWN)
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
            binding.layoutKeywordNormal.addKeywordView(it)
        }
        binding.btnKeywordAddNormal.setOnClickListener {
            requestInput(R.string.clipboard_setting_keyword_normal_title) {
                binding.layoutKeywordNormal.addKeywordView(it)
            }
        }
        getRegexKeywords().forEach {
            binding.layoutKeywordRegex.addKeywordView(it)
        }
        binding.btnKeywordAddRegex.setOnClickListener {
            requestInput(R.string.clipboard_setting_keyword_regex_title) {
                binding.layoutKeywordRegex.addKeywordView(it)
            }
        }
        binding.btnKeywordSave.setOnClickListener {
            setNormalKeywords(binding.layoutKeywordNormal.getKeywords())
            setRegexKeywords(binding.layoutKeywordRegex.getKeywords())
            toast(R.string.clipboard_setting_message_saved)
        }
    }
}