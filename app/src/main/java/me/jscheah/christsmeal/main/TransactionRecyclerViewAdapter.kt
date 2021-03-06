package me.jscheah.christsmeal.main

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler
import me.jscheah.christsmeal.R

import me.jscheah.christsmeal.data.models.Transaction
import me.jscheah.christsmeal.data.models.TransactionHeader
import me.jscheah.christsmeal.data.models.TransactionBase
import java.util.*

/**
 * [RecyclerView.Adapter] that can display a [Transaction]
 */
class TransactionRecyclerViewAdapter :
        RecyclerView.Adapter<TransactionRecyclerViewAdapter.ViewHolder>(),
        StickyHeaderHandler {
    private val TAG = this::class.java.simpleName
    var values: List<TransactionBase>? = null

    override fun getAdapterData(): MutableList<*> {
        return values?.toMutableList() ?: LinkedList<TransactionBase>()
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
        val item = values!![position]
        when (item) {
            is Transaction -> {
                holder.mTransaction = item
                holder.mNameView!!.text = "${if (item.quantity != "1") "(${item.quantity}) " else ""}${item.itemDescription}"
                holder.mPriceView!!.text = item.value
            }
            is TransactionHeader -> {
                holder.mHeaderView!!.text = item.title
            }
        }
    }

    override fun getItemCount(): Int {
        return values?.size ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (values?.get(position) is Transaction)
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
            values = state.map { it as TransactionBase }.toList()
        } catch (e: ClassCastException) {
            Log.e(TAG, "restoreState: failed to restore state $e")
            return false
        }
        return true
    }

    fun setData(data: List<Transaction>) {
        val mutableList = emptyList<TransactionBase>().toMutableList()
        var currentDate = ""
        data.sortedWith(compareByDescending<Transaction> { it.date }.thenBy { it.groupOrder })
                .forEach {
                    if (currentDate != it.rawDate) {
                        mutableList.add(TransactionHeader(it.rawDate))
                        currentDate = it.rawDate
                    }
                    mutableList.add(it)
                }
        if (values != null) {
            //TODO: use DiffUtil
        }
        values = mutableList.toList()
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

    companion object {
        const val VIEW_HEADER = 1
        const val VIEW_ITEM = 0
        const val STATE_LISTS = "state:transaction_lists"
    }
}
