package com.github.muellerma.prepaidbalance.parser.concrete

import com.github.muellerma.prepaidbalance.parser.RegexParser

class GenericParser : RegexParser("Generic") {
    override val groupIndex: Int
        get() = 2
    override val regex: Regex
        get() = "(.*?)((\\d)+\\.?(\\d)?(\\d)?)(.*)".toRegex()
}