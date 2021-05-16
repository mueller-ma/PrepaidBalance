package com.github.muellerma.prepaidbalance.utils

import android.content.Context
import android.text.format.DateFormat

private const val TAG = "ExtensionFunctions"

fun Double.formatAsCurrency(): String {
    return "%1\$,.2f".format(this)
}

fun Long.timestampForUi(context: Context): String {
    return DateFormat.getTimeFormat(context).format(this) + " " +
            DateFormat.getDateFormat(context).format(this)
}

fun String.isValidUssdCode(): Boolean {
    return matches("^\\*(\\d)+#$".toRegex())
}