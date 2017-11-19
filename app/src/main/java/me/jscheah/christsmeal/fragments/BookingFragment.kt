package me.jscheah.christsmeal.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.coroutines.experimental.android.UI
import kotlinx.android.synthetic.main.fragment_booking_list.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

import me.jscheah.christsmeal.R
import me.jscheah.christsmeal.adapters.BookingRecyclerViewAdapter
import me.jscheah.christsmeal.data.Network
import me.jscheah.christsmeal.models.Booking
import kotlin.properties.Delegates

/**
 * A fragment representing a list of [Booking]s.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class BookingFragment : Fragment() {
    private var refreshJob : Job? = null
    private var bookingList: List<Booking>? = null
    private var adapter = BookingRecyclerViewAdapter()
    private var layoutManager: LinearLayoutManager by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            adapter.restoreState(savedInstanceState.getBundle(STATE_ADAPTER))
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_booking_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            refreshData()
        }
        layoutManager = LinearLayoutManager(view!!.context)
        if (savedInstanceState == null) {
//            swipeRefreshLayout.isRefreshing = true
        } else {
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(STATE_LAYOUT))
        }
        booking_list.addItemDecoration(DividerItemDecoration(view.context, layoutManager.orientation))
        booking_list.layoutManager = layoutManager
        booking_list.adapter = adapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = "Formal Hall Booking"
        if (adapter.values != null) {
            adapter.notifyDataSetChanged()
        } else {
            refreshData()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putBundle(STATE_ADAPTER, adapter.saveState())
        outState?.putParcelable(STATE_LAYOUT, layoutManager.onSaveInstanceState())
    }

    override fun onDestroy() {
        // Cancel refresh job if it's running
        if (refreshJob != null && refreshJob!!.isActive)
            refreshJob!!.cancel()
        super.onDestroy()
    }

    private fun refreshData(showRefresh: Boolean = true) {
        if (refreshJob != null && refreshJob!!.isActive)
            return
        if (showRefresh)
            swipeRefreshLayout.isRefreshing = true
        refreshJob = launch(UI) {
            try {
                bookingList = Network.getMealBookings()
            } catch (e: Network.LoginFailedException) {
                Toast.makeText(this@BookingFragment.context, R.string.network_fail, Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
                return@launch
            }

            adapter.setData(bookingList!!)
            adapter.notifyDataSetChanged()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    fun scrollToTop() {
        layoutManager.scrollToPosition(0)
    }

    companion object {
        const val STATE_ADAPTER = "state:booking:adapter"
        const val STATE_LAYOUT = "state:booking:layout"

        fun newInstance(): BookingFragment {
            return BookingFragment()
        }
    }
}
