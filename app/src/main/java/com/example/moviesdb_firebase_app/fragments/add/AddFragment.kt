package com.example.moviesdb_firebase_app.fragments.add

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.moviesdb_firebase_app.R
import com.example.moviesdb_firebase_app.model.Movie
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream
import android.util.Base64
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.activity.addCallback
import com.example.moviesdb_firebase_app.LoginActivity
import com.example.moviesdb_firebase_app.RegisterActivity
import com.google.firebase.auth.auth

class AddFragment : Fragment() {

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { view?.findViewById<ImageView>(R.id.ivMoviePoster)?.setImageURI(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // navigate back to list fragment
            findNavController().navigate((R.id.action_addFragment_to_listFragment))
        }
        callback.isEnabled = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add, container, false)

        val db = Firebase.firestore

        // function that inserts data to database
        fun insertDataToDatabase() {
            val movieTitle = view.findViewById<EditText>(R.id.etMovieTitle).text.toString()
            val studioName = view.findViewById<EditText>(R.id.etStudioName).text.toString()
            val moviePoster = convertBitmapToBase64(view.findViewById<ImageView>(R.id.ivMoviePoster).drawable.toBitmap())
            // set a default value of 0
            var criticsRating = 0
            // only convert to number id string is not empty
            if (view.findViewById<EditText>(R.id.etCriticsRating).text.isNotEmpty()) {
                criticsRating = view.findViewById<EditText>(R.id.etCriticsRating).text.toString().toDouble().toInt()
            }

            // checks if 3 required text fields are filled out
            if (inputCheck(movieTitle, studioName, view.findViewById<EditText>(R.id.etCriticsRating).text)) {
                // checks that critics rating is between 0 to 100
                if (criticsRating in 0..100) {
                    // create new movie
                    val movie = Movie(movieTitle, studioName, criticsRating, moviePoster)

                    // inserts to database
                    db.collection("Movies")
                        .add(movie)
                        .addOnSuccessListener { documentReference ->
                            Log.d("AddFragment", "DocumentSnapshot added with ID: ${documentReference.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.w("AddFragment", "Error adding document", e)
                        }

                    // show success message
                    Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_LONG).show()

                    // navigate back to list fragment
                    findNavController().navigate((R.id.action_addFragment_to_listFragment))
                } else {
                    Toast.makeText(requireContext(), "Please ensure rating is between 0 and 100.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_LONG).show()
            }
        }

        // add submit button
        view.findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            insertDataToDatabase()
        }

        // select image button
        view.findViewById<Button>(R.id.btnSelectPoster).setOnClickListener {
            resultLauncher.launch("image/*")
        }

        // show delete_menu
        setHasOptionsMenu(true)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
        // hide delete menu item
        val deleteMenuItem = menu.findItem(R.id.menu_delete)
        deleteMenuItem.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
            val user = com.google.firebase.Firebase.auth.currentUser
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
        com.google.firebase.Firebase.auth.signOut()
        // Navigate to LoginActivity
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    // function to check that all required input fields are not empty
    private fun inputCheck(movieName: String, studioName: String, criticsRating: Editable): Boolean{
        return !TextUtils.isEmpty(movieName) && !TextUtils.isEmpty(studioName) && criticsRating.isNotEmpty()
    }

    // function to convert bitmap image to base64 before inserting to db
    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        // Convert the Bitmap to a byte array
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        // Encode the byte array to Base64
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}