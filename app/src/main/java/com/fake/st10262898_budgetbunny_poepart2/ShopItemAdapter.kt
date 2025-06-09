import com.fake.st10262898_budgetbunny_poepart2.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.ShopItem
import android.content.Context

class ShopItemAdapter(
    private val items: List<ShopItem>,
    private val onItemClick: (ShopItem) -> Unit
) : RecyclerView.Adapter<ShopItemAdapter.ViewHolder>() {

    private val imageResourceMap = mapOf(
        "jumpsuit_1" to R.drawable.jumpsuit_1,
        "jumpsuit_2" to R.drawable.jumpsuit_2,
        "blue_shirt" to R.drawable.clothes_blue_shirt,
        "brat_shirt" to R.drawable.clothes_brat,
        "cap" to R.drawable.clothes_cap,
        "KCP_shirt" to R.drawable.clothes_kcp,
        "coffee_shirt" to R.drawable.clothes_shirt_coffee,
        "sideman_hoodie" to R.drawable.clothes_sideman,
        "skirt" to R.drawable.clothes_skirt
    )

    private val filteredItems = items.toMutableList()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.itemImage)
        val price: TextView = view.findViewById(R.id.itemPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shop, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredItems[position]
        val resourceId = imageResourceMap[item.imageName] ?: R.drawable.ic_launcher_foreground // Default fallback image

        holder.image.apply {
            setImageResource(resourceId)
            scaleType = ImageView.ScaleType.CENTER_CROP
            adjustViewBounds = false
        }

        holder.price.text = "${item.price} coins"
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = filteredItems.size

    fun removeItem(item: ShopItem) {
        val position = filteredItems.indexOfFirst { it.id == item.id }
        if (position != -1) {
            filteredItems.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}