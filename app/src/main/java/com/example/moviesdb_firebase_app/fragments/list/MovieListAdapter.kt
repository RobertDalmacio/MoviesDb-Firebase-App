package com.example.moviesdb_firebase_app.fragments.list

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesdb_firebase_app.R
import com.example.moviesdb_firebase_app.model.Movie
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import android.graphics.BitmapFactory
import android.util.Base64

class MovieListAdapter(options: FirestoreRecyclerOptions<Movie>): FirestoreRecyclerAdapter<Movie, MovieListAdapter.ViewHolder>(options) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val movieTitle: TextView = itemView.findViewById(R.id.tvMovieTitle)
        val studioName: TextView = itemView.findViewById(R.id.tvStudioName)
        val criticsRating: TextView = itemView.findViewById(R.id.tvCriticsRating)
        val moviePoster: ImageView = itemView.findViewById(R.id.ivMoviePosterList)
        val constLayout: ConstraintLayout = itemView.findViewById(R.id.rowLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.custom_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Movie) {
        holder.movieTitle.text = model.movieTitle
        holder.studioName.text = model.studioName
        holder.criticsRating.text = model.criticsRating.toString()
        holder.moviePoster.setImageBitmap(convertBase64ToBitmap(model.moviePoster))
        val id = snapshots.getSnapshot(position).id

        // custom row item
        holder.constLayout.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToUpdateFragment(model, id)

            // navigate to update fragment
            holder.itemView.findNavController().navigate(action)
        }
    }

    // function to convert base64 image from db to bitmap
    private fun convertBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            // Decode the Base64 string to a byte array
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)

            // Convert the byte array back to a Bitmap
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}