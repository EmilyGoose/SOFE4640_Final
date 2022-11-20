package com.emilygoose.sofe4640final.data

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val type: String,
    val name: String,
    val dates: Dates,
    val description: String = "",
    val images: List<Image> = ArrayList()
) {
    // Ugly nested serializable because of the weird format from the API
    @Serializable
    data class Dates(
        val start: StartDate
    ) {
        @Serializable
        data class StartDate(
            val localDate: String = "",
            val dateTime: String = ""
        )
    }
}