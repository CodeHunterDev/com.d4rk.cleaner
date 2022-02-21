package com.d4rk.cleaner.empty
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.d4rk.cleaner.R
class AdvancedEmptyFolderCleanerSAFActivity : AppCompatActivity() {
    private var alreadyRed = ""
    var b_clean: Button? = null
    var b_default: Button? = null
    private var bottomPadding = 0
    private var currentLocation: String? = ""
    var currentProgressMemory = 0
    var currentStatus: String? = null
    private var et_path: TextView? = null
    var foldersCount = 0
    var isFinished = false
    private var leftPadding = 0
    var pickedDir: DocumentFile? = null
    private var rightPadding = 0
    var scrollView: ScrollView? = null
    private var topPadding = 0
    var totalUsedMemory = 0
    var tv_count: TextView? = null
    var tv_status: TextView? = null
    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_advanced_empty_folder_cleaner_saf)
        val scrollView2 = findViewById<View>(R.id.scrollView) as ScrollView
        scrollView = scrollView2
        bottomPadding = scrollView2.paddingBottom
        topPadding = scrollView!!.paddingTop
        leftPadding = scrollView!!.paddingLeft
        rightPadding = scrollView!!.paddingRight
        et_path = findViewById<View>(R.id.advancedEmptyFolderCleanerPath) as TextView
        b_clean = findViewById<View>(R.id.buttonAdvancedEmptyFolderCleanerClean) as Button
        b_default = findViewById<View>(R.id.buttonDefault) as Button
        tv_status = findViewById<View>(R.id.advancedEmptyFolderCleanerStatus) as TextView
        tv_count = findViewById<View>(R.id.advancedEmptyFolderCleanerCount) as TextView
        b_default!!.setOnClickListener {
            this@AdvancedEmptyFolderCleanerSAFActivity.startActivityForResult(
                Intent("android.intent.action.OPEN_DOCUMENT_TREE"),
                1000
            )
        }
        b_clean!!.setOnClickListener {
            if (currentLocation != "") {
                alreadyRed = ""
                totalUsedMemory = 0
                currentProgressMemory = 0
                foldersCount = 0
                currentStatus = ""
                tv_count!!.text = this@AdvancedEmptyFolderCleanerSAFActivity.getString(R.string.text_information)
                tv_status!!.text = currentStatus
                if (pickedDir!!.exists()) {
                    b_clean!!.isEnabled = false
                    b_default!!.isEnabled = false
                    return@setOnClickListener
                }
                tv_status!!.setText(R.string.text_missing)
                return@setOnClickListener
            }
            Toast.makeText(this@AdvancedEmptyFolderCleanerSAFActivity, R.string.toast_bad_folder_path, Toast.LENGTH_SHORT)
                .show()
        }
    }
    public override fun onActivityResult(i: Int, i2: Int, intent: Intent?) {
        super.onActivityResult(i, i2, intent)
        if (i != 1000) {
            return
        }
        if (i2 == -1) {
            val data = intent!!.data
            pickedDir = DocumentFile.fromTreeUri(this, data!!)
            grantUriPermission(packageName, data, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            contentResolver.takePersistableUriPermission(
                data,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            currentLocation = pickedDir!!.uri.path
            et_path!!.text = data.path!!.replace(":", "/").replace("/tree", "")
            b_clean!!.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))
            return
        }
        Toast.makeText(this, R.string.toast_no_folder, Toast.LENGTH_SHORT).show()
    }
    fun getFilesCount(documentFile: DocumentFile?) {
        for (documentFile2 in documentFile!!.listFiles()) {
            if (documentFile2.isDirectory) {
                getFilesCount(documentFile2)
                totalUsedMemory++
            }
        }
    }
    fun deleteFolders(documentFile: DocumentFile?) {
        val listFiles = documentFile!!.listFiles()
        when {
            listFiles.isNotEmpty() -> {
                for (documentFile2 in listFiles) {
                    if (documentFile2.isDirectory) {
                        deleteFolders(documentFile2)
                        if (!alreadyRed.contains(""" ${documentFile2.uri.path} """.trimIndent())) {
                            alreadyRed += """  ${documentFile2.uri.path} """.trimIndent()
                            currentProgressMemory++
                        }
                    }
                }
            }
            documentFile.uri.path != currentLocation -> {
                foldersCount++
                var replace = documentFile.uri.path!!.replace(":", "/")
                if (replace.contains("document")) {
                    replace = replace.substring(replace.indexOf("document") + 8)
                }
                currentStatus += "<b>" + getString(R.string.text_deleted) + "</b>" + replace + "<br />"
                documentFile.delete()
                isFinished = false
            }
            else -> {
                isFinished = true
            }
        }
    }
    private abstract inner class GetFilesJob : AsyncTask<Void?, Void?, Void?>() {
        fun doInBackground(vararg voidArr: Void): Void? {
            val programActivity = this@AdvancedEmptyFolderCleanerSAFActivity
            programActivity.getFilesCount(programActivity.pickedDir)
            return null
        }
        public override fun onPreExecute() {
            tv_status!!.setText(R.string.text_working)
        }
        public override fun onPostExecute(voidR: Void?) {
            super.onPostExecute(voidR)
        }
    }
    private abstract inner class DeleteFoldersTask private constructor() : AsyncTask<Void?, Void?, Void?>() {
        fun doInBackground(vararg voidArr: Void): Void? {
            do {
                isFinished = true
                val programActivity = this@AdvancedEmptyFolderCleanerSAFActivity
                programActivity.deleteFolders(programActivity.pickedDir)
            } while (!isFinished)
            return null
        }
        public override fun onPreExecute() {
            tv_status!!.setText(R.string.text_working)
        }
        fun onProgressUpdate(vararg voidArr: Void) {
            super.onProgressUpdate(*voidArr)
            val textView = tv_status
            val programActivity = this@AdvancedEmptyFolderCleanerSAFActivity
            textView!!.text = programActivity.getString(
                R.string.text_working_count,
                programActivity.currentProgressMemory,
                totalUsedMemory
            )
            Log.d("info", "$currentProgressMemory/$totalUsedMemory")
        }
        public override fun onPostExecute(voidR: Void?) {
            b_clean!!.isEnabled = true
            b_default!!.isEnabled = true
            if (isFinished) {
                if (currentStatus == "") {
                    tv_count!!.text = this@AdvancedEmptyFolderCleanerSAFActivity.getString(R.string.text_information)
                    tv_status!!.setText(R.string.text_no_empy_subfolders)
                } else {
                    val textView = tv_status
                    val programActivity = this@AdvancedEmptyFolderCleanerSAFActivity
                    textView!!.text = programActivity.convertHtml(programActivity.currentStatus)
                    val textView2 = tv_count
                    val programActivity2 = this@AdvancedEmptyFolderCleanerSAFActivity
                    textView2!!.text =
                        programActivity2.getString(
                            R.string.text_cleared,
                            programActivity2.foldersCount
                        )
                }
            }
        }
    }
    fun convertHtml(str: String?): Spanned {
        val substring = str!!.substring(0, str.lastIndexOf("<br />"))
        return if (Build.VERSION.SDK_INT >= 24) {
            Html.fromHtml(substring, 0)
        } else Html.fromHtml(substring)
    }
}