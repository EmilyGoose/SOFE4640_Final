package com.emilygoose.sofe4640final.util

import android.location.Location
import android.util.Log
import com.emilygoose.sofe4640final.BuildConfig
import com.emilygoose.sofe4640final.data.Venue
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import kotlinx.serialization.json.Json
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class Ticketmaster {
    // OkHttp client for calling request
    private var client: OkHttpClient = OkHttpClient()

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
    fun getNearbyVenues(location: Location, callback: (ArrayList<Venue>) -> Unit) {
        // Get GeoHash for the location and truncate to 9 chars
        // Ticketmaster API wants max 9
        val geoHash =
            GeoFireUtils.getGeoHashForLocation(GeoLocation(location.latitude, location.longitude))
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
                        Log.d("VenueSearch", venueJSON.toString())
                        // Bind JSON to Venue data class and add to list
                        val venue = json.decodeFromString(Venue.serializer(), venueJSON.toString())
                        venueList.add(venue)
                    }
                    callback(venueList)
                }
            }
        })

    }

    fun getVenueDetails(venueID: String, callback: (Venue) -> Unit) {
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
                    val venue = json.decodeFromString(Venue.serializer(), responseJSON.toString())
                    callback(venue)
                }
            }
        })
    }
}