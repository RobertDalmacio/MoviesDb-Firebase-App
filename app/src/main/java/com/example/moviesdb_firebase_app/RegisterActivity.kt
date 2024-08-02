package com.example.moviesdb_firebase_app

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

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
        setContentView(R.layout.activity_register)

        val editTextEmail = findViewById<TextInputEditText>(R.id.et_emailRegister)
        val editTextPassword = findViewById<TextInputEditText>(R.id.et_passwordRegister)
        val editTextPassword2 = findViewById<TextInputEditText>(R.id.et_passwordRegister2)
        val registerButton = findViewById<Button>(R.id.btn_register)
        val progressBar = findViewById<ProgressBar>(R.id.pb_register)
        val loginLink = findViewById<TextView>(R.id.tv_login)

        loginLink.setOnClickListener{
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        registerButton.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val password2 = editTextPassword2.text.toString()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(baseContext, "Missing Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password) || TextUtils.isEmpty(password2)) {
                Toast.makeText(baseContext, "Missing Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != password2) {
                Toast.makeText(baseContext, "Passwords Do Not Match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext, "Account created.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(applicationContext, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }


        }
    }
}