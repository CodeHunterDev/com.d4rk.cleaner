package com.d4rk.cleaner
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree
import androidx.appcompat.app.AppCompatActivity
import com.d4rk.cleaner.databinding.ActivityWhitelistBinding
import dev.shreyaspatil.MaterialDialog.MaterialDialog
import dev.shreyaspatil.MaterialDialog.interfaces.DialogInterface
import java.util.*
class WhitelistActivity : AppCompatActivity() {
    private var binding: ActivityWhitelistBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whitelist)
        binding = ActivityWhitelistBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.newButton.setOnClickListener { addToWhiteList() }
        getWhiteList(MainActivity.prefs)
        loadViews()
    }
    private fun loadViews() {
        binding!!.pathsLayout.removeAllViews()
        val layout = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        layout.setMargins(0, 20, 0, 20)
        for (path in whiteList) {
            val button = Button(this)
            button.text = path
            button.textSize = 18f
            button.isAllCaps = false
            button.setBackgroundResource(R.drawable.whitelist_card)
            button.setOnClickListener { removePath(path, button) }
            button.setPadding(50, 50, 50, 50)
            runOnUiThread { binding!!.pathsLayout.addView(button, layout) }
        }
        if (whiteList.isEmpty()) {
            val textView = TextView(this)
            textView.setText(R.string.whitelist_empty)
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            runOnUiThread { binding!!.pathsLayout.addView(textView, layout) }
        }
    }
    private fun removePath(path: String?, button: Button?) {
        val mDialog = MaterialDialog.Builder(this)
            .setTitle(getString(R.string.remove_question))
            .setMessage(path!!)
            .setAnimation(R.raw.whitelist)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.clean)) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
                whiteList.remove(path)
                MainActivity.prefs?.edit()!!.putStringSet("whitelist", HashSet(whiteList)).apply()
                binding!!.pathsLayout.removeView(button)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
            .build()
        mDialog.show()
    }
    private fun addToWhiteList() {
        mGetContent.launch(Uri.fromFile(Environment.getDataDirectory()))
    }
    private var mGetContent = registerForActivityResult(
        OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            whiteList.add(uri.path!!.substring(uri.path!!.indexOf(":") + 1))
            MainActivity.prefs?.edit()!!.putStringSet("whitelist", HashSet(whiteList)).apply()
            loadViews()
        }
    }
    companion object {
        private var whiteList: ArrayList<String> = ArrayList()
        fun getWhiteList(prefs: SharedPreferences?): List<String?> {
            if (whiteList.isNullOrEmpty()) {
                if (prefs != null) {
                    whiteList = ArrayList(prefs.getStringSet("whitelist", emptySet()))
                }
                whiteList.remove("[")
                whiteList.remove("]")
            }
            return whiteList
        }
    }
}