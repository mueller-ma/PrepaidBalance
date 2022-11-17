package com.github.muellerma.prepaidbalance.ui

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class AbstractBaseActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + Job()
    protected abstract val binding: ViewBinding

    protected fun showSnackbar(@StringRes message: Int, @BaseTransientBottomBar.Duration length: Int = Snackbar.LENGTH_LONG) {
        showSnackbar(getString(message), length)
    }

    protected fun showSnackbar(message: String, @BaseTransientBottomBar.Duration length: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(binding.root, message, length).show()
    }
}