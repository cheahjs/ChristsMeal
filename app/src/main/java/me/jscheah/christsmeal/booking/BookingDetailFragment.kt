package me.jscheah.christsmeal.booking

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import me.jscheah.christsmeal.R
import me.jscheah.christsmeal.data.models.Booking
import kotlinx.android.synthetic.main.fragment_booking_info.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import me.jscheah.christsmeal.data.Network
import kotlin.properties.Delegates

class BookingDetailFragment : Fragment() {

    private var mBooking by Delegates.notNull<Booking>()
    private var refreshJob: Job?= null
    private var menuText: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_booking_info, container, false)
        mBooking = arguments.getParcelable(ARG_DATA)
        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_MENU)) {
            booking_menu.text = savedInstanceState.getString(STATE_MENU)
        }
        with(mBooking) {
            booking_booked.text = booked
            booking_event.text = desc
            if (message.isNotBlank()) {
                booking_message.visibility = View.VISIBLE
                booking_message.text = message
            }
            booking_spaces.text = "$spaces ($guests Guests Allowed)"
            booking_time.text = if (rawTime.isBlank()) "Not Specified" else rawTime
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState == null || savedInstanceState.containsKey(STATE_MENU)) {
            refreshData()
        }
    }

    override fun onDestroy() {
        // Cancel refresh job if it's running
        if (refreshJob != null && refreshJob!!.isActive)
            refreshJob!!.cancel()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (menuText != null) {
            outState?.putString(STATE_MENU, menuText)
        }
    }

    private fun refreshData() {
        if (refreshJob != null && refreshJob!!.isActive)
            return
        refreshJob = launch(UI) {
            try {
                val menu = Network.getMenu(mBooking.id)
                menuText = menu
                booking_menu.text = menu
            } catch (e: Exception) {
                when (e) {
                     is Network.LoginFailedException,
                     is Network.NetworkErrorException -> {
                         Toast.makeText(this@BookingDetailFragment.context, R.string.network_fail, Toast.LENGTH_SHORT).show()
                         return@launch
                     }
                    else -> throw e
                }
            }
        }
    }

    companion object {
        private val STATE_MENU = "state:menu"
        /**
         * The fragment argument representing the booking data for this fragment
         */
        private val ARG_DATA = "booking_data"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(data: Booking): BookingDetailFragment {
            val fragment = BookingDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_DATA, data)
            fragment.arguments = args
            return fragment
        }
    }
}