package com.github.muellerma.prepaidbalance.parser.concrete

import com.github.muellerma.prepaidbalance.parser.AbstractParser

class KauflandMobilParser : AbstractParser("Kaufland mobil Germany") {
    override fun parse(message: String): ParserResult {
        if (!message.startsWith("Dein Guthaben betraegt: ")) {
            return ParserResult.NoMatch
        }

        val value = message
            .split(" ")
            .mapNotNull { it.toDoubleOrNull() }
            .sum()

        return ParserResult.Match(value)
    }
}