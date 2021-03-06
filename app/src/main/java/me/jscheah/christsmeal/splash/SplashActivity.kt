package me.jscheah.christsmeal.splash

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.preference.PreferenceManager
import me.jscheah.christsmeal.R
import me.jscheah.christsmeal.login.LoginActivity
import me.jscheah.christsmeal.main.MainActivity


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        var intent = Intent(this, LoginActivity::class.java)
        // Check if we already have credentials
        if (sharedPrefs!!.getBoolean(getString(R.string.pref_raven_authenticated), false)) {
            intent = Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
