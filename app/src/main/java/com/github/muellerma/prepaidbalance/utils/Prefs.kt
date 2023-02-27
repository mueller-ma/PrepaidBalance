package com.github.muellerma.prepaidbalance.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.github.muellerma.prepaidbalance.R

class Prefs(val context: Context) {
    val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var confirmedFirstRequest: Boolean
        get() = sharedPrefs.getBoolean("confirmed_first_request", false)
        set(value) {
            sharedPrefs.edit {
                putBoolean("confirmed_first_request", value)
            }
        }

    val ussdCode: String
        get() = sharedPrefs.getStringOrEmpty("ussd_code")

    var subscriptionId: Int?
        get() = sharedPrefs.getString("subscription_id", null)?.toIntOrNull()
        set(value) {
            sharedPrefs.edit {
                putString("subscription_id", "$value")
            }
        }

    var providerCodes: String
        get() = sharedPrefs.getStringOrEmpty("provider_codes")
        set(value) {
            sharedPrefs.edit {
                putString("provider_codes", value)
            }
        }

    var lastUssdResponse: String?
        get() = sharedPrefs.getStringOrEmpty("last_ussd_response")
        set(value) {
            sharedPrefs.edit {
                putString("last_ussd_response", value)
            }
        }

    var lastUpdateTimestamp: Long
        get() = sharedPrefs.getLong("last_update_timestamp", 0)
        set(value) {
            sharedPrefs.edit {
                putLong("last_update_timestamp", value)
            }
        }

    val notifyBalanceUnderThreshold: Boolean
        get() = sharedPrefs.getBoolean("notify_balance_under_threshold", false)

    val notifyBalanceUnderThresholdValue: Double
        get() = sharedPrefs
            .getStringOrEmpty("notify_balance_under_threshold_value")
            .replace(',', '.')
            .toDoubleOrNull() ?: Double.MAX_VALUE

    val notifyBalanceIncreased: Boolean
        get() = sharedPrefs.getBoolean("notify_balance_increased", false)

    val periodicCheckRateHours: Long?
        get() = sharedPrefs.getStringOrEmpty("periodic_checks_rate").toLongOrNull() ?:
            context.getString(R.string.periodic_checks_rate_twice_a_day_value).toLongOrNull()

    val periodicCheck: Boolean
        get() = sharedPrefs.getBoolean("periodic_checks", false)
}

fun Context.prefs() = Prefs(this)

fun SharedPreferences.getStringOrEmpty(key: String) = getString(key, "") ?: ""