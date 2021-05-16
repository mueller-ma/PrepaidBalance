package com.github.muellerma.prepaidbalance.ui

import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.github.muellerma.prepaidbalance.R
import com.github.muellerma.prepaidbalance.databinding.ActivityPreferenceBinding
import com.github.muellerma.prepaidbalance.room.AppDatabase
import com.github.muellerma.prepaidbalance.utils.isValidUssdCode
import com.github.muellerma.prepaidbalance.work.CheckBalanceWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Duration


class PreferenceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreferenceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.commit {
            add(binding.activityContent.id, MainSettingsFragment())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    class MainSettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_main)

            val thresholdValuePref = getPreference("notify_balance_under_threshold_value") as EditTextPreference
            thresholdValuePref.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            }

            val ussdCodePreference = getPreference("ussd_code")
            ussdCodePreference.setSummaryProvider { pref ->
                val currentValue = preferenceManager.sharedPreferences.getString(pref.key, "").orEmpty()
                if (!currentValue.isValidUssdCode()) {
                    getString(R.string.invalid_ussd_code)
                } else {
                    getString(R.string.ussd_code_summary, currentValue)
                }
            }

            val workPref = getPreference("periodic_checks")
            workPref.setOnPreferenceChangeListener { _, newValue ->
                WorkManager.getInstance(preferenceManager.context).apply {
                    if (newValue as Boolean) {
                        val request = PeriodicWorkRequest.Builder(
                            CheckBalanceWorker::class.java,
                            Duration.ofHours(12)
                        )
                            .setConstraints(Constraints.NONE)
                            .build()

                        enqueueUniquePeriodicWork(
                            "work",
                            ExistingPeriodicWorkPolicy.REPLACE,
                            request
                        )
                    } else {
                        cancelAllWork()
                    }
                }

                true
            }

            val clearDataPref = getPreference("clear_data")
            clearDataPref.setOnPreferenceClickListener {
                AlertDialog.Builder(it.context)
                    .setMessage(R.string.clear_current_data)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        GlobalScope.launch(Dispatchers.IO) {
                            AppDatabase.get(it.context)
                                .balanceDao()
                                .deleteAll()
                        }
                    }
                    .show()
                true
            }
        }
    }
}

fun PreferenceFragmentCompat.getPreference(key: String) =
    preferenceManager.findPreference<Preference>(key)!!