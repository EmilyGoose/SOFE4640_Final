package com.emilygoose.sofe4640final.data

import kotlinx.serialization.Serializable

@Serializable
data class Venue(
    val name: String = "Error getting venue name",
    val id: String = "",
    val distance: Float? = null,
    val address: Address? = null,
    val images: List<Image> = ArrayList(),
    val upcomingEvents: UpcomingEvents? = null
) {
    @Serializable
    data class UpcomingEvents(
        val _total: Int
    )
}