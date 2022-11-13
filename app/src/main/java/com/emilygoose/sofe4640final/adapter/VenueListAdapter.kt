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
        val venue = dataSet[position]

        // Set title and distance
        holder.venueTitle.text = venue.name
        // todo grab string resource properly
        holder.venueDistance.text =
            holder.view.context.getString(R.string.label_distance, venue.distance)

        Log.d("BindImage", "Adding image to card")
        // Make sure images exist
        if (venue.images.isNotEmpty()) {
            // Load first venue image into ImageView with Picasso
            Picasso.get().load(venue.images[0].url).resize(250, 250).centerCrop()
                .into(holder.venueImageView)
        }

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