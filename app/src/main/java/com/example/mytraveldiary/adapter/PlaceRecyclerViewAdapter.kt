package com.example.mytraveldiary.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mytraveldiary.databinding.ListItemMainBinding
import com.example.mytraveldiary.model.Place
import com.example.mytraveldiary.view.MapsActivity

class PlaceRecyclerViewAdapter(val placeList: List<Place>):
    RecyclerView.Adapter<PlaceRecyclerViewAdapter.PlaceRecyclerViewHolder>() {

    class PlaceRecyclerViewHolder(val listItemMainBinding: ListItemMainBinding) : RecyclerView.ViewHolder(listItemMainBinding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceRecyclerViewHolder {
        val listItemMainBinding = ListItemMainBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PlaceRecyclerViewHolder(listItemMainBinding)
    }

    override fun onBindViewHolder(holder: PlaceRecyclerViewHolder, position: Int) {
        holder.listItemMainBinding.listItemTextview.text = placeList[position].name
        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context,MapsActivity::class.java)
            intent.putExtra("selectedPlace",placeList.get(position))
            intent.putExtra("info","old")
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return placeList.size
    }

}