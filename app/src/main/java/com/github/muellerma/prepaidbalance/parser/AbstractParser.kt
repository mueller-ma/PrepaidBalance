package com.github.muellerma.prepaidbalance.parser

abstract class AbstractParser(val name: String) {
    abstract fun parse(message: String): Double?
}