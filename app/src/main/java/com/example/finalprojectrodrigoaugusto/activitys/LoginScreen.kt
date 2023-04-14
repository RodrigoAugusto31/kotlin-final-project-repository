package com.example.finalprojectrodrigoaugusto.activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.example.finalprojectrodrigoaugusto.MainActivity
import com.example.finalprojectrodrigoaugusto.R
import com.example.finalprojectrodrigoaugusto.databinding.ActivityLoginScreenBinding
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class LoginScreen : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText

    private lateinit var mGoogleClient: GoogleSignInClient
    private val reqCode = 123
    private lateinit var firebaseAuth: FirebaseAuth

    private var _binding: ActivityLoginScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        email = binding.editTxtEmail
        password = binding.editTxtPassword

        firebaseAuth = FirebaseAuth.getInstance()

        setButtonsListeners()
        setUpGoogleSignIn()

    }

    private fun setUpGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setButtonsListeners() {
        binding.btRegister.setOnClickListener {
            val activity = Intent(this, CreateAccount::class.java)
            startActivity(activity)
        }

        binding.btLoginGoogle.setOnClickListener {
            val signIntent: Intent = mGoogleClient.signInIntent
            startActivityForResult(signIntent, reqCode)
        }

        binding.btLogin.setOnClickListener {
            sign()
        }

        binding.textViewForgotPassword.setOnClickListener {
            val activity = Intent(this, ForgotPasswordActivity::class.java);
            startActivity(activity)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == reqCode) {
            val result = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(result)
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            Toast.makeText(
                this,
                getString(R.string.create_account_login_success),
                Toast.LENGTH_LONG
            ).show()
            if (account != null) {
                updateUser(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, getString(R.string.create_account_login_fail), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun updateUser(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun notEmpty(): Boolean =
        email.text.toString().trim().isNotEmpty() && password.text.toString().trim().isNotEmpty()

    private fun sign() {
        if (notEmpty()) {
            val userEmail = email.text.toString().trim()
            val userPassword = password.text.toString().trim()

            firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser
                        if (firebaseUser != null && firebaseUser.isEmailVerified) {
                            startActivity(Intent(this, MainActivity::class.java))
                            Toast.makeText(
                                this,
                                getString(R.string.create_account_login_success),
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else if (firebaseUser != null && !firebaseUser.isEmailVerified) {
                            firebaseAuth.signOut()
                            Toast.makeText(
                                this,
                                getString(R.string.check_your_email),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.create_account_login_fail),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.create_account_login_fail),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(
                this,
                getString(R.string.create_account_fill_all_fields),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}