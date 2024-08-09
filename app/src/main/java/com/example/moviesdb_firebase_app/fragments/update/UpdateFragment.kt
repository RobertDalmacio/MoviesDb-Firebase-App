package com.example.moviesdb_firebase_app.fragments.update

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.moviesdb_firebase_app.LoginActivity
import com.example.moviesdb_firebase_app.R
import com.example.moviesdb_firebase_app.model.Movie
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class UpdateFragment : Fragment() {

    private val args by navArgs<UpdateFragmentArgs>()
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { view?.findViewById<ImageView>(R.id.ivMoviePoster_update)?.setImageURI(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // navigate back to list fragment
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
        callback.isEnabled = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_update, container, false)
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("Movies").document(args.movieId)

        view.findViewById<EditText>(R.id.etMovieTitle_update).setText(args.currentMovie.movieTitle)
        view.findViewById<EditText>(R.id.etStudioName_update).setText(args.currentMovie.studioName)
        view.findViewById<EditText>(R.id.etCriticsRating_update).setText(args.currentMovie.criticsRating.toString())
        view.findViewById<ImageView>(R.id.ivMoviePoster_update).setImageBitmap(convertBase64ToBitmap(args.currentMovie.moviePoster))

        // update submit button
        view.findViewById<Button>(R.id.btnSubmit_update).setOnClickListener {
            val movieTitle = view.findViewById<EditText>(R.id.etMovieTitle_update).text.toString()
            val studioName = view.findViewById<EditText>(R.id.etStudioName_update).text.toString()
            val moviePoster = convertBitmapToBase64(view.findViewById<ImageView>(R.id.ivMoviePoster_update).drawable.toBitmap())
            // set a default value of 0
            var criticsRating = 0
            // only convert to number id string is not empty
            if (view.findViewById<EditText>(R.id.etCriticsRating_update).text.isNotEmpty()) {
                criticsRating = view.findViewById<EditText>(R.id.etCriticsRating_update).text.toString().toDouble().toInt()
            }

            // checks if 3 required text fields are filled out
            if (inputCheck(movieTitle, studioName, view.findViewById<EditText>(R.id.etCriticsRating_update).text)) {
                // checks that critics rating is between 0 to 100
                if (criticsRating in 0..100) {
                    // create updated movie
                    val updatedMovie = Movie(movieTitle, studioName, criticsRating, moviePoster)

                    // updates existing entry from database
                    docRef.set(updatedMovie)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Document successfully updated")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error updating document", e)
                        }

                    // show success message
                    Toast.makeText(requireContext(), "Updated Successfully!", Toast.LENGTH_SHORT).show()

                    // navigate back to list fragment
                    view.findNavController().navigate(R.id.action_updateFragment_to_listFragment)
                } else {
                    Toast.makeText(requireContext(), "Please ensure rating is between 0 and 100.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            }
        }

        // select image button
        view.findViewById<Button>(R.id.btnSelectPoster_update).setOnClickListener {
            resultLauncher.launch("image/*")
        }

        // show delete_menu
        setHasOptionsMenu(true)

        return view
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
                    val db = FirebaseFirestore.getInstance()
                    val docRef = db.collection("Movies").document(args.movieId)
                    // delete selected movie
                    docRef.delete()
                        .addOnSuccessListener {
                            Log.d("Firestore", "DocumentSnapshot successfully deleted!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error deleting document", e)
                        }

                    // show success message
                    Toast.makeText(requireContext(), "Successfully removed: ${args.currentMovie.movieTitle}", Toast.LENGTH_SHORT).show()

                    // navigate back to list fragment
                    view?.findNavController()?.navigate(R.id.action_updateFragment_to_listFragment)
                }
                builder.setNegativeButton("No") { _, _ ->
                    // do nothing
                }
                builder.setTitle("Delete ${args.currentMovie.movieTitle}?")
                builder.setMessage("Are you sure you want to delete ${args.currentMovie.movieTitle}?")
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
        com.google.firebase.Firebase.auth.signOut()
        // Navigate to login screen or perform any other necessary actions
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