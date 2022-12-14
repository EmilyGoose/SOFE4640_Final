package com.emilygoose.sofe4640final.util

import android.location.Location
import android.util.Log
import com.emilygoose.sofe4640final.BuildConfig
import com.emilygoose.sofe4640final.data.Event
import com.emilygoose.sofe4640final.data.Venue
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.IOException

class Ticketmaster {
    // OkHttp client for calling request
    private var client: OkHttpClient = OkHttpClient.Builder()
        // Add a cache to avoid calling API too often
        .cache(Cache(
            directory = File("http_cache"),
            // 50MB max size
            maxSize = 50L * 1024L * 1024L
        ))
        .dispatcher(
            // Only allow 1 request to run concurrently
            Dispatcher().apply {
                maxRequestsPerHost = 1
            })
        .addInterceptor(RateLimitInterceptor())
        .build()

    // Base URL for Ticketmaster Discovery API
    private val baseUrl = "https://app.ticketmaster.com/discovery/v2/"

    // Api key (big secret, get from apikey.properties)
    private var apiKey: String = BuildConfig.TICKETMASTER_API_KEY

    // Json serializer config
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Get nearby venues
    suspend fun getNearbyVenues(location: Location, callback: (ArrayList<Venue>) -> Unit) {
        withContext(Dispatchers.IO) {
            // Get GeoHash for the location and truncate to 9 chars
            // Ticketmaster API wants max 9
            val geoHash =
                GeoFireUtils.getGeoHashForLocation(
                    GeoLocation(
                        location.latitude,
                        location.longitude
                    )
                )
                    .substring(0, 9)

            Log.d("VenueSearch", "User GeoHash: $geoHash")

            // Build query URL
            val urlBuilder = StringBuilder(baseUrl)
            // Specify API endpoint
            urlBuilder.append("venues")
            // Add API key to authenticate
            urlBuilder.append("?apikey=$apiKey")
            // Append query parameters
            urlBuilder
                .append("&geoPoint=$geoHash")
                .append("&radius=30")
                .append("&unit=km")

            // Build and call HTTP request
            val request = Request.Builder().url(urlBuilder.toString()).build()

            Log.d("VenueSearch", "Performing HTTP request")
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("VenueSearch", "Request failed. Reason " + e.message)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.let { body ->
                        val responseJSON = JSONObject(body.string())
                        // Return empty list if error occurs
                        if (responseJSON.has("errors")) {
                            Log.e("VenueSearch", responseJSON.get("errors").toString())
                            return
                        }

                        // Get list of venues and bind to data classes
                        val venues = responseJSON.getJSONObject("_embedded").getJSONArray("venues")
                        val venueList = ArrayList<Venue>()
                        for (i in 0 until venues.length()) {
                            val venueJSON = venues.get(i)
                            // Bind JSON to Venue data class and add to list
                            val venue =
                                json.decodeFromString(Venue.serializer(), venueJSON.toString())
                            // Check if venue has upcoming events and add to list
                            if (venue.upcomingEvents == null || venue.upcomingEvents._total > 0) {
                                venueList.add(venue)
                            }
                        }
                        callback(venueList)
                    }
                }
            })
        }
    }

    suspend fun getVenueDetails(venueID: String, callback: (Venue) -> Unit) {
        // Run on IO thread
        withContext(Dispatchers.IO) {
            // Build query URL
            val urlBuilder = StringBuilder(baseUrl)
            // Specify API endpoint
            urlBuilder.append("venues")
            // Add venue ID
            urlBuilder.append("/$venueID")
            // Add API key to authenticate
            urlBuilder.append("?apikey=$apiKey")

            // Build and call HTTP request
            val request = Request.Builder().url(urlBuilder.toString()).build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("VenueSearch", "Request failed. Reason " + e.message)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.let { body ->
                        val responseJSON = JSONObject(body.string())
                        // Return empty if nothing occurs
                        if (responseJSON.has("errors")) {
                            Log.e("GetVenueDetail", responseJSON.get("errors").toString())
                            return
                        }

                        // Bind response to data class and call back
                        val venue =
                            json.decodeFromString(Venue.serializer(), responseJSON.toString())
                        callback(venue)
                    }
                }
            })
        }
    }

    suspend fun getEventsByVenueId(
        venueID: String,
        callback: (ArrayList<Event>) -> Unit
    ) {
        // Run on IO thread
        withContext(Dispatchers.IO) {
            // Build query URL
            val urlBuilder = StringBuilder(baseUrl)
            // Specify API endpoint
            urlBuilder.append("events")
            // Add API key to authenticate
            urlBuilder.append("?apikey=$apiKey")
            // Append query parameters
            urlBuilder
                .append("&venueId=$venueID")

            // Build and call HTTP request
            val request = Request.Builder().url(urlBuilder.toString()).build()

            Log.d("EventSearch", "Performing HTTP request")
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("EventSearch", "Request failed. Reason " + e.message)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.let { body ->
                        val responseJSON = JSONObject(body.string())
                        // Return if error occurs
                        if (responseJSON.has("errors")) {
                            Log.e("EventSearch", responseJSON.get("errors").toString())
                            return
                        }

                        // Make sure results actually returned
                        if (!responseJSON.has("_embedded")) {
                            return
                        }

                        // Get list of events and bind to data classes
                        val events = responseJSON.getJSONObject("_embedded").getJSONArray("events")
                        val eventList = ArrayList<Event>()
                        for (i in 0 until events.length()) {
                            val eventJSON = events.get(i)
                            // Bind JSON to Event data class and add to list
                            val venue = json.decodeFromString(
                                Event.serializer(),
                                eventJSON.toString()
                            )
                            eventList.add(venue)
                        }
                        callback(eventList)
                    }
                }
            })
        }
    }

    suspend fun getEventDetails(
        eventID: String,
        callback: (Event) -> Unit
    ) {
        // Run on IO thread
        withContext(Dispatchers.IO) {
            val urlBuilder = StringBuilder(baseUrl)
            // Specify API endpoint
            urlBuilder.append("events")
            // Add venue ID to request
            urlBuilder.append("/$eventID")
            // Add API key to authenticate
            urlBuilder.append("?apikey=$apiKey")

            // Build and call request
            val request = Request.Builder().url(urlBuilder.toString()).build()

            Log.d("EventDetails", "Performing HTTP request for event ID $eventID")
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("EventDetails", "Request failed. Reason " + e.message)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.let { body ->
                        val responseJSON = JSONObject(body.string())
                        // Return if error occurs
                        if (responseJSON.has("errors")) {
                            Log.e("EventDetails", responseJSON.get("errors").toString())
                            return
                        }

                        // Deserialize and return event
                        val event =
                            json.decodeFromString(Event.serializer(), responseJSON.toString())
                        callback(event)
                    }
                }

            })
        }
    }
}