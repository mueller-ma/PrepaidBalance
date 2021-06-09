package com.github.muellerma.prepaidbalance.utils

import android.util.Log

class ResponseParser {
    companion object {
        private val TAG = ResponseParser::class.java.simpleName

        private val MATCHERS = listOf(
            MATCHER("^(.*?)((\\d)+\\.?(\\d)?(\\d)?)(.*) EUR[ ,.](.*)".toRegex(), 2),
            MATCHER("^(.*?)((\\d)+\\.?(\\d)?(\\d)?)(.*)\$".toRegex(), 2)
        )

        fun getBalance(response: String?): Double? {
            if (response == null || response.trim().isEmpty()) {
                return null
            }
            val withDots = response
                .replace(',', '.')
                .replace("\n", " ")

            MATCHERS.forEach { matcher ->
                Log.d(TAG, "Check matcher $matcher")
                if (withDots.matches(matcher.regex)) {
                    return matcher
                        .regex
                        .matchEntire(withDots)
                        ?.groups
                        ?.get(matcher.groupContainsBalance)
                        ?.value
                        ?.toDouble()
                        .also { Log.d(TAG, "Found balance $it") }
                }
            }

            return null
        }

        private data class MATCHER(val regex: Regex, val groupContainsBalance: Int)
    }
}