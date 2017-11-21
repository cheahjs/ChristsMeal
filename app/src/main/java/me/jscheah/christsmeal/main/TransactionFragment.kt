package me.jscheah.christsmeal.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.brandongogetap.stickyheaders.StickyLayoutManager
import kotlinx.coroutines.experimental.android.UI
import kotlinx.android.synthetic.main.fragment_transaction_list.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import me.jscheah.christsmeal.data.Network.Balances

import me.jscheah.christsmeal.R
import me.jscheah.christsmeal.data.DataManager
import me.jscheah.christsmeal.data.Network
import me.jscheah.christsmeal.data.models.Transaction
import kotlin.properties.Delegates

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
    private var layoutManager: StickyLayoutManager by Delegates.notNull()
    private var dataManager: DataManager by Delegates.notNull()

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
        dataManager = DataManager(view!!.context)

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            refreshData()
        }
        layoutManager = StickyLayoutManager(view.context, adapter)
        if (savedInstanceState == null) {
//            swipeRefreshLayout.isRefreshing = true
        } else {
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(STATE_LAYOUT))
        }
        transaction_list.addItemDecoration(DividerItemDecoration(view.context, layoutManager.orientation))
        layoutManager.elevateHeaders(true)
        transaction_list.layoutManager = layoutManager
        transaction_list.adapter = adapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = "Transactions"
        if (balances != null) {
            setBalances()
        }
        if (adapter.values != null) {
            adapter.notifyDataSetChanged()
        } else {
            if (dataManager.isTransactionCached())
                getCachedData()
            refreshData(!dataManager.isTransactionCached())
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putBundle(STATE_ADAPTER, adapter.saveState())
        outState?.putParcelable(STATE_LAYOUT, layoutManager.onSaveInstanceState())
        outState?.putParcelable(STATE_BALANCES, balances)
    }

    override fun onDestroy() {
        // Cancel refresh job if it's running
        if (refreshJob != null && refreshJob!!.isActive)
            refreshJob!!.cancel()
        super.onDestroy()
    }

    private fun getCachedData() {
        transactionList = dataManager.getTransactionHistoryCached()
        balances = dataManager.getBalancesCached()
        setBalances()
        adapter.setData(transactionList!!)
        adapter.notifyDataSetChanged()
    }

    private fun refreshData(showRefresh: Boolean = true) {
        if (refreshJob != null && refreshJob!!.isActive)
            return
        if (showRefresh)
            swipeRefreshLayout.isRefreshing = true
        refreshJob = launch(UI) {
            try {
                transactionList = dataManager.getTransactionHistory(10 * 365L, 2 * 365L)
            } catch (e: Network.LoginFailedException) {
                Toast.makeText(this@TransactionFragment.context, R.string.network_fail, Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
                return@launch
            }
            balances = dataManager.getBalancesCached()

            setBalances()
            adapter.setData(transactionList!!)
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

    fun scrollToTop() {
        layoutManager.scrollToPosition(0)
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
