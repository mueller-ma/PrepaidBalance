package com.github.muellerma.prepaidbalance

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.muellerma.prepaidbalance.room.AppDatabase
import com.github.muellerma.prepaidbalance.room.BalanceEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    @Before
    fun setupDatabase() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val now = System.currentTimeMillis()
        AppDatabase.get(appContext)
            .balanceDao().apply {
                deleteAll()
                insert(BalanceEntry(timestamp = now, balance = 10.15, fullResponse = "foobar 10.15"))
                insert(BalanceEntry(timestamp = now - 5 * 60 * 1000, balance = 0.15, fullResponse = "foobar 0.15"))
                insert(BalanceEntry(timestamp = now - 60 * 60 * 1000, balance = 5.12, fullResponse = "foobar 5.12"))
                insert(BalanceEntry(timestamp = now - 30 * 60 * 60 * 1000, balance = 7.12, fullResponse = "foobar 7.12"))
                insert(BalanceEntry(timestamp = 12, balance = 7.12, fullResponse = null)) // quite old
            }
    }

    @Test
    fun getAllTest() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals(5, AppDatabase.get(appContext).balanceDao().getAll().size)
    }

    @Test
    fun getLatestTest() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val latest = AppDatabase.get(appContext).balanceDao().getLatest()
        assertNotNull(latest)
        assertEquals(10.15, latest!!.balance, 0.01)
    }

    @Test
    fun getSinceTest() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val database = AppDatabase.get(appContext).balanceDao()
        assertEquals(4, database.getSince(10000).size)
    }
}