package com.fake.st10262898_budgetbunny_poepart2


import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fake.st10262898_budgetbunny_poepart2.data.ChatMessage

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSender: TextView = itemView.findViewById(R.id.tvSender)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val messageContainer: ViewGroup = itemView.findViewById(R.id.messageContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]


        holder.tvSender.text = if (message.isBot) "Budget Bunny" else "You"
        holder.tvMessage.text = message.message
        holder.tvTime.text = message.time


        if (message.isBot) {
            holder.messageContainer.setBackgroundResource(R.drawable.chat_bubble_bot)
            (holder.messageContainer.layoutParams as ViewGroup.MarginLayoutParams).let { params ->
                params.marginStart = 0
                params.marginEnd = dpToPx(holder.itemView.context, 64f)
                holder.messageContainer.layoutParams = params
            }
        } else {
            holder.messageContainer.setBackgroundResource(R.drawable.chat_bubble_user)
            (holder.messageContainer.layoutParams as ViewGroup.MarginLayoutParams).let { params ->
                params.marginStart = dpToPx(holder.itemView.context, 64f)
                params.marginEnd = 0
                holder.messageContainer.layoutParams = params
            }
        }
    }

    //Gets total amount of messages in the chat
    override fun getItemCount() = messages.size

    private fun dpToPx(context: android.content.Context, dp: Float): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}