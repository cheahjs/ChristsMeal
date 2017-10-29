package me.jscheah.christsmeal.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.brandongogetap.stickyheaders.StickyLayoutManager
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.android.synthetic.main.fragment_transaction_list.*
import me.jscheah.christsmeal.Network

import me.jscheah.christsmeal.R
import me.jscheah.christsmeal.adapters.TransactionRecyclerViewAdapter
import java.util.*

/**
 * A fragment representing a list of [Transaction]s.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class TransactionFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_transaction_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val context = view!!.context
        async(UI) {
            val (sundries, meals, journeys) = Network.getBalances()
            textJourneys.text = journeys
            textMeals.text = meals
            textSundries.text = sundries

            val adapter = TransactionRecyclerViewAdapter(
                    Network.getTransactions(
                            Date(System.currentTimeMillis()-(10L*365*24*60*60*1000)),
                            Date(System.currentTimeMillis()+(2L*365*24*60*60*1000)))
            )
            val layoutManager = StickyLayoutManager(context, adapter)
            layoutManager.elevateHeaders(true)
            transaction_list.layoutManager = layoutManager
            transaction_list.adapter = adapter
            transaction_list.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
        }
    }

    companion object {
        fun newInstance(): TransactionFragment {
            return TransactionFragment()
        }
    }
}
