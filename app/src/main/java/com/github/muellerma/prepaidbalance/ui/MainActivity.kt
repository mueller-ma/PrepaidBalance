package com.github.muellerma.prepaidbalance.ui

import android.Manifest.permission.CALL_PHONE
import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SubscriptionManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.muellerma.prepaidbalance.R
import com.github.muellerma.prepaidbalance.databinding.ActivityMainBinding
import com.github.muellerma.prepaidbalance.room.AppDatabase
import com.github.muellerma.prepaidbalance.utils.hasPermissions
import com.github.muellerma.prepaidbalance.utils.prefs
import com.github.muellerma.prepaidbalance.work.CheckBalanceWorker
import com.github.muellerma.prepaidbalance.work.CheckBalanceWorker.Companion.CheckResult
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


class MainActivity : AbstractBaseActivity(), SwipeRefreshLayout.OnRefreshListener {
    override lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase
    private var databaseLoaded = false
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { isGranted: Map<String, Boolean> ->
        if (isGranted.all { it.value }) {
            binding.swiperefresh.isRefreshing = true
            setDefaultSubscriptionId()
            onRefresh()
        } else {
            showSnackbar(R.string.permissions_required)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")
        splashScreen.setKeepOnScreenCondition { !databaseLoaded }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.get(this)

        binding.swiperefresh.setOnRefreshListener(this)

        binding.list.layoutManager = LinearLayoutManager(this)
        binding.list.adapter = BalanceListAdapter(this)

        PreferenceManager.setDefaultValues(this, R.xml.pref_main, false)
    }

    override fun onResume() {
        Log.d(TAG, "onResume()")
        updateBalanceList()
        super.onResume()
    }

    private fun updateBalanceList() {
        Log.d(TAG, "updateBalanceList()")
        launch {
            val lastOneYear = System.currentTimeMillis() - 12L * 30 * 24 * 60 * 60 * 1000
            val entries = database.balanceDao().getSince(lastOneYear)
            Handler(Looper.getMainLooper()).post {
                (binding.list.adapter as BalanceListAdapter).balances = entries
                binding.list.isVisible = entries.isNotEmpty()
                binding.hint.isVisible = entries.isEmpty()
                databaseLoaded = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d(TAG, "onCreateOptionsMenu()")
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected($item)")
        return when(item.itemId) {
            R.id.preferences -> {
                Intent(this, PreferenceActivity::class.java).apply {
                    startActivity(this)
                }
                true
            }
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRefresh() {
        Log.d(TAG, "onRefresh()")

        if (!prefs().confirmedFirstRequest) {
            val ussdCode = prefs().ussdCode
            val message = if (ussdCode.isEmpty()) {
                getString(R.string.invalid_ussd_code)
            } else {
                getString(R.string.confirm_first_request, ussdCode)
            }

            AlertDialog.Builder(this)
                .setMessage(message)
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    binding.swiperefresh.isRefreshing = false
                }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    Log.d(TAG, "Confirmed request")
                    prefs().confirmedFirstRequest = true
                    onRefresh()
                }
                .show()

            return
        }

        CheckBalanceWorker.checkBalance(this@MainActivity) { result, data ->
            Log.d(TAG, "Got result $result")
            binding.swiperefresh.isRefreshing = false

            when (result) {
                CheckResult.OK -> {
                    launch {
                        updateBalanceList()
                        data ?: return@launch
                        showSnackbar(data, Snackbar.LENGTH_INDEFINITE)
                    }
                }
                CheckResult.PARSER_FAILED -> {
                    showSnackbar(
                        getString(R.string.unable_get_balance, data),
                        Snackbar.LENGTH_INDEFINITE
                    )
                }
                CheckResult.USSD_FAILED -> {
                    showSnackbar(R.string.ussd_failed)
                }
                CheckResult.MISSING_PERMISSIONS -> {
                    requestPermissionLauncher.launch(arrayOf(CALL_PHONE, READ_PHONE_STATE))
                }
                CheckResult.USSD_INVALID -> {
                    showSnackbar(R.string.invalid_ussd_code)
                }
                CheckResult.SUBSCRIPTION_INVALID -> {
                    showSnackbar(R.string.invalid_subscription)
                }
            }
        }
    }

    private fun setDefaultSubscriptionId() {
        val subscriptionManager = getSystemService(SubscriptionManager::class.java)
        if (hasPermissions(READ_PHONE_STATE)) {
            @SuppressLint("MissingPermission") // Permission IS checked one line above
            val defaultSubscriptionId = subscriptionManager.activeSubscriptionInfoList.firstOrNull()?.subscriptionId
            prefs().subscriptionId = defaultSubscriptionId
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}