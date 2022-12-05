package com.emilygoose.sofe4640final.data

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val type: String,
    val id: String,
    val name: String,
    val url: String,
    val dates: Dates,
    val images: List<Image> = ArrayList(),
    val _embedded: Embedded? = null,
    val classifications: List<Classification>
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

    @Serializable
    data class Embedded(
        val venues: List<Venue>
    )

    @Serializable
    data class Classification (
        val segment: ClassificationProperty
    ) {
        @Serializable
        data class ClassificationProperty (
            val name: String
            )
    }
}

