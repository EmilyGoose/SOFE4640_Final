package com.emilygoose.sofe4640final

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.emilygoose.sofe4640final.data.Venue
import com.emilygoose.sofe4640final.util.Ticketmaster

class VenueDetailActivity : AppCompatActivity() {

    private lateinit var venueID: String

    // Ticketmaster API helper
    private val ticketmaster = Ticketmaster()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venue_detail)

        venueID = intent.getStringExtra("ID")!!

        Log.d("VenueDetail", "Getting detail for venue $venueID")

        // Get venue details
        ticketmaster.getVenueDetails(venueID, ::populateVenueFields)
    }

    private fun populateVenueFields(venue: Venue) {
        Log.d("PopulateVenueDetail", venue.toString())
    }
}