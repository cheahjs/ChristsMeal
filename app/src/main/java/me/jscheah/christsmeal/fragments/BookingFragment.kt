package me.jscheah.christsmeal.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import me.jscheah.christsmeal.R
import me.jscheah.christsmeal.adapters.BookingRecyclerViewAdapter
import me.jscheah.christsmeal.fragments.dummy.DummyContent
import me.jscheah.christsmeal.fragments.dummy.DummyContent.DummyItem

/**
 * A fragment representing a list of Bookings.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class BookingFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_booking_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            view.layoutManager = LinearLayoutManager(context)
            view.adapter = BookingRecyclerViewAdapter(emptyList())
        }
        return view
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    companion object {
        fun newInstance(): BookingFragment {
            return BookingFragment()
        }
    }
}
