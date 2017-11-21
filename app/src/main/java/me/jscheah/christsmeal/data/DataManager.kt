package me.jscheah.christsmeal.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import me.jscheah.christsmeal.data.models.Transaction
import org.jetbrains.anko.db.*
import java.util.*

class DataManager(ctx: Context,
        private val dbHelper: DbHelper = ctx.database) {
    private val sharedPrefs = ctx.getSharedPreferences("me.jscheah.christsmeal.data", MODE_PRIVATE)

    /**
     * @return True if transactions have been cached before
     */
    fun isTransactionCached(): Boolean = sharedPrefs.contains(KEY_TRANSACTION_LAST_UPDATE)

    /**
     * @return Timestamp of last transaction update
     */
    fun getTransactionLastUpdated() = sharedPrefs.getLong(KEY_TRANSACTION_LAST_UPDATE, -1)

    private fun setTransactionLastUpdated(time: Long) = sharedPrefs.edit().putLong(KEY_TRANSACTION_LAST_UPDATE, time).apply()

    @Synchronized
    fun getTransactionHistoryCached(): List<Transaction> {
        return dbHelper.use {
            select(TransactionTable.TABLE_TRANSACTIONS,
                    TransactionTable.COL_RAWDATE, TransactionTable.COL_RAWTIME,
                    TransactionTable.COL_DATE, TransactionTable.COL_ITEMID,
                    TransactionTable.COL_ITEMDESC, TransactionTable.COL_QUANTITY,
                    TransactionTable.COL_VAL, TransactionTable.COL_GROUPORDER)
                    .exec { parseList(classParser()) }
        }
    }

    @Synchronized
    suspend fun getTransactionHistory(daysBefore: Long, daysAfter: Long): List<Transaction> {
        val (transactions, balances) = Network.getTransactions(
                Date(System.currentTimeMillis() - (daysBefore*24*60*60*1000)),
                Date(System.currentTimeMillis() + (daysAfter*24*60*60*1000)))
        replaceTransactionHistory(transactions)
        setTransactionLastUpdated(System.currentTimeMillis())
        setBalances(balances)
        return getTransactionHistoryCached()
    }

    @Synchronized
    private fun replaceTransactionHistory(newTransactions: List<Transaction>) {
        dbHelper.use {
            transaction {
                delete(TransactionTable.TABLE_TRANSACTIONS)
                newTransactions.forEach {
                    insert(TransactionTable.TABLE_TRANSACTIONS,
                            TransactionTable.COL_RAWDATE to it.rawDate,
                            TransactionTable.COL_RAWTIME to it.rawTime,
                            TransactionTable.COL_DATE to it.date,
                            TransactionTable.COL_ITEMID to it.itemId,
                            TransactionTable.COL_ITEMDESC to it.itemDescription,
                            TransactionTable.COL_QUANTITY to it.quantity,
                            TransactionTable.COL_VAL to it.value,
                            TransactionTable.COL_GROUPORDER to it.groupOrder)
                }
            }
        }
    }

    @Synchronized
    fun getBalancesCached(): Network.Balances =
            Network.Balances(
                    sharedPrefs.getString(KEY_SUNDRIES, ""),
                    sharedPrefs.getString(KEY_MEALS, ""),
                    sharedPrefs.getString(KEY_JOURNEYS, ""))

    @Synchronized
    suspend fun getBalances(): Network.Balances {
        val balances = Network.getBalances()
        setBalances(balances)
        return balances
    }

    @Synchronized
    private fun setBalances(balances: Network.Balances) {
        sharedPrefs.edit()
                .putString(KEY_SUNDRIES, balances.sundries)
                .putString(KEY_MEALS, balances.meals)
                .putString(KEY_JOURNEYS, balances.journeys)
                .apply()
    }

    companion object {
        const val KEY_TRANSACTION_LAST_UPDATE = "key:transaction_last_update"
        const val KEY_SUNDRIES = "key:sundries"
        const val KEY_JOURNEYS = "key:journeys"
        const val KEY_MEALS = "key:meals"
    }
}