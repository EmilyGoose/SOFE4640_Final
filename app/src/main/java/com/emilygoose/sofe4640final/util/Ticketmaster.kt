package com.emilygoose.sofe4640final.util

import com.emilygoose.sofe4640final.BuildConfig
import okhttp3.OkHttpClient

class Ticketmaster {
    // OkHttp client for calling request
    private var client: OkHttpClient = OkHttpClient()

    // Base URL for Ticketmaster Discovery API
    private val baseUrl = "https://app.ticketmaster.com/discovery/v2/"

    // Api key (big secret, get from apikey.properties)
    private var apiKey: String = BuildConfig.TICKETMASTER_API_KEY

}