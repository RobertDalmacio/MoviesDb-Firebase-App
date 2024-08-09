package com.example.moviesdb_firebase_app.fragments.list

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesdb_firebase_app.LoginActivity
import com.example.moviesdb_firebase_app.R
import com.example.moviesdb_firebase_app.model.Movie
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ListFragment : Fragment() {

    private lateinit var adapter: MovieListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Do nothing on back pressed
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {}
        callback.isEnabled = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val db = FirebaseFirestore.getInstance()
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        // display movies, sorted by criticsRating
        val query = db.collection("Movies").orderBy("criticsRating", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<Movie>()
            .setQuery(query, Movie::class.java)
            .build()
        adapter = MovieListAdapter(options)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // add new movie button
        view.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_addFragment)
        }

        // show delete_menu
        setHasOptionsMenu(true)

        return view
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> {
                val builder = AlertDialog.Builder(requireContext())
                builder.setPositiveButton("Yes") { _, _ ->
                    // delete all data in movie db table
                    deleteAllMovies()

                    // show success message
                    Toast.makeText(requireContext(), "Movie database cleared.", Toast.LENGTH_SHORT).show()
                }
                builder.setNegativeButton("No") { _, _ ->
                    // do nothing
                }
                builder.setTitle("Clear Movie Database?")
                builder.setMessage("Are you sure you want to delete all movies from the database?")
                builder.create().show()
                true
            }
            R.id.menu_user -> {
                showUserMenu(item)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showUserMenu(item: MenuItem) {
        val menuItemView = requireActivity().findViewById<View>(item.itemId)

        if (menuItemView != null) {
            val popupMenu = PopupMenu(requireContext(), menuItemView, Gravity.END)
            popupMenu.menuInflater.inflate(R.menu.user_menu, popupMenu.menu)

            // set the user's email
            val user = Firebase.auth.currentUser
            user?.let {
                val emailMenuItem = popupMenu.menu.findItem(R.id.user_email)
                emailMenuItem.title = it.email
            }

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_logout -> {
                        logout()
                        true
                    }
                    else -> false
                }
            }

            // Show icons in the popup menu (if you want to display the logout icon)
            try {
                val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                fieldMPopup.isAccessible = true
                val mPopup = fieldMPopup.get(popupMenu)
                mPopup?.javaClass?.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    ?.invoke(mPopup, true)
            } catch (e: Exception) {
                Log.e("PopupMenu", "Error showing icons", e)
            }

            popupMenu.show()
        } else {
            // Fallback if we can't find the view
            Toast.makeText(requireContext(), "Unable to show menu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        Firebase.auth.signOut()
        // Navigate to login screen or perform any other necessary actions
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun deleteAllMovies() {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("Movies")

        collectionRef.get()
            .addOnSuccessListener { querySnapshot ->
                val batch = db.batch()
                for (document in querySnapshot.documents) {
                    batch.delete(document.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        Toast.makeText(context, "All documents successfully deleted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error deleting documents", e)
                        Toast.makeText(context, "Error deleting documents: $e", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error getting documents", e)
                Toast.makeText(context, "Error getting documents: $e", Toast.LENGTH_SHORT).show()
            }
    }
}