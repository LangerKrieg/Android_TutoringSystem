package chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.tutoringsystem.R
import models.Chat

class ChatListAdapter(private val context: Context, private val chatList: List<Chat>) : BaseAdapter() {

    override fun getCount(): Int = chatList.size

    override fun getItem(position: Int): Chat = chatList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false)

        return view
    }
}
