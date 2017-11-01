package me.jscheah.christsmeal.models

import com.brandongogetap.stickyheaders.exposed.StickyHeader

data class Transaction(
        val date: String,
        val time: String,
        val itemId: String,
        val itemDescription: String,
        val quantity: String,
        val value: String
) : TransactionBase()

data class TransactionHeader(
        val title: String
) : TransactionBase(), StickyHeader

open class TransactionBase