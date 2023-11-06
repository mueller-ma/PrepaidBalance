package com.github.muellerma.prepaidbalance.parser.concrete

import com.github.muellerma.prepaidbalance.parser.AbstractParser

/**
 * Parser to catch known messages that don't contain a balance. Otherwise phone numbers might be recognized as balance.
 */
class NoBalanceParser : AbstractParser("Response without balance") {
    override fun parse(message: String): ParserResult {
        val isMessageWithoutBalance = NO_BALANCE_START.any { message.startsWith(it, ignoreCase = true) }
        return if (isMessageWithoutBalance) ParserResult.Match(null) else ParserResult.NoMatch
    }

    companion object {
        // The char replacement that is done in ReponseParser.getBalance() must be done here manually
        private val NO_BALANCE_START = listOf(
            "Tu solicitud no puede ser tramitada en este momento",
            "Dieser Service steht auf Grund von Wartungsarbeiten leider erst",
            "Wir k nnen Deine Anfrage derzeit leider nicht bearbeiten",
        )
    }
}