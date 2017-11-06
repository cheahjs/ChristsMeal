package me.jscheah.christsmeal.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class DbHelper(ctx: Context): ManagedSQLiteOpenHelper(ctx, DB_NAME, null, DB_VERSION) {
    companion object {
        const val DB_NAME = "data.db"
        const val DB_VERSION = 1

        private var instance: DbHelper? = null
        @Synchronized
        fun getInstance(ctx: Context): DbHelper {
            if (instance == null) {
                instance = DbHelper(ctx)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTable(TransactionTable.TABLE_TRANSACTIONS, true,
                TransactionTable.COL_ID to INTEGER + PRIMARY_KEY,
                TransactionTable.COL_RAWDATE to TEXT,
                TransactionTable.COL_RAWTIME to TEXT,
                TransactionTable.COL_DATE to INTEGER,
                TransactionTable.COL_ITEMID to TEXT,
                TransactionTable.COL_ITEMDESC to TEXT,
                TransactionTable.COL_QUANTITY to TEXT,
                TransactionTable.COL_VAL to TEXT,
                TransactionTable.COL_GROUPORDER to INTEGER)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.dropTable(TransactionTable.TABLE_TRANSACTIONS, true)
        onCreate(db)
    }
}

val Context.database: DbHelper
    get() = DbHelper.getInstance(applicationContext)