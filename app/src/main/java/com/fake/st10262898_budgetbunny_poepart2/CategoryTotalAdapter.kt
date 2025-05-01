package com.fake.st10262898_budgetbunny_poepart2

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.CategoryTotal

class CategoryTotalAdapter (private val categoryTotals: List<CategoryTotal>) :
    RecyclerView.Adapter<CategoryTotalAdapter.CategoryTotalViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryTotalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_total, parent, false)
        return CategoryTotalViewHolder(view)
    }


    override fun onBindViewHolder(holder: CategoryTotalViewHolder, position: Int) {
        val categoryTotal = categoryTotals[position]
        Log.d("ADAPTER_BIND", "Binding category: ${categoryTotal.budgetCategory}, Total: ${categoryTotal.total}")
        holder.bind(categoryTotal)
    }

    override fun getItemCount(): Int = categoryTotals.size

    class CategoryTotalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryTextView: TextView = itemView.findViewById(R.id.tvCategory)
        private val totalTextView: TextView = itemView.findViewById(R.id.tvTotal)

        fun bind(categoryTotal: CategoryTotal) {
            categoryTextView.text = categoryTotal.budgetCategory
            totalTextView.text = "R${categoryTotal.total}"
        }
    }

}