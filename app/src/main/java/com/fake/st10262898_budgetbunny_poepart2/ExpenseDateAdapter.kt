package com.fake.st10262898_budgetbunny_poepart2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.Expense
import java.text.SimpleDateFormat
import java.util.*


class ExpenseDateAdapter (private var expenseList: List<Expense>) :

    RecyclerView.Adapter<ExpenseDateAdapter.ExpenseDateViewHolder>() {

        class ExpenseDateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val expenseName: TextView = itemView.findViewById(R.id.tv_expense_name)
            val expenseAmount: TextView = itemView.findViewById(R.id.tv_expense_amount)
            val expenseDate: TextView = itemView.findViewById(R.id.tv_expense_date)
            val expenseCategory: TextView = itemView.findViewById(R.id.tv_expense_category)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseDateViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.expense_item, parent, false)
            return ExpenseDateViewHolder(view)
        }

        override fun onBindViewHolder(holder: ExpenseDateViewHolder, position: Int) {
            val expense = expenseList[position]
            holder.expenseName.text = expense.expenseName
            holder.expenseAmount.text = "R ${expense.expenseAmount}"

            val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(Date(expense.expenseDate))
            holder.expenseDate.text = date

            holder.expenseCategory.text = expense.expenseCategory ?: "Uncategorized"
        }

        override fun getItemCount(): Int = expenseList.size

        fun submitList(newExpenses: List<Expense>) {
            val diffResult = DiffUtil.calculateDiff(ExpenseDiffCallback(expenseList, newExpenses))
            expenseList = newExpenses
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