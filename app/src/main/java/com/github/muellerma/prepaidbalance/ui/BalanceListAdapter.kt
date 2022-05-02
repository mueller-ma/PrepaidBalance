package com.github.muellerma.prepaidbalance.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.muellerma.prepaidbalance.R
import com.github.muellerma.prepaidbalance.room.BalanceEntry
import com.github.muellerma.prepaidbalance.utils.formatAsCurrency
import com.github.muellerma.prepaidbalance.utils.formatAsDiff
import com.github.muellerma.prepaidbalance.utils.timestampForUi

class BalanceListAdapter(private val context: Context) :
    RecyclerView.Adapter<BalanceListViewHolder>() {
    var balances = emptyList<BalanceEntry>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BalanceListViewHolder {
        return BalanceListViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: BalanceListViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.balance).apply {
            text = balances[position].balance.formatAsCurrency()
        }

        holder.itemView.findViewById<TextView>(R.id.difference).apply {
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

        holder.itemView.findViewById<TextView>(R.id.date).apply {
            text = balances[position].timestamp.timestampForUi(context)
        }
    }

    override fun getItemCount() = balances.size
}