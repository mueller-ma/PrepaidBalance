package com.github.muellerma.prepaidbalance.parser.concrete

import com.github.muellerma.prepaidbalance.parser.AbstractParser

class KauflandMobilParser : AbstractParser("Kaufland mobil Germany") {
    override fun parse(message: String): Double? {
        if (!message.startsWith("Dein Guthaben betraegt: ")) {
            return null
        }

        return message
            .split(" ")
            .mapNotNull { it.toDoubleOrNull() }
            .sum()
    }
}