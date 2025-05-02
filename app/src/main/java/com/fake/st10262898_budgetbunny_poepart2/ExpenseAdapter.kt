package com.fake.st10262898_budgetbunny_poepart2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.Expense

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private val onItemClick: (Expense) -> Unit // Add this parameter
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val expenseName: TextView = itemView.findViewById(R.id.tv_expense_name)
        val expenseAmount: TextView = itemView.findViewById(R.id.tv_expense_amount)
        // Removed date and category views as they're not needed
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false) // Changed to simple layout
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.expenseName.text = expense.expenseName
        holder.expenseAmount.text = "R ${expense.expenseAmount}"

        holder.itemView.setOnClickListener {
            onItemClick(expense)
        }
    }

    override fun getItemCount(): Int = expenses.size

    // Keep only one update method (removed redundant submitList)
    fun updateExpenses(newExpenses: List<Expense>) {
        val diffResult = DiffUtil.calculateDiff(ExpenseDiffCallback(expenses, newExpenses))
        expenses = newExpenses
        diffResult.dispatchUpdatesTo(this)
    }

    class ExpenseDiffCallback(
        private val oldList: List<Expense>,
        private val newList: List<Expense>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos].id == newList[newPos].id
        }
        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos] == newList[newPos]
        }
    }
}