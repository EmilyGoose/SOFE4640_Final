package com.emilygoose.sofe4640final

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emilygoose.sofe4640final.adapter.EventListAdapter
import com.emilygoose.sofe4640final.data.Event
import com.emilygoose.sofe4640final.data.Venue
import com.emilygoose.sofe4640final.util.Ticketmaster
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class VenueDetailActivity : AppCompatActivity() {

    private lateinit var venueID: String

    // Firebase auth and db
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Ticketmaster API helper
    private val ticketmaster = Ticketmaster()

    // List of events
    private val eventList = ArrayList<Event>()

    // Is user following venue?
    private var userFollowing = false

    // Declare views
    private lateinit var venueNameView: TextView
    private lateinit var venueAddressView: TextView
    private lateinit var venueImageView: ImageView
    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var followButton: Button
    private lateinit var appBar: MaterialToolbar
    private lateinit var followersView: TextView

    // Adapter for event list RecyclerView
    private lateinit var eventAdapter: EventListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_venue_detail)

        val intentExtra = intent.getStringExtra("ID")

        // Kick user back to main activity if venueID is somehow null
        if (intentExtra == null) {
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            ) // Need return call here so non-nullability of intentExtra is inferred
            return
        }

        venueID = intentExtra

        // Initialize Firebase auth
        auth = Firebase.auth

        // Initialize Firestore db
        db = Firebase.firestore

        // Kick out unauthed users to login screen
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginRegisterActivity::class.java))
        }

        // Initialize views
        venueNameView = findViewById(R.id.label_venue_name)
        venueAddressView = findViewById(R.id.label_venue_address)
        venueImageView = findViewById(R.id.image_venue_detail)
        eventRecyclerView = findViewById(R.id.recycler_nearby)
        followButton = findViewById(R.id.button_follow)
        appBar = findViewById(R.id.topAppBar)
        followersView = findViewById(R.id.label_followers)

        // Configure RecyclerView for events
        eventAdapter = EventListAdapter(eventList)
        eventRecyclerView.adapter = eventAdapter
        eventRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        Log.d("VenueDetail", "Getting detail for venue $venueID")

        lifecycleScope.launch { // Get venue details
            ticketmaster.getVenueDetails(venueID, ::populateVenueFields)
        } // These only work in separate calls
        lifecycleScope.launch { // Get list of upcoming events
            ticketmaster.getEventsByVenueId(venueID, ::populateEventList)
        }

        // Check if user is following venue and display follow button accordingly
        db.collection("users").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { document ->
                val followList = document.get("following") as List<*>
                if (venueID in followList) {
                    userFollowing = true
                    followButton.setText(R.string.prompt_unfollow)
                } else {
                    userFollowing = false
                    followButton.setText(R.string.prompt_follow)
                }
            }

        // Click listener for follow button
        followButton.setOnClickListener { // Check if user is following venue and update collection accordingly
            val document = db.collection("users").document(auth.currentUser!!.uid)
            userFollowing = if (userFollowing) { // Remove venue from user's following
                document.update("following", FieldValue.arrayRemove(venueID)) // Update button
                followButton.setText(R.string.prompt_follow)
                false
            } else { // Add venue to user's following
                document.update("following", FieldValue.arrayUnion(venueID)) // Update button
                followButton.setText(R.string.prompt_unfollow)
                true
            }
        }

        // Get follower count from Firestore and update
        db.collection("users").whereArrayContains("following", venueID).get().addOnSuccessListener {
            documents -> followersView.text = getString(R.string.count_followers, documents.size())
        }

        appBar.setNavigationOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun populateVenueFields(venue: Venue) {
        Log.d("PopulateVenueDetail", venue.toString())

        // Run on UI thread so we can touch the views
        runOnUiThread { // Populate text fields
            venueNameView.text = venue.name
            venueAddressView.text = "${venue.address?.line1}"

            // Insert venue image
            if (venue.images.isNotEmpty()) {
                Picasso.get().load(venue.images[0].url).into(venueImageView)
            }
        }
    }

    private fun populateEventList(newEvents: ArrayList<Event>) { // Run on UI thread so we can touch the adapter
        runOnUiThread {
            eventList.clear()
            eventList.addAll(newEvents)
            Log.d("EventListCallback", "Got ${eventList.size} venues")
            Log.d("EventListCallback", "Adapter has ${eventAdapter.itemCount} items")
            eventAdapter.notifyItemRangeChanged(0, eventList.size)
        }
    }
}
