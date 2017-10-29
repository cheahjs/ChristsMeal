package me.jscheah.christsmeal.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.brandongogetap.stickyheaders.StickyLayoutManager
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
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
        val view = inflater!!.inflate(R.layout.fragment_transaction_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            async(UI) {
                val adapter = TransactionRecyclerViewAdapter(
                        Network.getTransactions(
                                Date(System.currentTimeMillis()-(10L*365*24*60*60*1000)),
                                Date(System.currentTimeMillis()+(2L*365*24*60*60*1000)))
                )
                val layoutManager = StickyLayoutManager(context, adapter)
                layoutManager.elevateHeaders(true)
                view.layoutManager = layoutManager
                view.adapter = adapter
                view.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
            }
        }
        return view
    }

    companion object {
        fun newInstance(): TransactionFragment {
            return TransactionFragment()
        }
    }
}
