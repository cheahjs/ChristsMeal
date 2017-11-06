package me.jscheah.christsmeal.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import me.jscheah.christsmeal.R
import me.jscheah.christsmeal.models.Booking

/**
 * [RecyclerView.Adapter] that can display a [Booking]
 * TODO: Replace the implementation with code for your data type.
 */
class BookingRecyclerViewAdapter(private val mValues: List<Booking>) : RecyclerView.Adapter<BookingRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_booking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
//        holder.mIdView.text = mValues[position].id
//        holder.mContentView.text = mValues[position].content
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
//        val mIdView: TextView
//        val mContentView: TextView
        var mItem: Booking? = null

        init {
//            mIdView = mView.findViewById(R.id.id) as TextView
//            mContentView = mView.findViewById(R.id.content) as TextView
        }

//        override fun toString(): String {
//            return super.toString() + " '" + mContentView.text + "'"
//        }
    }
}
