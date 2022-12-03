package com.emilygoose.sofe4640final

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginRegisterActivity : AppCompatActivity() {

    // Declare vars for views we'll need
    private lateinit var formTitle: TextView
    private lateinit var nameField: EditText
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var modeButton: Button

    // Boolean tracking whether form is in register or login mode
    private var registerMode = false

    // Firebase auth and db
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

        // Initialize Firebase auth
        auth = Firebase.auth
        // Make sure existing session is kill
        auth.signOut()

        // Initialize Firestore db
        db = Firebase.firestore

        // Grab all views by ID
        formTitle = findViewById(R.id.loginTitle)
        nameField = findViewById(R.id.fieldName)
        emailField = findViewById(R.id.fieldEmail)
        passwordField = findViewById(R.id.fieldPassword)
        loginButton = findViewById(R.id.loginButton)
        modeButton = findViewById(R.id.buttonModeSwitch)

        // Set form for current mode
        updateForm()

        // Set listener for login/register switch button
        modeButton.setOnClickListener {
            registerMode = !registerMode
            updateForm()
        }

        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (registerMode) {
                // Create new user
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Auth", "createUserWithEmail:success")
                            // Write user name to db
                            val name = nameField.text.toString()
                            val userData = hashMapOf(
                                "name" to name,
                                "following" to listOf<String>()
                            )
                            db.collection("users").document(auth.currentUser!!.uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    Log.d(
                                        "New User",
                                        "New user created in db"
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.w("New User", "Error adding document", e)
                                }
                            // Send user to MainActivity after success
                            startActivity(Intent(this, MainActivity::class.java))
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Auth", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Auth", "signInWithEmail:success")
                            // Send user to MainActivity after success
                            startActivity(Intent(this, MainActivity::class.java))
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Auth", "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

    override fun onBackPressed() {
        //Intentionally left empty to prevent users from going back after logging out
    }

    private fun updateForm() {
        if (registerMode) {
            formTitle.setText(R.string.title_register)
            modeButton.setText(R.string.prompt_switch_login)
            nameField.isVisible = true
        } else {
            formTitle.setText(R.string.title_login)
            // Hide name field when logging in we already have it in user data
            nameField.isVisible = false
            modeButton.setText(R.string.prompt_switch_register)
        }
    }
}
