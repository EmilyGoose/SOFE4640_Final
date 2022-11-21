package com.emilygoose.sofe4640final.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emilygoose.sofe4640final.R
import com.emilygoose.sofe4640final.data.Event
import com.squareup.picasso.Picasso

// This is basically VenueListAdapter reused to show events on the main page
class UpcomingEventAdapter(private val dataSet: ArrayList<Event>) :
    RecyclerView.Adapter<UpcomingEventAdapter.ViewHolder>() {
    class ViewHolder( // Declare and find all the views by ID
        view: View
    ) : RecyclerView.ViewHolder(view) {
        val venueImageView: ImageView = view.findViewById(R.id.image_venue)
        val venueTitle: TextView = view.findViewById(R.id.label_venue_name)
        val venueDistance: TextView = view.findViewById(R.id.label_venue_distance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_venue, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = dataSet[position]

        // Set Title and Venue name
        holder.venueTitle.text = event.name
        holder.venueDistance.text = event.place?.name

        Log.d("BindImage", "Adding image to card")
        // Make sure images exist
        if (event.images.isNotEmpty()) {
            // Load first venue image into ImageView with Picasso
            Picasso.get().load(event.images[0].url).resize(250, 250).centerCrop()
                .into(holder.venueImageView)
        }

        // Todo click listener to open event detail view
    }

    override fun getItemCount() = dataSet.size
}