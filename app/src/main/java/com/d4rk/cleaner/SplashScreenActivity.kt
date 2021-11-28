package com.d4rk.cleaner
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
class SplashScreenActivity : AppCompatActivity() {
    private var handler: Handler? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val darkModeValues = resources.getStringArray(R.array.theme_values)
        val pref = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.theme), getString(R.string.default_theme_switcher))
        if (pref == darkModeValues[0]) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        if (pref == darkModeValues[1]) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        if (pref == darkModeValues[2]) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        if (pref == darkModeValues[3]) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        setContentView(R.layout.activity_splash_screen)
        handler = Handler()
        handler!!.postDelayed({
            val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2500)
    }
}