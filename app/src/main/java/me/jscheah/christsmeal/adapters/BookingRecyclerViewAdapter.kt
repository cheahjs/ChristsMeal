package me.jscheah.christsmeal.adapters

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.jscheah.christsmeal.R
import me.jscheah.christsmeal.models.Booking
import kotlinx.android.synthetic.main.item_booking.view.*

/**
 * [RecyclerView.Adapter] that can display a [Booking]
 */
class BookingRecyclerViewAdapter :
        RecyclerView.Adapter<BookingRecyclerViewAdapter.ViewHolder>(){
    private val TAG = this::class.java.simpleName
    var values: List<Booking>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder( LayoutInflater.from(parent.context)
                .inflate(
                        when (viewType) {
                            VIEW_ITEM -> R.layout.item_booking
                            else -> throw IllegalArgumentException()
                        }, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values!![position]
        when (item) {
            is Booking -> {
                holder.bind(item)
            }
        }
    }

    override fun getItemCount(): Int {
        return values?.size ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (values?.get(position) is Booking)
            0 else 1
    }

    fun saveState(): Bundle {
        val bundle = Bundle()
        bundle.putParcelableArray(STATE_LISTS, values?.toTypedArray())
        return bundle
    }

    fun restoreState(bundle: Bundle): Boolean {
        val state = bundle.getParcelableArray(STATE_LISTS) ?: return false
        try {
            values = state.map { it as Booking }.toList()
        } catch (e: ClassCastException) {
            Log.e(TAG, "restoreState: failed to restore state $e")
            return false
        }
        return true
    }

    fun setData(data: List<Booking>) {
        val mutableList = emptyList<Booking>().toMutableList()
        data.forEach {mutableList.add(it)}
        if (values != null) {
            //TODO: use DiffUtil
        }
        values = mutableList.toList()
    }

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        fun bind(booking: Booking) {
            with(booking) {
                itemView.booking_booked.text = booked
                itemView.booking_date.text = rawDate
                itemView.booking_event.text = desc
                if (message.isNotBlank()) {
                    itemView.booking_message.visibility = View.VISIBLE
                    itemView.booking_message.text = message
                }
                else {
                    itemView.booking_message.visibility = View.GONE
                }
                itemView.booking_spaces.text = spaces
                itemView.booking_time.text = if (rawTime.isBlank()) "Not Specified" else rawTime
            }
        }
    }

    companion object {
        const val VIEW_ITEM = 0
        const val STATE_LISTS = "state:booking_list"
    }
}
