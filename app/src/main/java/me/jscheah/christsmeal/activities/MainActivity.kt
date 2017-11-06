package me.jscheah.christsmeal.activities

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import me.jscheah.christsmeal.data.Network
import me.jscheah.christsmeal.R
import me.jscheah.christsmeal.fragments.TransactionFragment


class MainActivity : AppCompatActivity() {

    private var transactionFragment = TransactionFragment.newInstance()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        // Do nothing if reselected
        if (navigation.selectedItemId == item.itemId) {
            return@OnNavigationItemSelectedListener false
        }
        when (item.itemId) {
            R.id.navigation_transactions -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, transactionFragment)
                        .commit()
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

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, transactionFragment, FRAGMENT_TAG_TRANSACTION)
                    .commit()
        } else {
            transactionFragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_TRANSACTION)
                    as TransactionFragment
        }
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    companion object {
        const val FRAGMENT_TAG_TRANSACTION = "fragment:transactions"
    }
}
