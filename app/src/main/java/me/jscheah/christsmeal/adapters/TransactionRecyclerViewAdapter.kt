package me.jscheah.christsmeal.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.brandongogetap.stickyheaders.exposed.StickyHeaderHandler
import me.jscheah.christsmeal.R

import me.jscheah.christsmeal.models.Transaction

/**
 * [RecyclerView.Adapter] that can display a [Transaction]
 */
class TransactionRecyclerViewAdapter(private val mValues: List<Transaction>) :
        RecyclerView.Adapter<TransactionRecyclerViewAdapter.ViewHolder>(),
        StickyHeaderHandler {

    override fun getAdapterData(): MutableList<*> {
        return mValues.toMutableList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mTransaction = mValues[position]
        holder.mNameView.text = mValues[position].itemDescription
        holder.mPriceView.text = mValues[position].value

//        holder.mView.setOnClickListener {
//            mListener?.onListFragmentInteraction(holder.mItem!!)
//        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mNameView: TextView
        val mPriceView: TextView
        var mTransaction: Transaction? = null

        init {
            mNameView = mView.findViewById(R.id.textName)
            mPriceView = mView.findViewById(R.id.textPrice)
        }

        override fun toString(): String {
            return super.toString() + " '" + mNameView.text + "'"
        }
    }
}
