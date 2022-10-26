package com.emilygoose.sofe4640final.data

import kotlinx.serialization.Serializable

@Serializable
data class Image(val url: String, val fallback: Boolean)