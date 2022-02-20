package com.d4rk.cleaner.empty
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Html
import android.text.Spanned
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.d4rk.cleaner.R
import java.io.File
class ProgramOldActivity : AppCompatActivity() {
    var b_clean: Button? = null
    private var b_default: Button? = null
    private var bottomPadding = 0
    var currentLocation: String? = null
    var currentStatus: String? = null
    private var et_path: EditText? = null
    var foldersCount = 0
    var isFinished = false
    private var leftPadding = 0
    private var rightPadding = 0
    var scrollView: ScrollView? = null
    private var topPadding = 0
    var tv_count: TextView? = null
    var tv_status: TextView? = null
    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_program_old)
        if (true && checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf("android.permission.WRITE_EXTERNAL_STORAGE"), 1000)
        }
        val scrollView2 = findViewById<View>(R.id.scrollView) as ScrollView
        scrollView = scrollView2
        bottomPadding = scrollView2.paddingBottom
        topPadding = scrollView!!.paddingTop
        leftPadding = scrollView!!.paddingLeft
        rightPadding = scrollView!!.paddingRight
        et_path = findViewById<View>(R.id.et_path) as EditText
        val absolutePath = Environment.getExternalStorageDirectory().absolutePath
        currentLocation = absolutePath
        et_path!!.setText(absolutePath)
        b_clean = findViewById<View>(R.id.b_clean) as Button
        b_default = findViewById<View>(R.id.b_default) as Button
        tv_status = findViewById<View>(R.id.tv_status) as TextView
        tv_count = findViewById<View>(R.id.tv_count) as TextView
        b_default!!.setOnClickListener {
            currentLocation = Environment.getExternalStorageDirectory().absolutePath
            et_path!!.setText(currentLocation)
            hideKeyboard()
        }
        b_clean!!.setOnClickListener {
            foldersCount = 0
            currentStatus = ""
            tv_status!!.text = currentStatus
            val programOldActivity = this@ProgramOldActivity
            programOldActivity.currentLocation = programOldActivity.et_path!!.text.toString()
            if (File(currentLocation).exists()) {
                b_clean!!.isEnabled = false
            } else {
                tv_status!!.setText(R.string.text_missing)
            }
            hideKeyboard()
        }
        (findViewById<View>(R.id.card_saf) as CardView).visibility = View.VISIBLE
        (findViewById<View>(R.id.b_saf) as Button).setOnClickListener {
            this@ProgramOldActivity.startActivity(
                Intent(
                    this@ProgramOldActivity.applicationContext,
                    ProgramActivity::class.java
                )
            )
            finish()
        }
        return

    }
    fun downloadNow(str: String) {
        try {
            startActivity(
                Intent(
                    "android.intent.action.VIEW",
                    Uri.parse("market://details?id=$str")
                )
            )
        } catch (unused: ActivityNotFoundException) {
            startActivity(
                Intent(
                    "android.intent.action.VIEW",
                    Uri.parse("http://play.google.com/store/apps/details?id=$str")
                )
            )
        }
    }
    override fun onRequestPermissionsResult(i: Int, strArr: Array<String>, iArr: IntArray) {
        super.onRequestPermissionsResult(i, strArr, iArr)
        if (i != 1000) {
            return
        }
        if (iArr.isEmpty() || iArr[0] != 0) {
            Toast.makeText(this, R.string.text_permission_not_granted, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        Toast.makeText(this, R.string.text_permission_granted, Toast.LENGTH_SHORT).show()
    }
    fun deleteFolders(str: String?) {
        val file = File(str)
        val listFiles = file.listFiles()
        if (listFiles != null) {
            when {
                listFiles.isNotEmpty() -> {
                    for (file2 in listFiles) {
                        if (file2.isDirectory) {
                            deleteFolders(file2.absolutePath)
                        }
                    }
                }
                str != currentLocation -> {
                    foldersCount++
                    currentStatus += "<b>" + getString(R.string.text_deleted) + "</b>" + file.absolutePath + "<br />"
                    file.delete()
                    isFinished = false
                }
                else -> {
                    isFinished = true
                }
            }
        } else {
            runOnUiThread {
                Toast.makeText(
                    this@ProgramOldActivity,
                    R.string.text_no_folder,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private abstract inner class DeleteFoldersTask : AsyncTask<Void?, Void?, Void?>() {
        fun doInBackground(): Void? {
            do {
                isFinished = true
                val programOldActivity = this@ProgramOldActivity
                programOldActivity.deleteFolders(programOldActivity.currentLocation)
            } while (!isFinished)
            return null
        }
        public override fun onPreExecute() {
            tv_status!!.setText(R.string.text_working)
        }
        public override fun onPostExecute(voidR: Void?) {
            b_clean!!.isEnabled = true
            if (!isFinished) {
                return
            }
            if (currentStatus == "") {
                tv_count!!.text =
                    this@ProgramOldActivity.getString(R.string.text_information)
                tv_status!!.setText(R.string.text_no_empy_subfolders)
                return
            }
            val textView = tv_status
            val programOldActivity = this@ProgramOldActivity
            textView!!.text = programOldActivity.convertHtml(programOldActivity.currentStatus)
            val textView2 = tv_count
            val programOldActivity2 = this@ProgramOldActivity
            textView2!!.text = programOldActivity2.getString(
                R.string.text_cleared,
                Integer.valueOf(programOldActivity2.foldersCount)
            )
        }
    }
    fun convertHtml(str: String?): Spanned {
        val substring = str!!.substring(0, str.lastIndexOf("<br />"))
        return if (Build.VERSION.SDK_INT >= 24) {
            Html.fromHtml(substring, 0)
        } else Html.fromHtml(substring)
    }
    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(et_path!!.windowToken, 2)
    }
    private fun showExitDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setMessage(R.string.dialog_message)
        builder.setPositiveButton(
            R.string.dialog_yes,
            { _, _ -> finish() })
        builder.setNegativeButton(
            R.string.dialog_no,
            { dialogInterface, _ -> dialogInterface.dismiss() })
        val create = builder.create()
        create.setCanceledOnTouchOutside(true)
        create.show()
    }
}