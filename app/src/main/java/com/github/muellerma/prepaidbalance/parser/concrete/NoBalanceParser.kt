package com.github.muellerma.prepaidbalance.parser.concrete

import com.github.muellerma.prepaidbalance.parser.AbstractParser

/**
 * Parser to catch known messages that don't contain a balance. Otherwise phone numbers might be recognized as balance.
 */
class NoBalanceParser : AbstractParser("Response without balance") {
    override fun parse(message: String): ParserResult {
        val isMessageWithoutBalance = NO_BALANCE_START.any { message.startsWith(it) }
        return if (isMessageWithoutBalance) ParserResult.Match(null) else ParserResult.NoMatch
    }

    companion object {
        private val NO_BALANCE_START = listOf(
            "In this moment we cannot process your transaction"
        )
    }
}