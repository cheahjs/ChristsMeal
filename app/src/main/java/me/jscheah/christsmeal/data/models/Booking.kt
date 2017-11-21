package me.jscheah.christsmeal.data.models

import android.os.Parcel
import android.os.Parcelable

data class Booking(
        // Data
        val id: String,
        val rawDate: String,
        val date: Long,
        val rawTime: String,
//        val parentDesc: String,
//        val codeDesc: String,
        val desc: String,
        val spaces: String, //Int?
        val guests: String,
        val booked: String,
        // Buttons
        val bookingAvailable: Boolean,
        val guestAvailable: Boolean,
        val changeAvailable: Boolean,
        val cancelAvailable: Boolean,
        val viewAvailable: Boolean,
        val menuAvailable: Boolean,
        val infoAvailable: Boolean,
        // Hidden data
        val message: String = "",
        val adjTime: Int = 0,
        val adjDay: Int = 0,
        val validTime: Boolean = true
): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readByte() != 0.toByte())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(rawDate)
        parcel.writeLong(date)
        parcel.writeString(rawTime)
        parcel.writeString(desc)
        parcel.writeString(spaces)
        parcel.writeString(guests)
        parcel.writeString(booked)
        parcel.writeByte(if (bookingAvailable) 1 else 0)
        parcel.writeByte(if (guestAvailable) 1 else 0)
        parcel.writeByte(if (changeAvailable) 1 else 0)
        parcel.writeByte(if (cancelAvailable) 1 else 0)
        parcel.writeByte(if (viewAvailable) 1 else 0)
        parcel.writeByte(if (menuAvailable) 1 else 0)
        parcel.writeByte(if (infoAvailable) 1 else 0)
        parcel.writeString(message)
        parcel.writeInt(adjTime)
        parcel.writeInt(adjDay)
        parcel.writeByte(if (validTime) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Booking> {
        override fun createFromParcel(parcel: Parcel): Booking {
            return Booking(parcel)
        }

        override fun newArray(size: Int): Array<Booking?> {
            return arrayOfNulls(size)
        }
    }
}