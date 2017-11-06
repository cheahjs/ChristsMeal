package me.jscheah.christsmeal.models

data class Booking(
        // Data
        val rawDate: String,
        val date: Long,
        val rawTime: String,
        val parentDesc: String,
        val codeDesc: String,
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
        val infoAvailable: Boolean
)