package com.github.muellerma.prepaidbalance.parser.concrete

import com.github.muellerma.prepaidbalance.parser.RegexParser

class TMobileUsParser : RegexParser("T-Mobile US") {
    override val groupIndex: Int
        get() = 2
    override val regex: Regex
        get() = "(.*?)Account Balance \\\$((\\d)+\\.?(\\d)?(\\d)?)(.*)[ .](.*)".toRegex()
}