package com.github.muellerma.prepaidbalance

import com.github.muellerma.prepaidbalance.utils.formatAsCurrency
import com.github.muellerma.prepaidbalance.utils.formatAsDiff
import com.github.muellerma.prepaidbalance.utils.isValidUssdCode
import org.junit.Assert.*
import org.junit.Test

class ExtensionFunctionsTest {
    @Test
    fun testIsValidUssdCode() {
        assertFalse("".isValidUssdCode())
        assertFalse("abc".isValidUssdCode())
        assertFalse("*abc#".isValidUssdCode())
        assertFalse("123".isValidUssdCode())
        assertFalse("*123".isValidUssdCode())
        assertFalse("*#".isValidUssdCode())
        assertFalse("1*123#".isValidUssdCode())
        assertFalse("*123#1".isValidUssdCode())

        assertTrue("*123#".isValidUssdCode())
        assertTrue("*1#".isValidUssdCode())
        assertTrue("*0#".isValidUssdCode())
        assertTrue("#0#".isValidUssdCode())
        assertTrue("##0#".isValidUssdCode())
        assertTrue("##0*#".isValidUssdCode())
        assertTrue("#*0#".isValidUssdCode())
        assertTrue("*#0#".isValidUssdCode())
        assertTrue("#123#".isValidUssdCode()) // Truemove-H Thailand
        assertTrue("*123*8*2#".isValidUssdCode())
    }

    @Test
    fun testFormatAsCurrency() {
        //val dfs = DecimalFormatSymbols(Locale.getDefault()).decimalSeparator
        val dfs = "." // TODO
        assertEquals("0${dfs}00", 0.0.formatAsCurrency())
        assertEquals("42${dfs}00", 42.0.formatAsCurrency())
        assertEquals("42${dfs}10", 42.1.formatAsCurrency())
        assertEquals("42${dfs}12", 42.12.formatAsCurrency())
        assertEquals("42${dfs}12", 42.123.formatAsCurrency())
    }

    @Test
    fun testFormatAsDiff() {
        val dfs = "." // TODO
        assertEquals("+0${dfs}00", 0.0.formatAsDiff())
        assertEquals("+42${dfs}00", 42.0.formatAsDiff())
        assertEquals("-42${dfs}00", (-42.0).formatAsDiff())
    }
}