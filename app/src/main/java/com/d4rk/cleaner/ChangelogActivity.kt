package com.d4rk.cleaner
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
class ChangelogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changelog)
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}