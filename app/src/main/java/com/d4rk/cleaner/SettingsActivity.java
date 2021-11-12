package com.d4rk.cleaner;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import java.util.Arrays;
import java.util.ResourceBundle;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.SummaryProvider < androidx.preference.ListPreference > {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager().beginTransaction().replace(R.id.layout, new MyPreferenceFragment()).commit();
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null)
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String darkModeString = getString(R.string.theme);
        if (key != null && sharedPreferences != null)
            if (key.equals(darkModeString)) {
                final String[] darkModeValues = getResources().getStringArray(R.array.theme_values);
                String pref = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.theme), getString(R.string.default_theme_switcher));
                if (pref.equals(darkModeValues[0]))
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                if (pref.equals(darkModeValues[1]))
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                if (pref.equals(darkModeValues[2]))
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                if (pref.equals(darkModeValues[3]))
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
            }
    }
    @Override
    public CharSequence provideSummary(ListPreference preference) {
        String key = preference.getKey();
        if (key != null)
            if (key.equals(getString(R.string.theme)))
                return preference.getEntry();
        return null;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
    public static class MyPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setHasOptionsMenu(true);
            findPreference("aggressive").setOnPreferenceChangeListener((preference, newValue) -> {
                boolean checked = ((CheckBoxPreference) preference).isChecked();
                if (!checked) {
                    String[] filtersFiles = getResources().getStringArray(R.array.aggressive_filter_folders);
                    AlertDialog alertDialog = new AlertDialog.Builder(requireContext(), R.style.MyAlertDialogTheme).create();
                    alertDialog.setTitle(getString(R.string.aggressive_filter_what_title));
                    alertDialog.setMessage(getString(R.string.adds_the_following)+" "+ Arrays.toString(filtersFiles));
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK",
                            (dialog, which) -> dialog.dismiss());
                    alertDialog.show();
                }
                return true;
            });
            findPreference("true_aggressive").setOnPreferenceChangeListener((preference, newValue) -> {
                boolean checked = ((CheckBoxPreference) preference).isChecked();
                if (!checked) {
                    String[] filtersFiles = getResources().getStringArray(R.array.true_aggressive_filter_folders);
                    AlertDialog alertDialog = new AlertDialog.Builder(requireContext(), R.style.MyAlertDialogTheme).create();
                    alertDialog.setTitle(getString(R.string.aggressive_filter_what_title));
                    alertDialog.setMessage(getString(R.string.adds_the_following)+" "+ Arrays.toString(filtersFiles));
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK",
                            (dialog, which) -> dialog.dismiss());
                    alertDialog.show();
                }
                return true;
            });
            findPreference("dailyclean").setOnPreferenceChangeListener((preference, newValue) -> {
                boolean checked = ((CheckBoxPreference) preference).isChecked();
                if (!checked) {
                    CleanReciver.scheduleAlarm(requireContext().getApplicationContext());
                } else {
                    CleanReciver.cancelAlarm(requireContext().getApplicationContext());
                }
                return true;
            });
        }
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.settings);
        }
    }
}