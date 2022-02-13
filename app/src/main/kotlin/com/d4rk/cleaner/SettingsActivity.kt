package com.d4rk.cleaner
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.d4rk.cleaner.CleanReceiver.Companion.cancelAlarm
import com.d4rk.cleaner.CleanReceiver.Companion.scheduleAlarm
class SettingsActivity : AppCompatActivity(), OnSharedPreferenceChangeListener, Preference.SummaryProvider<ListPreference> {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager.beginTransaction().replace(R.id.layout, MyPreferenceFragment()).commit()
        val supportActionBar = supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this)
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val darkModeString = getString(R.string.theme)
        if (key == darkModeString) {
            val darkModeValues = resources.getStringArray(R.array.theme_values)
            val pref = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.theme), getString(R.string.default_theme_switcher))
            if (pref == darkModeValues[0]) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            if (pref == darkModeValues[1]) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            if (pref == darkModeValues[2]) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            if (pref == darkModeValues[3]) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }
    }
    override fun provideSummary(preference: ListPreference): CharSequence? {
        val key = preference.key
        if (key != null) if (key == getString(R.string.theme)) return preference.entry
        return null
    }
    class MyPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setHasOptionsMenu(true)
            findPreference<Preference>("true_aggressive")!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference, _: Any? ->
                    val checked = (preference as CheckBoxPreference).isChecked
                    if (!checked) {
                        val filtersFiles = resources.getStringArray(R.array.true_aggressive_filter_folders)
                        val alertDialog = AlertDialog.Builder(requireContext(), R.style.MyAlertDialogTheme).create()
                        alertDialog.setTitle(getString(R.string.warning))
                        alertDialog.setMessage(getString(R.string.adds_the_following) + " " + filtersFiles.contentToString())
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK!") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                        alertDialog.show()
                    }
                    true
                }
            findPreference<Preference>("buttonpositions")!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference, _: Any? ->
                val checked = (preference as CheckBoxPreference).isChecked
                if (!checked) {
                    val alertDialog = AlertDialog.Builder(requireContext(), R.style.MyAlertDialogTheme).create()
                    alertDialog.setTitle(getString(R.string.warning))
                    alertDialog.setMessage(getString(R.string.button_positions))
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK!") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                    alertDialog.show()
                }
                true
            }
            findPreference<Preference>("dailyclean")!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference, _: Any? ->
                    val checked = (preference as CheckBoxPreference).isChecked
                    if (!checked) {
                        scheduleAlarm(requireContext().applicationContext)
                    } else {
                        cancelAlarm(requireContext().applicationContext)
                    }
                    true
                }
        }
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.settings)
        }
    }
}