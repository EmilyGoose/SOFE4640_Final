package com.emilygoose.sofe4640final

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.emilygoose.sofe4640final.data.Venue
import com.emilygoose.sofe4640final.util.Ticketmaster
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    // Firebase auth
    private lateinit var auth: FirebaseAuth

    // Location services client
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Ticketmaster API client helper
    private val ticketmaster = Ticketmaster()

    // List of venues
    private var venueList = ArrayList<Venue>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase auth
        auth = Firebase.auth

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
                ticketmaster.getNearbyVenues(location ?: Location(null), ::venueListCallback)
            }
    }

    override fun onStart() {
        super.onStart()

        // Kick out unauthed users to login screen
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginRegisterActivity::class.java))
        }
    }

    private fun venueListCallback(newVenues: ArrayList<Venue>) {
        venueList = newVenues
        Log.d("VenueListCallback", "Got ${venueList.size} venues")
    }
}