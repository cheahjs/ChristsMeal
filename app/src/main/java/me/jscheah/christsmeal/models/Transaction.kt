package me.jscheah.christsmeal.models

data class Transaction(
        val date: String,
        val time: String,
        val itemId: String,
        val itemDescription: String,
        val quantity: String,
        val value: String
)