package com.emilygoose.sofe4640final

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.emilygoose.sofe4640final.data.Event
import com.emilygoose.sofe4640final.util.Ticketmaster
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class EventDetailActivity : AppCompatActivity() {

    // Firebase auth and db
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Ticketmaster API helper
    private val ticketmaster = Ticketmaster()

    // Views
    private lateinit var typeLabel: TextView
    private lateinit var buyButton: Button
    private lateinit var eventImage: ImageView
    private lateinit var eventNameLabel: TextView
    private lateinit var eventAddressLabel: TextView
    private lateinit var eventDateLabel: TextView
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
        typeLabel = findViewById(R.id.type)
        buyButton = findViewById(R.id.button_buy)
        eventImage = findViewById(R.id.image_event_detail)
        eventNameLabel = findViewById(R.id.label_event_name)
        eventAddressLabel = findViewById(R.id.label_event_address)
        eventDateLabel = findViewById(R.id.label_event_date)
        appBar = findViewById(R.id.topAppBar)

        val eventID = intent.getStringExtra("ID")!!

        // Get details for the event
        lifecycleScope.launch {
            ticketmaster.getEventDetails(eventID, ::eventDetailCallback)
        }

        val bundle = bundleOf("ID" to eventID)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add<CommentsFragment>(R.id.fragment_comments, args = bundle)
        }

        appBar.setNavigationOnClickListener {
            finish()
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
        // Run on UI thread so we can touch views
        runOnUiThread {
            // Set text view values
            typeLabel.text = event.classifications[0].segment.name
            eventNameLabel.text = event.name
            eventAddressLabel.text = event._embedded!!.venues[0].name
            eventDateLabel.text = event.dates.start.localDate

            // Make sure images exist
            if (event.images.isNotEmpty()) {
                // Load first event image into ImageView with Picasso
                Picasso.get().load(event.images[0].url).into(eventImage)
            }

            // Set buy button to go to Ticketmaster
            buyButton.setOnClickListener {
                // Open URL in browser
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                startActivity(browserIntent)
            }
        }
    }
}
