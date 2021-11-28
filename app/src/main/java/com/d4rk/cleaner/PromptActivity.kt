package com.d4rk.cleaner
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import kotlin.system.exitProcess
class PromptActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prompt)
        val button = findViewById<Button>(R.id.button1)
        button.setOnClickListener {
            resultLauncher.launch(Intent(Settings.ACTION_SETTINGS))
            exitProcess(0)
        }
    }
    private var resultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
        }
    }
}