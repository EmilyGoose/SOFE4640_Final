package com.emilygoose.sofe4640final.data

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val line1: String? = "",
    val line2: String? = "",
    val line3: String? = ""
)