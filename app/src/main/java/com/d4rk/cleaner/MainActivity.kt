package com.d4rk.cleaner
import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.d4rk.cleaner.clipboard.ClipboardActivity
import com.d4rk.cleaner.databinding.ActivityMainBinding
import com.d4rk.cleaner.invalid.ui.InvalidActivity
import com.google.android.material.navigation.NavigationView
import dev.shreyaspatil.MaterialDialog.MaterialDialog
import dev.shreyaspatil.MaterialDialog.interfaces.DialogInterface
import java.io.File
import java.text.DecimalFormat
import java.util.*
class MainActivity : AppCompatActivity() {
    private var drawerLayout: DrawerLayout? = null
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    private var navigationView: NavigationView? = null
    val context: MainActivity = this
    private var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.cleanBtn.setOnClickListener { clean() }
        binding!!.analyzeBtn.setOnClickListener { analyze() }
        WhitelistActivity.getWhiteList(prefs)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        setUpToolbar()
        navigationView = findViewById(R.id.navigation_view)
        @SuppressLint("RestrictedApi") val shortcut = ShortcutInfoCompat.Builder(context, "atm_shortcut")
            .setShortLabel(getString(R.string.atmegame))
            .setLongLabel(getString(R.string.long_shortcut_atmegame))
            .setIcon(IconCompat.createFromIcon(Icon.createWithResource(context, R.mipmap.ic_launch_atmegame)))
            .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/d4rkcleaneratm")))
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener { MenuItem: MenuItem ->
            val id = MenuItem.itemId
            if (id == R.id.nav_drawer_settings) {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            if (id == R.id.nav_drawer_whitelist) {
                startActivity(Intent(this, WhitelistActivity::class.java))
            }
            if (id == R.id.nav_drawer_clipboard_cleaner) {
                startActivity(Intent(this, ClipboardActivity::class.java))
            }
            if (id == R.id.nav_drawer_invalid_media_cleaner) {
                startActivity(Intent(this, InvalidActivity::class.java))
            }
            if (id == R.id.nav_drawer_about) {
                startActivity(Intent(this, AboutActivity::class.java))
            }
            if (id == R.id.nav_drawer_atmegame) {
                val openURL = Intent(Intent.ACTION_VIEW)
                openURL.data =
                    Uri.parse("https://bit.ly/d4rkcleaneratm")
                startActivity(openURL)
            }
            if (id == R.id.nav_drawer_share) {
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "Check this app"
                val shareBody = "https://bit.ly/d4rkcleaner"
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.try_right_now)
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                startActivity(Intent.createChooser(sharingIntent, "Share using..."))
            }
            false
        }
    }
    fun analyze() {
        requestWriteExternalPermission()
        if (!FileScanner.isRunning) {
            Thread { scan(false) }.start()
        }
    }
    private fun arrangeViews(isDelete: Boolean) {
        if (isDelete) arrangeForClean() else arrangeForAnalyze()
    }
    private fun arrangeForClean() {
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding!!.frameLayout.visibility = View.VISIBLE
            binding!!.fileScrollView.visibility = View.GONE
        }
    }
    private fun arrangeForAnalyze() {
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding!!.frameLayout.visibility = View.GONE
            binding!!.fileScrollView.visibility = View.VISIBLE
        }
    }
    fun clean() {
        requestWriteExternalPermission()
        if (!FileScanner.isRunning) {
            if (prefs == null) println("press is null")
            if (prefs!!.getBoolean("one_click", false)) {
                Thread { scan(true) }.start()
            } else {
                val mDialog = MaterialDialog.Builder(this)
                    .setTitle(getString(R.string.clean_confirm_title))
                    .setAnimation(R.raw.delete)
                    .setMessage(getString(R.string.are_you_sure_deletion))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.clean)) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss()
                        Thread { scan(true) }.start()
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
                    .build()
                mDialog.animationView.scaleType = ImageView.ScaleType.FIT_CENTER
                mDialog.show()
            }
        }
    }
    private fun clearClipboard() {
        try {
            val mCbm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mCbm.clearPrimaryClip()
            } else {
                val clipService = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("", "")
                clipService.setPrimaryClip(clipData)
            }
        } catch (e: NullPointerException) {
            runOnUiThread {
                Toast.makeText(this, R.string.clipboard_clean_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setUpToolbar() {
        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.empty, R.string.empty)
        actionBarDrawerToggle!!.syncState()
    }
    fun adflylink(view: View) {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse("http://anthargo.com/9cUM")
        startActivity(openURL)
    }
    @SuppressLint("SetTextI18n")
    private fun scan(delete: Boolean) {
        Looper.prepare()
        runOnUiThread {
            findViewById<View>(R.id.cleanBtn).isEnabled = !FileScanner.isRunning
            findViewById<View>(R.id.analyzeBtn).isEnabled = !FileScanner.isRunning
        }
        reset()
        if (prefs!!.getBoolean("clipboard", false)) clearClipboard()
        runOnUiThread {
            arrangeViews(delete)
            binding!!.statusTextView.text = getString(R.string.main_status_running)
        }
        val path = Environment.getExternalStorageDirectory()
        val fs = FileScanner(path, this)
            .setEmptyDir(prefs!!.getBoolean("empty", false))
            .setAutoWhite(prefs!!.getBoolean("auto_white", true))
            .setDelete(delete)
            .setCorpse(prefs!!.getBoolean("corpse", false))
            .setGUI(binding)
            .setContext(this)
            .setUpFilters(
                prefs!!.getBoolean("generic", true),
                prefs!!.getBoolean("aggressive", false),
                prefs!!.getBoolean("true_aggressive", false),
                prefs!!.getBoolean("apk", false)
            )
        if (path.listFiles() == null) {
            val textView = printTextView(getString(R.string.clipboard_clean_failed), Color.RED)
            runOnUiThread { binding!!.fileListView.addView(textView) }
        }
        val kilobytesTotal = fs.startScan()
        runOnUiThread {
            if (delete) binding!!.statusTextView.text = getString(R.string.main_freed) + " " + convertSize(kilobytesTotal) else binding!!.statusTextView.text = getString(R.string.main_found) + " " + convertSize(kilobytesTotal)
            binding!!.scanProgress.progress = binding!!.scanProgress.max
            binding!!.scanTextView.setText(R.string.main_progress_100)
        }
        binding!!.fileScrollView.post { binding!!.fileScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
        runOnUiThread {
            findViewById<View>(R.id.cleanBtn).isEnabled = !FileScanner.isRunning
            findViewById<View>(R.id.analyzeBtn).isEnabled = !FileScanner.isRunning
        }
        Looper.loop()
    }
    private fun printTextView(text: String, color: Int): TextView {
        val textView = TextView(this@MainActivity)
        textView.setTextColor(color)
        textView.text = text
        textView.setPadding(3, 3, 3, 3)
        return textView
    }
    fun displayDeletion(file: File): TextView {
        val textView = printTextView(file.absolutePath, ContextCompat.getColor(context, R.color.colorAccent))
        runOnUiThread { binding!!.fileListView.addView(textView) }
        binding!!.fileScrollView.post { binding!!.fileScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
        return textView
    }
    fun displayText(text: String) {
        val textView = printTextView(text, ContextCompat.getColor(context, R.color.colorGoogleYellow))
        runOnUiThread { binding!!.fileListView.addView(textView) }
        binding!!.fileScrollView.post { binding!!.fileScrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }
    private fun reset() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        runOnUiThread {
            binding!!.fileListView.removeAllViews()
            binding!!.scanProgress.progress = 0
            binding!!.scanProgress.max = 1
        }
    }
    private fun requestWriteExternalPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE
                ), 1
            )
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, R.string.permission_needed, Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            if (!isAccessGranted) {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                startActivity(intent)
            }
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), 1
            )
        }
    }
    private val isAccessGranted: Boolean
        get() = try {
            val packageManager = packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val appOpsManager = getSystemService(APP_OPS_SERVICE) as AppOpsManager
            val mode: Int = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo.uid, applicationInfo.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) prompt()
    }
    private fun prompt() {
        val intent = Intent(this, PromptActivity::class.java)
        startActivity(intent)
    }
    companion object {
        @JvmField
        var prefs: SharedPreferences? = null
        @JvmStatic
        fun convertSize(length: Long): String {
            val format = DecimalFormat("#.##")
            val mib = (1024 * 1024).toLong()
            val kib: Long = 1024
            if (length > mib) {
                return format.format(length / mib) + " MB"
            }
            return if (length > kib) {
                format.format(length / kib) + " KB"
            } else format.format(length) + " B"
        }
    }
}