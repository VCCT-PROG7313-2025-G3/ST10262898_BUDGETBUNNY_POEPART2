package com.fake.st10262898_budgetbunny_poepart2.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.R
import com.fake.st10262898_budgetbunny_poepart2.data.Expense

class ExpenseAdapter(private var expenseList: List<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val expenseName: TextView = itemView.findViewById(R.id.tv_expense_name)
        val expenseAmount: TextView = itemView.findViewById(R.id.tv_expense_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.expenseName.text = expense.expenseName
        holder.expenseAmount.text = "R ${expense.expenseAmount}"
    }

    override fun getItemCount(): Int = expenseList.size

    // Method to update the expense list
    fun updateExpenses(newExpenses: List<Expense>) {
        expenseList = newExpenses
        notifyDataSetChanged()  // Refresh the RecyclerView
    }
}