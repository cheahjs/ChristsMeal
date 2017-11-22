package me.jscheah.christsmeal.booking

import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle

import me.jscheah.christsmeal.R
import kotlinx.android.synthetic.main.activity_booking.*
import me.jscheah.christsmeal.data.models.Booking
import kotlin.properties.Delegates

class BookingActivity : AppCompatActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var mBooking by Delegates.notNull<Booking>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

        // Avoid having to recreate fragments
        container.offscreenPageLimit = 3

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        val bookingData = intent.getParcelableExtra<Booking>("DATA")
        mBooking = bookingData
        toolbar.title = mBooking.rawDate
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment =// getItem is called to instantiate the fragment for the given page.
                when (position) {
                    0 -> BookingDetailFragment.newInstance(mBooking)
                    1 -> BookingGuestFragment.newInstance(mBooking)
                    else -> BookingDetailFragment.newInstance(mBooking)
                }

        override fun getCount(): Int =// Show 3 total pages.
                3
    }

}
