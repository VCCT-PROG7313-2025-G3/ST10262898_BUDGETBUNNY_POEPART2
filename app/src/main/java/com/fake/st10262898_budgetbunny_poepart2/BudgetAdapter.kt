package com.fake.st10262898_budgetbunny_poepart2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.BudgetFirestore
import java.text.NumberFormat
import java.util.Locale

class BudgetAdapter(
    private var budgets: List<BudgetFirestore>,
    private val onItemClick: (BudgetFirestore) -> Unit = {}
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 2
    }

    inner class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.tv_category)
        val goalAmount: TextView = itemView.findViewById(R.id.tv_amount)

        fun bind(budget: BudgetFirestore) {
            categoryName.text = budget.budgetCategory
            goalAmount.text = "Goal: ${currencyFormat.format(budget.totalBudgetGoal)}" +
                    " | Income: ${currencyFormat.format(budget.budgetIncome)}"

            itemView.setOnClickListener { onItemClick(budget) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal_card, parent, false) // Reuse your existing layout
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(budgets[position])
    }

    override fun getItemCount(): Int = budgets.size

    fun updateData(newBudgets: List<BudgetFirestore>) {
        val diffResult = DiffUtil.calculateDiff(
            BudgetDiffCallback(budgets, newBudgets)
        )
        budgets = newBudgets
        diffResult.dispatchUpdatesTo(this)
    }

    private class BudgetDiffCallback(
        private val oldList: List<BudgetFirestore>,
        private val newList: List<BudgetFirestore>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos].id == newList[newPos].id // Compare by budget ID
        }
        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos] == newList[newPos]
        }
    }
}