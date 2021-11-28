package com.d4rk.cleaner
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
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
        if (id == R.id.privacy_policy) {
            val newIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://ghcdn.rawgit.org/D4rK7355608/com.d4rk.cleaner/master/privacy_policy.html")
            )
            startActivity(newIntent)
        }
        if (id == R.id.about_license) {
            val newIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.gnu.org/licenses/gpl-3.0.en.html")
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