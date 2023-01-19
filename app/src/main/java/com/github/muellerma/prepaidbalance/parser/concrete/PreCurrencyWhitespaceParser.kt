package com.github.muellerma.prepaidbalance.parser.concrete

import com.github.muellerma.prepaidbalance.parser.RegexParser

class PreCurrencyWhitespaceParser : RegexParser("Currency before balance, separated by whitespace") {
    override val groupIndex: Int
        get() = 3
    override val regex: Regex
        get() = "(.*?) (CHF) ((\\d)+\\.(\\d){1,2})(.*)".toRegex()
}