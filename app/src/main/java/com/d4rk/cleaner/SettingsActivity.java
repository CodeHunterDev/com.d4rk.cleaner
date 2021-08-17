package com.d4rk.cleaner;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager().beginTransaction().replace(R.id.layout, new MyPreferenceFragment()).commit();
    }
    public static class MyPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setHasOptionsMenu(true);
        }
        /**
         * Inflate Preferences
         */
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.settings);
        }
    }
}