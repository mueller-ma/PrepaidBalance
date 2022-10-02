package com.github.muellerma.prepaidbalance.utils

import android.util.Log

class ResponseParser {
    companion object {
        private val TAG = ResponseParser::class.java.simpleName

        private val MATCHERS = listOf(
            Matcher("Kaufland mobil Germany", "^Dein Guthaben betraegt: ((\\d)+\\.(\\d){1,2})(.*)\$".toRegex()) { groups ->
                // Get full response (group 0) and split by whitespace
                // Then convert each element to a double or filter it out.
                val values = groups?.get(0)?.value
                    ?.split(" ")
                    ?.mapNotNull { it.toDoubleOrNull() }
                    ?: return@Matcher null

                return@Matcher values.sum()
            },
            Matcher("T-Mobile US", "^(.*?)Account Balance \\\$((\\d)+\\.?(\\d)?(\\d)?)(.*)[ .](.*)\$".toRegex()) { groups ->
                return@Matcher parseRegexGroupAsDouble(groups, 2)
            },
            Matcher("Generic Euro", "^(.*?)((\\d)+\\.?(\\d)?(\\d)?)(.*) EUR[ .](.*)\$".toRegex()) { groups ->
                return@Matcher parseRegexGroupAsDouble(groups, 2)
            },
            Matcher("Generic PLN", "^(.*?)((\\d)+\\.(\\d){1,2}) PLN(.*?)\$".toRegex()) { groups ->
                return@Matcher parseRegexGroupAsDouble(groups, 2)
            },
            Matcher("Generic CHF", "^(.*?)((\\d)+\\.(\\d){1,2}) CHF(.*?)\$".toRegex()) { groups ->
                return@Matcher parseRegexGroupAsDouble(groups, 2)
            },
            Matcher("Generic", "^(.*?)((\\d)+\\.?(\\d)?(\\d)?)(.*)\$".toRegex()) { groups ->
                return@Matcher parseRegexGroupAsDouble(groups, 2)
            },
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
                    val groups = matcher
                        .regex
                        .matchEntire(withDots)
                        ?.groups
                    groups?.forEachIndexed { index, matchGroup ->
                        println("Matcher ${matcher.name}: Index '$index' ${matchGroup?.value}")
                    }
                    val balance = matcher.process(groups)
                    Log.d(TAG, "Found balance $balance")

                    return balance
                }
            }

            return null
        }

        private fun parseRegexGroupAsDouble(groups: MatchGroupCollection?, groupNumber: Int): Double? {
            return groups?.get(groupNumber)?.value?.toDouble()
        }

        private data class Matcher(
            val name: String,
            val regex: Regex,
            val process: (groups: MatchGroupCollection?) -> Double?
        )
    }
}