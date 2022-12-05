package com.emilygoose.sofe4640final

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emilygoose.sofe4640final.adapter.UpcomingEventAdapter
import com.emilygoose.sofe4640final.adapter.VenueListAdapter
import com.emilygoose.sofe4640final.data.Event
import com.emilygoose.sofe4640final.data.Venue
import com.emilygoose.sofe4640final.util.Ticketmaster
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // Firebase auth and db
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Location services client
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Ticketmaster API client helper
    private val ticketmaster = Ticketmaster()

    // List of venues
    private val upcomingVenueList = ArrayList<Venue>()

    // List of following venues
    private val followingVenueList = ArrayList<Venue>()

    // List of events
    private val eventList = ArrayList<Event>()

    // Declare view variables
    private lateinit var nearbyVenueRecyclerView: RecyclerView
    private lateinit var followingVenueRecyclerView: RecyclerView
    private lateinit var upcomingEventRecyclerView: RecyclerView
    private lateinit var appBar: MaterialToolbar

    // Adapters for RecyclerViews
    private lateinit var nearbyVenueAdapter: VenueListAdapter
    private lateinit var followingVenueAdapter: VenueListAdapter
    private lateinit var upcomingEventAdapter: UpcomingEventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Grab view variables
        nearbyVenueRecyclerView = findViewById(R.id.recycler_nearby)
        followingVenueRecyclerView = findViewById(R.id.recycler_following)
        upcomingEventRecyclerView = findViewById(R.id.recycler_homepage_upcoming)
        appBar = findViewById(R.id.topAppBar)

        // Configure nearby venue RecyclerView
        nearbyVenueAdapter = VenueListAdapter(upcomingVenueList, true)
        nearbyVenueRecyclerView.adapter = nearbyVenueAdapter
        nearbyVenueRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)

        // Configure following venue RecyclerView
        followingVenueAdapter = VenueListAdapter(followingVenueList, false)
        followingVenueRecyclerView.adapter = followingVenueAdapter
        followingVenueRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)

        // Configure upcoming event RecyclerView
        upcomingEventAdapter = UpcomingEventAdapter(eventList)
        upcomingEventRecyclerView.adapter = upcomingEventAdapter
        upcomingEventRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)

        // Initialize Firebase auth
        auth = Firebase.auth
        db = Firebase.firestore

        // Kick out unauthed users to login screen
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginRegisterActivity::class.java))
        }

        // Initiate the fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check that we have location permissions, if not then ask the user
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Location", "No location perms, ask the user")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                0
            )
        }

        // Get the user's last known location and populate the nearby venues list
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                Log.d("Location", "Got user's location, calling Ticketmaster API")
                // Get nearby venues - In rare cases location is null so send user to null island
                // Pass it the callback function to populate venueList
                lifecycleScope.launch {
                    ticketmaster.getNearbyVenues(location ?: Location(null), ::venueListCallback)
                }
            }

        eventList.clear()

        // Get user's following venues and get events for them
        db.collection("users").document(auth.currentUser!!.uid)
            .get().addOnSuccessListener { document ->
                val followList = document.get("following") as List<*>
                for (venueID in followList) {
                    Log.d("FollowingVenues", "User follows venue $venueID")
                    // Get details for following venue
                    lifecycleScope.launch {
                        ticketmaster.getVenueDetails(venueID as String, ::followingVenueCallback)
                    }

                    // Get list of upcoming events at following venues
                    lifecycleScope.launch {
                        ticketmaster.getEventsByVenueId(
                            venueID as String,
                            ::upcomingEventCallback
                        )
                    }
                }
            }

        appBar.navigationIcon=null

        appBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.logout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginRegisterActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun venueListCallback(newVenues: ArrayList<Venue>) {
        // Run on UI thread so we can touch the adapter
        runOnUiThread {
            upcomingVenueList.clear()
            // Add new venues and update the adapter
            upcomingVenueList.addAll(newVenues.reversed())
            nearbyVenueAdapter.notifyItemRangeChanged(0, upcomingVenueList.size)
            // Scroll to "end" (this is actually the start lol)
            nearbyVenueRecyclerView.scrollToPosition(upcomingVenueList.size - 1)
        }
    }

    private fun followingVenueCallback(venue: Venue) {
        runOnUiThread {
            followingVenueList.add(venue)
            // Update the adapter
            followingVenueAdapter.notifyItemInserted(followingVenueList.size - 1)
            // Scroll fix
            followingVenueRecyclerView.scrollToPosition(followingVenueList.size - 1)
        }
    }

    private fun upcomingEventCallback(newEvents: ArrayList<Event>) {
        // Run on UI thread so we can touch the adapter
        runOnUiThread {
            eventList.addAll(newEvents)
            // Sort eventList by dates
            val sortedEvents = eventList.sortedBy { it.dates.start.localDate }
            eventList.clear()
            eventList.addAll(sortedEvents.reversed())
            upcomingEventAdapter.notifyItemRangeChanged(0, eventList.size)
            // Scroll to "end" (actually start)
            upcomingEventRecyclerView.scrollToPosition(eventList.size - 1)
        }
    }
}
