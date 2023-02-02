package com.github.muellerma.prepaidbalance.parser.concrete

import com.github.muellerma.prepaidbalance.parser.RegexParser

class PostCurrencyParser : RegexParser("Currency after balance") {
    override val groupIndex: Int
        get() = 2
    override val regex: Regex
        get() = "(.*?)((\\d)+(\\.(\\d){1,2}|)) (EUR|EURO|PLN|zl|Kc)[ .](.*)".toRegex()
}