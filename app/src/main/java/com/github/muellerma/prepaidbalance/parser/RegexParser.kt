package com.github.muellerma.prepaidbalance.parser

abstract class RegexParser(name: String) : AbstractParser(name) {
    protected abstract val regex: Regex
    protected abstract val groupIndex: Int

    override fun parse(message: String): Double? {
        return regex.matchEntire(message)?.groups?.get(groupIndex)?.value?.toDouble()
    }
}