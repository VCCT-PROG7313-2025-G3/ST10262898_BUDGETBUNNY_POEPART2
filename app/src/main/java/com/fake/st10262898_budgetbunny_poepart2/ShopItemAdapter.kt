package com.fake.st10262898_budgetbunny_poepart2

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.ShopItem

class ShopItemAdapter(
    private val items: MutableList<ShopItem>,
    private val onBuyClicked: (ShopItem) -> Unit
) : RecyclerView.Adapter<ShopItemAdapter.ShopItemViewHolder>() {

    inner class ShopItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.itemImage)
        val priceText: TextView = view.findViewById(R.id.itemPrice)
        val buyButton: Button = view.findViewById(R.id.buyButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shop_item_layout, parent, false)
        Log.d("ShopAdapter", "Inflated shop_item_layout")
        return ShopItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopItemViewHolder, position: Int) {
        val item = items[position]
        Log.d("ShopAdapter", "Binding item: ${item.id}, Price: ${item.price}")
        holder.imageView.setImageResource(item.imageRes)
        holder.priceText.text = "Price: ${item.price} coins"
        holder.buyButton.setOnClickListener {
            Log.d("ShopAdapter", "Buy button clicked for item: ${item.id}")
            onBuyClicked(item)
        }
    }

    override fun getItemCount() = items.size

    fun removeItem(item: ShopItem) {
        val pos = items.indexOf(item)
        if (pos >= 0) {
            items.removeAt(pos)
            notifyItemRemoved(pos)
        }
    }
}



