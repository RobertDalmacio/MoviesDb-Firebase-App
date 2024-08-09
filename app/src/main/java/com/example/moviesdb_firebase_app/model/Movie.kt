package com.example.moviesdb_firebase_app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize

data class Movie(
    val movieTitle: String = "",
    val studioName: String = "",
    val criticsRating: Int = 0,
    val moviePoster: String = "" // Assuming moviePoster is a URL or Base64 string
): Parcelable
