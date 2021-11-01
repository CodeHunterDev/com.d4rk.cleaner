package com.d4rk.cleaner;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.kieronquinn.monetcompat.app.MonetCompatActivity;
public class PromptActivity extends MonetCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompt);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        Button button = findViewById(R.id.button1);
        button.setOnClickListener(view -> {
            resultLauncher.launch(new Intent(android.provider.Settings.ACTION_SETTINGS));
            System.exit(0);
        });
    }
    //Instead of onActivityResult() method use this one.
    final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
            });
}