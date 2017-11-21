package me.jscheah.christsmeal.main

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import me.jscheah.christsmeal.data.Network
import me.jscheah.christsmeal.R


class MainActivity : AppCompatActivity() {

    private var transactionFragment = TransactionFragment.newInstance()
    private var bookingFragment = BookingFragment.newInstance()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_transactions -> {
                if (navigation.selectedItemId == item.itemId) {
                    transactionFragment.scrollToTop()
                    return@OnNavigationItemSelectedListener false
                }
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, transactionFragment)
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_bookings -> {
                if (navigation.selectedItemId == item.itemId) {
                    bookingFragment.scrollToTop()
                    return@OnNavigationItemSelectedListener false
                }
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, bookingFragment)
                        .commit()
                return@OnNavigationItemSelectedListener true
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
                    .add(R.id.fragment_container,bookingFragment, FRAGMENT_TAG_BOOKING)
                    .replace(R.id.fragment_container, transactionFragment, FRAGMENT_TAG_TRANSACTION)
                    .commit()
        } else {
            transactionFragment = (supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_TRANSACTION)
                    as TransactionFragment?) ?: transactionFragment
            bookingFragment = (supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_BOOKING)
                    as BookingFragment?) ?: bookingFragment
        }
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    companion object {
        const val FRAGMENT_TAG_TRANSACTION = "fragment:transactions"
        const val FRAGMENT_TAG_BOOKING = "fragment:bookings"
    }
}
