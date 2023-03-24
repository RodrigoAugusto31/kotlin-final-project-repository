package com.example.finalprojectrodrigoaugusto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.finalprojectrodrigoaugusto.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private var _binding: ActivityForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        firebaseAuth = FirebaseAuth.getInstance()
        setButtonsListeners()
    }

    private fun setButtonsListeners() {
        binding.btnRecuperar.setOnClickListener {
            val emailAddress = binding.forgotPasswordEditTxtEmail.text.toString()

            firebaseAuth.sendPasswordResetEmail(emailAddress).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.email_sent), Toast.LENGTH_SHORT).show()
                    returnToLogin()
                }
            }
        }

        binding.forgotPasswordBtBackToLogin.setOnClickListener {
            returnToLogin()
        }
    }

    private fun returnToLogin() {
        val activity = Intent(this, LoginScreen::class.java)
        startActivity(activity)
        finish()
    }
}