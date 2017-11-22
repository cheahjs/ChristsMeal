package me.jscheah.christsmeal.booking

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.android.synthetic.main.fragment_booking_guest_list.*

import me.jscheah.christsmeal.R
import me.jscheah.christsmeal.data.Network
import me.jscheah.christsmeal.data.models.Booking
import kotlin.properties.Delegates

/**
 * A fragment representing a list of Items.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class BookingGuestFragment : Fragment() {

    private var mBooking by Delegates.notNull<Booking>()
    private var refreshJob: Job? = null
    private var mGuestList: List<String>? = null
    private var mLayoutManager: LinearLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBooking = arguments.getParcelable(ARG_DATA)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_booking_guest_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = view!!.context
        mLayoutManager = LinearLayoutManager(context)
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_LAYOUT)) {
            mLayoutManager!!.onRestoreInstanceState(savedInstanceState.getParcelable(STATE_LAYOUT))
        }
        booking_guest_list.layoutManager = mLayoutManager
        booking_guest_list.addItemDecoration(DividerItemDecoration(view.context, mLayoutManager!!.orientation))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ADAPTER)) {
            mGuestList = savedInstanceState.getStringArray(STATE_ADAPTER).toList()
        }
        if (mGuestList != null) {
            booking_guest_list.adapter = BookingGuestRecyclerViewAdapter(mGuestList!!)
            booking_guest_list.adapter.notifyDataSetChanged()
        } else {
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
        outState?.putStringArray(STATE_ADAPTER, mGuestList?.toTypedArray())
        outState?.putParcelable(STATE_LAYOUT, mLayoutManager?.onSaveInstanceState())
    }

    private fun refreshData() {
        if (refreshJob != null && refreshJob!!.isActive)
            return
        refreshJob = launch(UI) {
            try {
                mGuestList = Network.getGuestList(mBooking.id)
            } catch (e: Exception) {
                when (e) {
                    is Network.LoginFailedException,
                    is Network.NetworkErrorException -> {
                        Toast.makeText(this@BookingGuestFragment.context, R.string.network_fail, Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    else -> throw e
                }
            }

            booking_guest_list.adapter = BookingGuestRecyclerViewAdapter(mGuestList!!)
            booking_guest_list.adapter.notifyDataSetChanged()
        }
    }


    companion object {
        const val STATE_ADAPTER = "state:guest:adapter"
        const val STATE_LAYOUT = "state:guest:layout"
        /**
         * The fragment argument representing the booking data for this fragment
         */
        private val ARG_DATA = "booking_data"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(data: Booking): BookingGuestFragment {
            val fragment = BookingGuestFragment()
            val args = Bundle()
            args.putParcelable(ARG_DATA, data)
            fragment.arguments = args
            return fragment
        }
    }
}
