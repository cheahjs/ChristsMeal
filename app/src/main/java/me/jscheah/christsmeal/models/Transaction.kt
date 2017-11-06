package me.jscheah.christsmeal.models

import android.os.Parcel
import android.os.Parcelable
import com.brandongogetap.stickyheaders.exposed.StickyHeader

data class Transaction(
        val rawDate: String,
        val rawTime: String,
        val date: Long,
        val itemId: String,
        val itemDescription: String,
        val quantity: String,
        val value: String,
        val groupOrder: Int
) : TransactionBase() {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(rawDate)
        parcel.writeString(rawTime)
        parcel.writeLong(date)
        parcel.writeString(itemId)
        parcel.writeString(itemDescription)
        parcel.writeString(quantity)
        parcel.writeString(value)
        parcel.writeInt(groupOrder)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Transaction> {
        override fun createFromParcel(parcel: Parcel): Transaction {
            return Transaction(parcel)
        }

        override fun newArray(size: Int): Array<Transaction?> {
            return arrayOfNulls(size)
        }
    }
}

data class TransactionHeader(
        val title: String
) : TransactionBase(), StickyHeader {
    constructor(parcel: Parcel) : this(parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TransactionHeader> {
        override fun createFromParcel(parcel: Parcel): TransactionHeader {
            return TransactionHeader(parcel)
        }

        override fun newArray(size: Int): Array<TransactionHeader?> {
            return arrayOfNulls(size)
        }
    }
}

sealed class TransactionBase: Parcelable