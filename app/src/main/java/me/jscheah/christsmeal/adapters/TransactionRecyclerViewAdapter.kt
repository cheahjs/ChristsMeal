package me.jscheah.christsmeal.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler
import me.jscheah.christsmeal.R

import me.jscheah.christsmeal.models.Transaction
import me.jscheah.christsmeal.models.TransactionHeader
import me.jscheah.christsmeal.models.TransactionBase
import java.util.*

/**
 * [RecyclerView.Adapter] that can display a [Transaction]
 */
class TransactionRecyclerViewAdapter :
        RecyclerView.Adapter<TransactionRecyclerViewAdapter.ViewHolder>(),
        StickyHeaderHandler {

    private var mValues: List<TransactionBase>? = null
    private val VIEW_HEADER = 1
    private val VIEW_ITEM = 0

    override fun getAdapterData(): MutableList<*> {
        return mValues?.toMutableList() ?: LinkedList<TransactionBase>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder( LayoutInflater.from(parent.context)
                    .inflate(
                            when (viewType) {
                                VIEW_ITEM -> R.layout.item_transaction
                                VIEW_HEADER -> R.layout.item_transaction_header
                                else -> throw IllegalArgumentException()
                            }, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues!![position]
        when (item) {
            is Transaction -> {
                holder.mTransaction = item
                holder.mNameView!!.text = item.itemDescription
                holder.mPriceView!!.text = item.value
            }
            is TransactionHeader -> {
                holder.mHeaderView!!.text = item.title
            }
        }
    }
    override fun getItemCount(): Int {
        return mValues?.size ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (mValues?.get(position) ?: TransactionBase() is Transaction)
            0 else 1
    }

    fun setData(data: List<Transaction>) {
        val mutableList = emptyList<TransactionBase>().toMutableList()
        var currentDate = ""
        data.forEach {
            if (currentDate != it.date) {
                mutableList.add(TransactionHeader(it.date))
                currentDate = it.date
            }
            mutableList.add(it)
        }
        mValues = mutableList.toList()
    }

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val mNameView: TextView? = mView.findViewById(R.id.textName)
        val mPriceView: TextView? = mView.findViewById(R.id.textPrice)
        val mHeaderView: TextView? = mView.findViewById(R.id.textHeader)
        var mTransaction: Transaction? = null

        override fun toString(): String {
            return super.toString() + " '" + (mNameView?.text ?: mHeaderView?.text) + "'"
        }
    }
}
