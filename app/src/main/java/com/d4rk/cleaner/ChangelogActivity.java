package com.d4rk.cleaner;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Objects;
public class ChangelogActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_changelog);
    }
    @Override public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    public void changelog_button(View view) {
        Toast.makeText(this, "Changelog updated.", Toast.LENGTH_SHORT).show();
    }
}