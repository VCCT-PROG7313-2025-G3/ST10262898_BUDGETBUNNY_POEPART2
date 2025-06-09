package com.fake.st10262898_budgetbunny_poepart2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.ExpenseFirebase
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(
    private var expenses: List<ExpenseFirebase>,
    private val onItemClick: (ExpenseFirebase) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val expenseName: TextView = itemView.findViewById(R.id.tv_expense_name)
        val expenseAmount: TextView = itemView.findViewById(R.id.tv_expense_amount)
        val expenseDate: TextView = itemView.findViewById(R.id.tv_expense_date)
        val expenseCategory: TextView = itemView.findViewById(R.id.tv_expense_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.expense_item, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.expenseName.text = expense.expenseName
        holder.expenseAmount.text = "R %.2f".format(expense.expenseAmount)

        val dateText = if (expense.expenseDate > 0L) {
            val date = Date(expense.expenseDate)
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
        } else {
            "No date"
        }
        holder.expenseDate.text = dateText

        holder.expenseCategory.text = expense.expenseCategory ?: "Uncategorized"

        holder.itemView.setOnClickListener {
            onItemClick(expense)
        }
    }

    override fun getItemCount(): Int = expenses.size

    fun updateExpenses(newExpenses: List<ExpenseFirebase>) {
        val diffResult = DiffUtil.calculateDiff(ExpenseDiffCallback(expenses, newExpenses))
        expenses = newExpenses
        diffResult.dispatchUpdatesTo(this)
    }

    class ExpenseDiffCallback(
        private val oldList: List<ExpenseFirebase>,
        private val newList: List<ExpenseFirebase>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {

            val oldItem = oldList[oldPos]
            val newItem = newList[newPos]
            return oldItem.expenseName == newItem.expenseName &&
                    oldItem.expenseDate == newItem.expenseDate
        }
        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos] == newList[newPos]
        }
    }
}
