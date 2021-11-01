package com.d4rk.cleaner;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBar;
import androidx.preference.PreferenceFragmentCompat;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.kieronquinn.monetcompat.app.MonetCompatActivity;
public class AboutActivity extends MonetCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.about, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.about, rootKey);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.open_source_libraries) {
            startActivity(new Intent(this, OssLicensesMenuActivity.class));
        }
        if (id == R.id.privacy_policy) {
            Intent newIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://ghcdn.rawgit.org/D4rK7355608/com.d4rk.cleaner/master/privacy_policy.html"));
            startActivity(newIntent);
        }
        if (id == R.id.about_license) {
            Intent newIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.gnu.org/licenses/gpl-3.0.en.html"));
            startActivity(newIntent);
        }
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}