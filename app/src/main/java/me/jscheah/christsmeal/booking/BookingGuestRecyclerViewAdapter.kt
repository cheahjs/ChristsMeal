package me.jscheah.christsmeal.booking

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.jscheah.christsmeal.R
import kotlinx.android.synthetic.main.item_booking_guest.view.*

/**
 * [RecyclerView.Adapter] that can display a [String]
 */
class BookingGuestRecyclerViewAdapter(private val mValues: List<String>) : RecyclerView.Adapter<BookingGuestRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_booking_guest, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mValues[position])
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        fun bind(name: String) {
            mView.guest_name.text = name
        }
    }
}
