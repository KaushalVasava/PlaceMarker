package com.lahsuak.placemarker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lahsuak.placemarker.R
import com.lahsuak.placemarker.models.UserMap

class MapsAdapter(val context: Context,val mapsList: List<UserMap>,val listener:MapsListener) :
    RecyclerView.Adapter<MapsAdapter.MapsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapsViewHolder {
          val view = LayoutInflater.from(parent.context).inflate(R.layout.maps_item,parent,false)
          return MapsViewHolder(view)
    }
    override fun onBindViewHolder(holder: MapsViewHolder, position: Int) {
        holder.title.text= mapsList[position].title
        holder.itemView.setOnClickListener {
            listener.onItemClick(position)
        }
    }
    override fun getItemCount(): Int {
        return mapsList.size
    }
    class MapsViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val title = itemView.findViewById<TextView>(R.id.maps_name)
    }
}

interface MapsListener {
    fun onItemClick(position: Int)
}
