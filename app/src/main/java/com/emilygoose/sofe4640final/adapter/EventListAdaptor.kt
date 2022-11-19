package com.emilygoose.sofe4640final.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emilygoose.sofe4640final.R
import com.emilygoose.sofe4640final.VenueDetailActivity
import com.emilygoose.sofe4640final.data.Venue
import com.squareup.picasso.Picasso

class VenueListAdapter(private val dataSet: ArrayList<Venue>) :
    RecyclerView.Adapter<VenueListAdapter.ViewHolder>() {
    class ViewHolder( // Declare and find all the views by ID
        val view: View
    ) : RecyclerView.ViewHolder(view) {
        val upcomingBand: TextView = view.findViewById(R.id.label_upcoming_band)
        val upcomingDate: TextView = view.findViewById(R.id.label_upcoming_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_upcoming, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val venue = dataSet[position]

        // Set title and distance
        holder.upcomingBand.text = event.name
        // todo grab string resource properly
        holder.upcomingDate.text =
            holder.view.context.getString(R.string.label_distance, event.dates)

        // Set click listener for the view
        holder.view.setOnClickListener {
            val context = holder.view.context
            // Launch venue detail activity with the venue ID
            val intent = Intent(context, VenueDetailActivity::class.java)
            intent.putExtra("ID", venue.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = dataSet.size
}