package com.github.muellerma.prepaidbalance.parser.concrete

import com.github.muellerma.prepaidbalance.parser.RegexParser

class PreCurrencyParser : RegexParser("Currency before balance") {
    override val groupIndex: Int
        get() = 3
    override val regex: Regex
        get() = "(.*?) (£|\$)((\\d)+\\.(\\d){1,2})(.*)".toRegex()
}