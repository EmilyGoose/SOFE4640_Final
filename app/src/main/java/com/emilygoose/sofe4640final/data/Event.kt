package com.emilygoose.sofe4640final.data

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val type: String,
    val name: String,
    val description: String = "",
    val images: List<Image> = ArrayList()
)