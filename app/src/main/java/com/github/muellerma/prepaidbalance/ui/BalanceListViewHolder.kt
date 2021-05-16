package com.github.muellerma.prepaidbalance.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.muellerma.prepaidbalance.R

class BalanceListViewHolder internal constructor(
    inflater: LayoutInflater,
    parent: ViewGroup
) : RecyclerView.ViewHolder(inflater.inflate(R.layout.list_balance, parent, false))