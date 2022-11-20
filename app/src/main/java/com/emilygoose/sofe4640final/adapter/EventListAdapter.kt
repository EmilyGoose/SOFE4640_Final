package com.emilygoose.sofe4640final.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emilygoose.sofe4640final.R
import com.emilygoose.sofe4640final.data.Event

class EventListAdapter(private val dataSet: ArrayList<Event>) : RecyclerView.Adapter<EventListAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventTitle: TextView = view.findViewById(R.id.label_upcoming_band)
        val eventDate: TextView = view.findViewById(R.id.label_upcoming_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_upcoming, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = dataSet[position]

        holder.eventTitle.text = event.name
        holder.eventDate.text = event.dates.start.localDate
    }

    override fun getItemCount() = dataSet.size
}