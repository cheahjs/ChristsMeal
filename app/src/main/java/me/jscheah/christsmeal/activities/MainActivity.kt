package me.jscheah.christsmeal.activities

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import me.jscheah.christsmeal.Network
import me.jscheah.christsmeal.R
import me.jscheah.christsmeal.fragments.TransactionFragment


class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_transactions -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, TransactionFragment())
                transaction.commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_bookings -> {
                // TODO: Bookings fragment
                return@OnNavigationItemSelectedListener false
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.title_activity_main)

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        Network.crsId = sharedPrefs.getString(getString(R.string.pref_raven_username), "")
        Network.password = sharedPrefs.getString(getString(R.string.pref_raven_password), "")

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, TransactionFragment())
        transaction.commit()
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
