package me.jscheah.christsmeal.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.brandongogetap.stickyheaders.StickyLayoutManager
import kotlinx.coroutines.experimental.android.UI
import kotlinx.android.synthetic.main.fragment_transaction_list.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import me.jscheah.christsmeal.Network

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
    var refreshJob : Job? = null
    var applicationContext: Context? = null
    var transactionList: List<Transaction>? = null
    private var adapter = TransactionRecyclerViewAdapter()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_transaction_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        swipeRefreshLayout.setOnRefreshListener { refreshData() }
        applicationContext = view!!.context.applicationContext
        val layoutManager = StickyLayoutManager(view!!.context, adapter)
        transaction_list.addItemDecoration(DividerItemDecoration(view.context, layoutManager.orientation))
        layoutManager.elevateHeaders(true)
        transaction_list.layoutManager = layoutManager
        transaction_list.adapter = adapter
        refreshData()
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

            val (sundries, meals, journeys) = balances
            textJourneys.text = journeys
            textMeals.text = meals
            textSundries.text = sundries
            adapter.setData(transactionList)
            adapter.notifyDataSetChanged()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    companion object {
        fun newInstance(): TransactionFragment {
            return TransactionFragment()
        }
    }
}
