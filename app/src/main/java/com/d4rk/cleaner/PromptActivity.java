package com.d4rk.cleaner;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
public class PromptActivity extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompt);
        Button button = findViewById(R.id.button1);
        button.setOnClickListener(view -> {
            startActivityForResult(new Intent(Settings.ACTION_APP_SEARCH_SETTINGS), 0);
            System.exit(0);
        });
    }
}