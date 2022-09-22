package com.github.muellerma.prepaidbalance.ui

import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.commit
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.WorkManager
import com.github.muellerma.prepaidbalance.R
import com.github.muellerma.prepaidbalance.databinding.ActivityPreferenceBinding
import com.github.muellerma.prepaidbalance.room.AppDatabase
import com.github.muellerma.prepaidbalance.utils.isValidUssdCode
import com.github.muellerma.prepaidbalance.utils.prefs
import com.github.muellerma.prepaidbalance.work.CheckBalanceWorker
import com.mikepenz.aboutlibraries.LibsBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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
                onBackPressedDispatcher.onBackPressed()
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
                val currentValue = pref.sharedPreferences!!.getString(pref.key, "").orEmpty()
                if (!currentValue.isValidUssdCode()) {
                    getString(R.string.invalid_ussd_code)
                } else {
                    getString(R.string.ussd_code_summary, currentValue)
                }
            }

            val workPref = getPreference("periodic_checks")
            workPref.setOnPreferenceChangeListener { _, newValue ->
                val context = preferenceManager.context

                if (newValue as Boolean) {
                    CheckBalanceWorker.enqueue(context)
                } else {
                    WorkManager
                        .getInstance(preferenceManager.context)
                        .cancelAllWork()
                }

                true
            }

            val clearDataPref = getPreference("clear_data")
            clearDataPref.setOnPreferenceClickListener {
                AlertDialog.Builder(it.context)
                    .setMessage(R.string.clear_current_data)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            AppDatabase.get(it.context)
                                .balanceDao()
                                .deleteAll()
                        }
                    }
                    .show()
                true
            }

            getPreference("provider_codes").apply {
                val config = preferenceManager.context.resources.configuration
                val codes = "MCC: ${config.mcc}\nMNC: ${config.mnc}"
                summary = codes
                context.prefs().edit {
                    // Set as value to make it accessible to CopyToClipboardClickHandler
                    putString("provider_codes", codes)
                }
                onPreferenceClickListener = CopyToClipboardClickHandler()
            }

            getPreference("last_ussd_response").apply {
                summary = context.prefs().getString("last_ussd_response", "")
                onPreferenceClickListener = CopyToClipboardClickHandler()
            }

            getPreference("about").apply {
                setOnPreferenceClickListener {
                    val fragment = LibsBuilder()
                        .withAboutIconShown(true)
                        .withAboutVersionShownName(true)
                        .withSortEnabled(true)
                        .withListener(AboutButtonsListener())
                        .supportFragment()

                    parentFragmentManager.commit {
                        addToBackStack("about")
                        val prefActivity = requireActivity() as PreferenceActivity
                        replace(prefActivity.binding.activityContent.id, fragment, "about")
                    }
                    true
                }
            }
        }
    }
}

fun PreferenceFragmentCompat.getPreference(key: String) =
    preferenceManager.findPreference<Preference>(key)!!