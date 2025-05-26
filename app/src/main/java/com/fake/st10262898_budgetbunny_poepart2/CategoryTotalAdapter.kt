package com.fake.st10262898_budgetbunny_poepart2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.CategoryTotalFirestore
import java.text.NumberFormat
import java.util.Locale

class CategoryTotalAdapter(
    private var categoryTotals: List<CategoryTotalFirestore>,
    private val onItemClick: (CategoryTotalFirestore) -> Unit = {}
) : RecyclerView.Adapter<CategoryTotalAdapter.CategoryTotalViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 2
    }

    inner class CategoryTotalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.tv_category)
        val totalAmount: TextView = itemView.findViewById(R.id.tv_amount)

        fun bind(categoryTotal: CategoryTotalFirestore) {
            categoryName.text = categoryTotal.budgetCategory
            totalAmount.text = currencyFormat.format(categoryTotal.total)

            itemView.setOnClickListener {
                onItemClick(categoryTotal)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryTotalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal_card, parent, false)
        return CategoryTotalViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryTotalViewHolder, position: Int) {
        holder.bind(categoryTotals[position])
    }

    override fun getItemCount(): Int = categoryTotals.size

    fun updateData(newCategoryTotals: List<CategoryTotalFirestore>) {
        val diffResult = DiffUtil.calculateDiff(
            CategoryTotalDiffCallback(categoryTotals, newCategoryTotals)
        )
        categoryTotals = newCategoryTotals
        diffResult.dispatchUpdatesTo(this)
    }

    private class CategoryTotalDiffCallback(
        private val oldList: List<CategoryTotalFirestore>,
        private val newList: List<CategoryTotalFirestore>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos].budgetCategory == newList[newPos].budgetCategory
        }
        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos] == newList[newPos]
        }
    }
}