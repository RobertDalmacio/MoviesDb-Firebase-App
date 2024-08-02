package com.example.moviesdb_firebase_app

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var passwordVisible = false

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val editTextEmail = findViewById<TextInputEditText>(R.id.et_emailLogin)
        val editTextPassword = findViewById<TextInputEditText>(R.id.et_passwordLogin)
        val loginButton = findViewById<Button>(R.id.btn_login)
        val progressBar = findViewById<ProgressBar>(R.id.pb_login)
        val registerLink = findViewById<TextView>(R.id.tv_register)
        val visibilityButton = findViewById<ImageButton>(R.id.ib_togglePasswordVisibility)

        visibilityButton.setOnClickListener {
            passwordVisible = !passwordVisible
            if (passwordVisible) {
                editTextPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                visibilityButton.setImageResource(R.drawable.ic_visibility_off)
            } else {
                editTextPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                visibilityButton.setImageResource(R.drawable.ic_visibility)
            }
            editTextPassword.setSelection(editTextPassword.text?.length ?: 0)
        }

        registerLink.setOnClickListener{
            val intent = Intent(applicationContext, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        loginButton.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(baseContext, "Missing Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(baseContext, "Missing Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext, "Login Successful.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }

        }


    }
}