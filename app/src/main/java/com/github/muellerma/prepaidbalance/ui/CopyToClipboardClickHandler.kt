package com.github.muellerma.prepaidbalance.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.util.Log
import androidx.preference.Preference
import com.github.muellerma.prepaidbalance.R
import com.github.muellerma.prepaidbalance.utils.getStringOrEmpty
import com.github.muellerma.prepaidbalance.utils.prefs
import com.github.muellerma.prepaidbalance.utils.showToast

class CopyToClipboardClickHandler : Preference.OnPreferenceClickListener {
    override fun onPreferenceClick(preference: Preference): Boolean {
        val context = preference.context
        val value = context.prefs().sharedPrefs.getStringOrEmpty(preference.key)

        Log.d(TAG, "Copy $value to clipboard")

        val clipboardManager = context.getSystemService(ClipboardManager::class.java)
        val clip = ClipData.newPlainText(context.getString(R.string.app_name), value)
        clipboardManager.setPrimaryClip(clip)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Avoid duplicate notifications
            // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste?hl=en#duplicate-notifications
            context.showToast(R.string.copied_to_clipboard)
        }

        return true
    }

    companion object {
        private val TAG = CopyToClipboardClickHandler::class.java.simpleName
    }
}