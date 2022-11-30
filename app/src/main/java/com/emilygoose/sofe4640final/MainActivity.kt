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
    private val venueList = ArrayList<Venue>()

    // List of events
    private val eventList = ArrayList<Event>()

    // Declare view variables
    private lateinit var nearbyVenueRecyclerView: RecyclerView
    private lateinit var upcomingEventRecyclerView: RecyclerView
    private lateinit var appBar: MaterialToolbar

    // Adapters for RecyclerViews
    private lateinit var nearbyVenueAdapter: VenueListAdapter
    private lateinit var upcomingEventAdapter: UpcomingEventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Grab view variables
        nearbyVenueRecyclerView = findViewById(R.id.recycler_follow)
        upcomingEventRecyclerView = findViewById(R.id.recycler_homepage_upcoming)
        appBar = findViewById(R.id.topAppBar)

        // Configure nearby venue RecyclerView
        nearbyVenueAdapter = VenueListAdapter(venueList)
        nearbyVenueRecyclerView.adapter = nearbyVenueAdapter
        nearbyVenueRecyclerView.layoutManager =
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
                    lifecycleScope.launch {
                        ticketmaster.getEventsByVenueId(
                            venueID as String,
                            ::upcomingEventCallback
                        )
                    }
                }
            }

        appBar.setNavigationOnClickListener {
            Log.d("Nav click", "Doesn't do anything on this screen :)")
        }
    }

    private fun venueListCallback(newVenues: ArrayList<Venue>) {
        // Run on UI thread so we can touch the adapter
        runOnUiThread {
            venueList.clear()
            // Add new venues and update the adapter
            venueList.addAll(newVenues.reversed())
            nearbyVenueAdapter.notifyItemRangeChanged(0, venueList.size)
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
        }
    }
}
