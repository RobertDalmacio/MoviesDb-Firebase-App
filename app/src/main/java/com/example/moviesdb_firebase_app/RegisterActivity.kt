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

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var passwordVisible = false
    private var password2Visible = false

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
        val visibilityButton1 = findViewById<ImageButton>(R.id.ib_togglePasswordVisibilityRegister)
        val visibilityButton2 = findViewById<ImageButton>(R.id.ib_togglePassword2VisibilityRegister)

        // login text link
        loginLink.setOnClickListener{
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // password visibility button
        visibilityButton1.setOnClickListener {
            passwordVisible = !passwordVisible
            // toggle password visibility
            if (passwordVisible) {
                editTextPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                visibilityButton1.setImageResource(R.drawable.ic_visibility_off)
            } else {
                editTextPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                visibilityButton1.setImageResource(R.drawable.ic_visibility)
            }
            // bring cursor to the end
            editTextPassword.setSelection(editTextPassword.text?.length ?: 0)
        }

        // confirm password visibility button
        visibilityButton2.setOnClickListener {
            password2Visible = !password2Visible
            // toggle password visibility
            if (password2Visible) {
                editTextPassword2.transformationMethod = HideReturnsTransformationMethod.getInstance()
                visibilityButton2.setImageResource(R.drawable.ic_visibility_off)
            } else {
                editTextPassword2.transformationMethod = PasswordTransformationMethod.getInstance()
                visibilityButton2.setImageResource(R.drawable.ic_visibility)
            }
            // bring cursor to the end
            editTextPassword2.setSelection(editTextPassword.text?.length ?: 0)
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
                        // navigate to MainActivity
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}