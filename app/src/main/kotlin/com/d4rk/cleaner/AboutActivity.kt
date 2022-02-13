package com.d4rk.cleaner
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.about, SettingsFragment())
                .commit()
        }
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }
    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.about, rootKey)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.about_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.open_source_libraries) {
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
        }
        if (id == R.id.changelog) {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(R.string.about_changelog)
            alertDialog.setMessage(R.string.changelog)
            alertDialog.setPositiveButton("Cool!") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            alertDialog.show()
        }
        if (id == R.id.privacy_policy) {
            val newIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://bit.ly/d4rkcleanerprivacypolicy")
            )
            startActivity(newIntent)
        }
        if (id == R.id.about_license) {
            val newIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://bit.ly/GPL-3_0")
            )
            startActivity(newIntent)
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}