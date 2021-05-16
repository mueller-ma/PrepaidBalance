package com.github.muellerma.prepaidbalance.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BalanceEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun balanceDao(): BalanceDao

    companion object {
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "balance-history"
                ).build()
            }

            // TODO better solution?
            return instance!!
        }
    }
}