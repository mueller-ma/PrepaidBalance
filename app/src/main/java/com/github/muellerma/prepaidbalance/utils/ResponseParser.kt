package com.github.muellerma.prepaidbalance.utils

class ResponseParser {
    companion object {
        private val TAG = ResponseParser::class.java.simpleName

        private val MATCHERS = listOf(
            MATCHER("^(.*?)((\\d)+\\.?(\\d)?(\\d)?)(.*) EUR (.*)".toRegex(), 2),
            MATCHER("^(.*?)((\\d)+\\.?(\\d)?(\\d)?)(.*)\$".toRegex(), 2)
        )

        fun getBalance(response: String?): Double? {
            if (response == null || response.trim().isEmpty()) {
                return null
            }
            val withDots = response.replace(',', '.')

            MATCHERS.forEach { matcher ->
                println("Check matcher $matcher") // TODO Log.d()
                if (withDots.matches(matcher.regex)) {
                    return matcher
                        .regex
                        .matchEntire(withDots)
                        ?.groups
                        ?.get(matcher.groupContainsBalance)
                        ?.value
                        ?.toDouble()
                        .also { println("Found balance $it") }
                }
            }

            return null
        }

        private data class MATCHER(val regex: Regex, val groupContainsBalance: Int)
    }
}