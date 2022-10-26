package com.emilygoose.sofe4640final.data

import kotlinx.serialization.Serializable

@Serializable
data class Venue(val name: String, val id: String, val distance: Float)