package com.emilygoose.sofe4640final

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.emilygoose.sofe4640final.data.Event
import com.emilygoose.sofe4640final.util.Ticketmaster
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class EventDetailActivity : AppCompatActivity() {

    // Firebase auth and db
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Ticketmaster API helper
    private val ticketmaster = Ticketmaster()

    private lateinit var appBar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        // Initialize Firebase auth
        auth = Firebase.auth

        // Initialize Firestore db
        db = Firebase.firestore

        // Kick out unauthed users to login screen
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginRegisterActivity::class.java))
        }

        // InitializeViews
        appBar = findViewById(R.id.topAppBar)

        // Get details for the event
        lifecycleScope.launch {
            ticketmaster.getEventDetails("", ::eventDetailCallback)
        }

        appBar.setNavigationOnClickListener {
            val intent = Intent(
                this,
                MainActivity::class.java
            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        }

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

    private fun eventDetailCallback(event: Event) {
        Log.d("EventDetailCallback", event.toString())
    }
}
