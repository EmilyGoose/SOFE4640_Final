package com.emilygoose.sofe4640final

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.emilygoose.sofe4640final.data.Event
import com.emilygoose.sofe4640final.data.Venue
import com.emilygoose.sofe4640final.util.Ticketmaster
import com.squareup.picasso.Picasso

class VenueDetailActivity : AppCompatActivity() {

    private lateinit var venueID: String

    // Declare views
    private lateinit var venueNameView: TextView
    private lateinit var venueAddressView: TextView
    private lateinit var venueImageView: ImageView

    // Ticketmaster API helper
    private val ticketmaster = Ticketmaster()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venue_detail)

        val intentExtra = intent.getStringExtra("ID")

        // Kick user back to main activity if venueID is somehow null
        if (intentExtra == null) {
            startActivity(Intent(this, MainActivity::class.java))
            // Need return call here so non-nullability of intentExtra is inferred
            return
        }

        venueID = intentExtra

        // Initialize views
        venueNameView = findViewById(R.id.label_venue_name)
        venueAddressView = findViewById(R.id.label_venue_address)
        venueImageView = findViewById(R.id.image_venue_detail)

        Log.d("VenueDetail", "Getting detail for venue $venueID")

        // Get venue details
        ticketmaster.getVenueDetails(venueID, ::populateVenueFields)

        // Get list of upcoming events
        ticketmaster.getEventsByVenueId(venueID, ::populateEventList)

    }

    private fun populateVenueFields(venue: Venue) {
        Log.d("PopulateVenueDetail", venue.toString())

        // Run on UI thread so we can touch the views
        runOnUiThread {
            // Populate text fields
            venueNameView.text = venue.name
            venueAddressView.text = "${venue.address?.line1}"

            // Insert venue image
            if (venue.images.isNotEmpty()) {
                Picasso.get().load(venue.images[0].url).into(venueImageView)
            }
        }
    }

    private fun populateEventList(eventList: List<Event>) {
        for (event in eventList) {
            Log.d("PopulateEventList", event.name)
        }
    }
}