package com.github.muellerma.prepaidbalance.utils

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateFormat
import androidx.preference.PreferenceManager

private const val TAG = "ExtensionFunctions"

fun Double.formatAsCurrency() = "%1\$,.2f".format(this)

fun Long.timestampForUi(context: Context): String {
    return DateFormat.getTimeFormat(context).format(this) + " " +
            DateFormat.getDateFormat(context).format(this)
}

fun String.isValidUssdCode() = matches("^\\*(\\d)+#$".toRegex())

fun Context.prefs() = PreferenceManager.getDefaultSharedPreferences(this)