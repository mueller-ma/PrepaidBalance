package com.github.muellerma.prepaidbalance.ui

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SubscriptionManager
import android.text.InputType
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.commit
import androidx.preference.*
import androidx.preference.Preference.OnPreferenceChangeListener
import com.github.muellerma.prepaidbalance.R
import com.github.muellerma.prepaidbalance.databinding.ActivityPreferenceBinding
import com.github.muellerma.prepaidbalance.room.AppDatabase
import com.github.muellerma.prepaidbalance.utils.hasPermissions
import com.github.muellerma.prepaidbalance.utils.isValidUssdCode
import com.github.muellerma.prepaidbalance.utils.prefs
import com.github.muellerma.prepaidbalance.work.CheckBalanceWorker
import com.mikepenz.aboutlibraries.LibsBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PreferenceActivity : AbstractBaseActivity() {
    override lateinit var binding: ActivityPreferenceBinding

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
        private val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                addSubscriptionList()
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_main)

            val thresholdValuePref = getPreference("notify_balance_under_threshold_value") as EditTextPreference
            thresholdValuePref.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            }

            val ussdCodePreference = getPreference("ussd_code")
            ussdCodePreference.setSummaryProvider { pref ->
                val context = pref.context
                val currentValue = context.prefs().ussdCode
                if (currentValue.isValidUssdCode()) {
                    getString(R.string.ussd_code_summary, currentValue)
                } else {
                    getString(R.string.invalid_ussd_code)
                }
            }

            if (requireContext().hasPermissions(Manifest.permission.READ_PHONE_STATE)) {
                addSubscriptionList()
            } else {
                getPreference("subscription_id").isEnabled = false
                requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            }

            val updateWorkerListener = OnPreferenceChangeListener { _, _ ->
                Handler(Looper.getMainLooper()).post {
                    CheckBalanceWorker.enqueueOrCancel(preferenceManager.context)
                }

                true
            }

            getPreference("periodic_checks").onPreferenceChangeListener = updateWorkerListener
            getPreference("periodic_checks_rate").onPreferenceChangeListener = updateWorkerListener

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
                // Set as value to make it accessible to CopyToClipboardClickHandler
                context.prefs().providerCodes = codes
                onPreferenceClickListener = CopyToClipboardClickHandler()
            }

            getPreference("last_ussd_response").apply {
                summary = context.prefs().lastUssdResponse

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

        private fun addSubscriptionList() {
            if (!requireContext().hasPermissions(Manifest.permission.READ_PHONE_STATE)) {
                return
            }
            val subscriptionManager = requireContext().getSystemService(SubscriptionManager::class.java)
            val (subscriptionIds, carrierNames) = subscriptionManager.activeSubscriptionInfoList.let { subscriptions ->
                val subscriptionIds = subscriptions.map { "${it.subscriptionId}" }.toTypedArray()
                val carrierNames = subscriptions.map { it.carrierName }.toTypedArray()
                subscriptionIds to carrierNames
            }

            val subscriptionIdPref = getPreference("subscription_id") as ListPreference
            subscriptionIdPref.isEnabled = true
            subscriptionIdPref.entries = carrierNames
            subscriptionIdPref.entryValues = subscriptionIds
        }
    }
}

fun PreferenceFragmentCompat.getPreference(key: String) =
    preferenceManager.findPreference<Preference>(key)!!