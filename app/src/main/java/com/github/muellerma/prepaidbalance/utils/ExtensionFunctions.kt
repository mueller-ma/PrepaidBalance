package com.github.muellerma.prepaidbalance.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import com.github.muellerma.prepaidbalance.R

private const val TAG = "ExtensionFunctions"

fun Double.formatAsCurrency() = "%1\$,.2f".format(this)

fun Double.formatAsDiff(): String {
    if (this >= 0) {
        return "+${formatAsCurrency()}"
    }

    return formatAsCurrency()
}

fun Long.timestampForUi(context: Context): String {
    return DateFormat.getTimeFormat(context).format(this) + " " +
            DateFormat.getDateFormat(context).format(this)
}

fun String.isValidUssdCode() = matches("^([*#][*#]?)([\\d*])+([*#][*#]?)$".toRegex())

fun String.openInBrowser(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(this))
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.d(TAG, "Unable to open url in browser: $intent")
        context.showToast(R.string.error_no_browser_found)
    }
}

fun Context.showToast(@StringRes msg: Int) {
    Toast
        .makeText(this, msg, Toast.LENGTH_SHORT)
        .show()
}

fun Context.hasPermissions(vararg permissions: String): Boolean {
    return permissions
        .map { permission -> ActivityCompat.checkSelfPermission(this, permission) }
        .all { result -> result == PackageManager.PERMISSION_GRANTED }
}