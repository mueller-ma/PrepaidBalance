package com.github.muellerma.prepaidbalance.parser

abstract class AbstractParser(val name: String) {
    abstract fun parse(message: String): ParserResult

    sealed class ParserResult {
        object NoMatch : ParserResult()
        class Match(val value: Double?) : ParserResult()
    }
}