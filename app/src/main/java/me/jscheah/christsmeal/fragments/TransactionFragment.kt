package me.jscheah.christsmeal.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.brandongogetap.stickyheaders.StickyLayoutManager
import kotlinx.coroutines.experimental.android.UI
import kotlinx.android.synthetic.main.fragment_transaction_list.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import me.jscheah.christsmeal.Network
import me.jscheah.christsmeal.Network.Balances

import me.jscheah.christsmeal.R
import me.jscheah.christsmeal.adapters.TransactionRecyclerViewAdapter
import me.jscheah.christsmeal.models.Transaction
import java.util.*

/**
 * A fragment representing a list of [Transaction]s.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class TransactionFragment : Fragment() {
    private var refreshJob : Job? = null
    private var transactionList: List<Transaction>? = null
    private var balances: Balances? = null
    private var adapter = TransactionRecyclerViewAdapter()
    private var layoutManager: StickyLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            adapter.restoreState(savedInstanceState.getBundle(STATE_ADAPTER))
            balances = savedInstanceState.getParcelable(STATE_BALANCES)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_transaction_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        swipeRefreshLayout.setOnRefreshListener { refreshData() }
        layoutManager = StickyLayoutManager(view!!.context, adapter)
        if (savedInstanceState == null) {
            swipeRefreshLayout.isRefreshing = true
        } else {
            layoutManager!!.onRestoreInstanceState(savedInstanceState.getParcelable(STATE_LAYOUT))
        }
        transaction_list.addItemDecoration(DividerItemDecoration(view.context, layoutManager!!.orientation))
        layoutManager!!.elevateHeaders(true)
        transaction_list.layoutManager = layoutManager
        transaction_list.adapter = adapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (balances != null) {
            setBalances()
        }
        if (adapter.values != null) {
            adapter.notifyDataSetChanged()
        } else {
            refreshData()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putBundle(STATE_ADAPTER, adapter.saveState())
        outState?.putParcelable(STATE_LAYOUT, layoutManager?.onSaveInstanceState())
        outState?.putParcelable(STATE_BALANCES, balances)
    }

    override fun onDestroy() {
        // Cancel refresh job if it's running
        if (refreshJob != null && refreshJob!!.isActive)
            refreshJob!!.cancel()
        super.onDestroy()
    }

    private fun refreshData() {
        if (refreshJob != null && refreshJob!!.isActive)
            return
        swipeRefreshLayout.isRefreshing = true
        refreshJob = launch(UI) {
            val (transactionList, balances) = Network.getTransactions(
                    Date(System.currentTimeMillis()-(10L*365*24*60*60*1000)),
                    Date(System.currentTimeMillis()+(2L*365*24*60*60*1000)))
            this@TransactionFragment.transactionList = transactionList
            this@TransactionFragment.balances = balances

            setBalances()
            adapter.setData(transactionList)
            adapter.notifyDataSetChanged()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setBalances() {
        val (sundries, meals, journeys) = balances!!
        textJourneys.text = journeys
        textMeals.text = meals
        textSundries.text = sundries
    }

    companion object {
        const val STATE_ADAPTER = "state:adapter"
        const val STATE_LAYOUT = "state:layout"
        const val STATE_BALANCES = "state:balances"

        fun newInstance(): TransactionFragment {
            return TransactionFragment()
        }
    }
}
