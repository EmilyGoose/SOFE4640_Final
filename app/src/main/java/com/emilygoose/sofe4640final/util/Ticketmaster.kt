package com.emilygoose.sofe4640final.util

import android.location.Location
import android.util.Log
import com.emilygoose.sofe4640final.BuildConfig
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import okhttp3.OkHttpClient
import okhttp3.Request

class Ticketmaster {
    // OkHttp client for calling request
    private var client: OkHttpClient = OkHttpClient()

    // Base URL for Ticketmaster Discovery API
    private val baseUrl = "https://app.ticketmaster.com/discovery/v2/"

    // Api key (big secret, get from apikey.properties)
    private var apiKey: String = BuildConfig.TICKETMASTER_API_KEY

    // Get nearby venues - TODO return something lol
    fun getNearbyVenues(location: Location) {
        // Get GeoHash for the location and truncate to 9 chars
        // Ticketmaster API wants max 9
        val geoHash =
            GeoFireUtils.getGeoHashForLocation(GeoLocation(location.latitude, location.longitude))
                .substring(0, 9)

        Log.d("VenueSearch", "User GeoHash: $geoHash")



    }


}