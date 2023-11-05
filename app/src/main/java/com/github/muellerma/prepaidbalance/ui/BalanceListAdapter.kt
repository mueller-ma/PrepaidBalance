package com.github.muellerma.prepaidbalance.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.muellerma.prepaidbalance.R
import com.github.muellerma.prepaidbalance.databinding.ListBalanceBinding
import com.github.muellerma.prepaidbalance.room.AppDatabase
import com.github.muellerma.prepaidbalance.room.BalanceEntry
import com.github.muellerma.prepaidbalance.utils.formatAsCurrency
import com.github.muellerma.prepaidbalance.utils.formatAsDiff
import com.github.muellerma.prepaidbalance.utils.showToast
import com.github.muellerma.prepaidbalance.utils.timestampForUi
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BalanceListAdapter(val activity: MainActivity) :
    RecyclerView.Adapter<BalanceListViewHolder>() {
    var balances = emptyList<BalanceEntry>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private val inflater = LayoutInflater.from(activity)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BalanceListViewHolder {
        return BalanceListViewHolder(ListBalanceBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: BalanceListViewHolder, position: Int) {
        holder.binding.balance.text = balances[position].balance.formatAsCurrency()

        holder.binding.difference.apply {
            if (position == balances.lastIndex) {
                text = ""
                return@apply
            }

            val previous = balances[position + 1].balance
            val diff = balances[position].balance - previous
            text = diff.formatAsDiff()
            @ColorRes val color = if (diff < 0) R.color.red else R.color.green
            setTextColor(ContextCompat.getColor(context, color))
        }

        val fullResponse = balances[position].fullResponse
        holder.binding.card.setOnClickListener {
            if (fullResponse.isNullOrEmpty()) {
                it.context.showToast(R.string.no_response_saved)
            } else {
                MaterialAlertDialogBuilder(it.context)
                    .setPositiveButton(R.string.close, null)
                    .setMessage(fullResponse)
                    .show()
            }
        }
        holder.binding.card.setOnLongClickListener {
            val balance = balances[position]
            MaterialAlertDialogBuilder(it.context)
                .setPositiveButton(R.string.delete) { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        AppDatabase.get(it.context).balanceDao().delete(balance)
                        this@BalanceListAdapter.activity.updateBalanceList()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setMessage(it.context.getString(R.string.delete_entry, balance.balance.formatAsCurrency()))
                .show()
            true
        }

        holder.binding.date.apply {
            text = balances[position].timestamp.timestampForUi(context)
        }
    }

    override fun getItemCount() = balances.size
}