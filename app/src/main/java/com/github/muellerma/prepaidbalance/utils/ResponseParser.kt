package com.github.muellerma.prepaidbalance.utils

import android.util.Log
import com.github.muellerma.prepaidbalance.parser.AbstractParser
import com.github.muellerma.prepaidbalance.parser.concrete.*

class ResponseParser {
    companion object {
        private val TAG = ResponseParser::class.java.simpleName

        private val MATCHERS = listOf(
            NoBalanceParser(),
            KauflandMobilParser(),
            TMobileUsParser(),
            PostCurrencyParser(),
            PreCurrencyParser(),
            GenericParser()
        )

        fun getBalance(response: String?): Double? {
            if (response == null || response.trim().isEmpty()) {
                return null
            }
            val stripped = response
                .replace(',', '.')
                .replace(Regex("[^A-Za-z0-9.:$£]"), " ")

            MATCHERS.forEach { parser ->
                Log.d(TAG, "Check matcher ${parser.name}")
                val balance = when (val result = parser.parse(stripped)) {
                    is AbstractParser.ParserResult.NoMatch -> return@forEach
                    is AbstractParser.ParserResult.Match -> result.value
                }
                Log.d(TAG, "Found balance $balance")

                return balance
            }

            return null
        }
    }
}