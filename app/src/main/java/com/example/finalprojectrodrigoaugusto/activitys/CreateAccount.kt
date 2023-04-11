package com.example.finalprojectrodrigoaugusto.activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.example.finalprojectrodrigoaugusto.MainActivity
import com.example.finalprojectrodrigoaugusto.R
import com.example.finalprojectrodrigoaugusto.databinding.ActivityCreateAccountBinding
import com.example.finalprojectrodrigoaugusto.fragments.PasswordFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class CreateAccount : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: PasswordFragment
    private lateinit var confirmPassword: EditText
    private lateinit var createAccountInputArrayList: Array<Any>

    private lateinit var mGoogleClient: GoogleSignInClient
    private val reqCode = 123
    private lateinit var firebaseAuth: FirebaseAuth

    private var _binding: ActivityCreateAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        email = binding.createAccountEditTxtEmail
        password = supportFragmentManager.findFragmentById(R.id.password_fragment) as PasswordFragment
        confirmPassword = binding.createAccountEditTxtConfirmPassword
        createAccountInputArrayList = arrayOf(email, password, confirmPassword)

        setUpFireBaseAuth()
        setUpGoogleSignIn()
        setRegisterButtonListener()
        setLoginButtonListener()
        setLoginWithGoogleButtonListener()

    }

    private fun setUpFireBaseAuth() {
        FirebaseApp.initializeApp(this)
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun setUpGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setLoginButtonListener() {
        binding.createAccountBtLogin.setOnClickListener {
            val activity = Intent(this, LoginScreen::class.java)
            startActivity(activity)
        }
    }

    private fun setLoginWithGoogleButtonListener() {
        binding.createAccountBtLoginGoogle.setOnClickListener {
            val signIntent: Intent = mGoogleClient.signInIntent
            startActivityForResult(signIntent, reqCode)
        }
    }

    private fun setRegisterButtonListener() {
        binding.createAccountBtRegister.setOnClickListener {
            signIn()
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

    private fun notEmpty(): Boolean = password.text.toString().trim().isNotEmpty() &&
            confirmPassword.text.toString().trim().isNotEmpty() && email.text.toString().trim()
        .isNotEmpty()

    private fun verifyIdenticalPassword(): Boolean {
        var identical = false
        if (password.text.toString().trim() == confirmPassword.text.toString().trim()) {
            identical = true
        } else {
            Toast.makeText(
                this,
                getString(R.string.create_account_password_do_not_match),
                Toast.LENGTH_LONG
            ).show()

        }
        return identical
    }

    private fun verifySizePassword(): Boolean {
        var correct = false
        if (password.text.toString().trim().length >= 6) {
            correct = true
        }
        return correct
    }

    private fun signIn() {
        if (notEmpty()) {
            if (verifyIdenticalPassword()) {
                if (verifySizePassword()) {
                    val userEmail = binding.createAccountEditTxtEmail.text.toString().trim()
                    val userPassword = password.text.toString().trim()

                    firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                sendEmailVerification()

                                Toast.makeText(
                                    this,
                                    getString(R.string.create_account_user_created),
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                                finish()
                            } else {
                                val exception = it.exception
                                if (exception is FirebaseAuthException && exception.errorCode == "ERROR_EMAIL_ALREADY_IN_USE") {
                                    Toast.makeText(
                                        this,
                                        getString(R.string.create_account_email_already_in_use),
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                } else if (exception is FirebaseAuthException && exception.errorCode == "ERROR_WEAK_PASSWORD") {
                                    Toast.makeText(
                                        this,
                                        getString(R.string.create_account_weak_password),
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this,
                                        getString(R.string.create_account_password_do_not_match),
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                }
                            }
                        }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.create_account_equal_but_not_right_size_passwords),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.create_account_password_do_not_match),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                this,
                getString(R.string.create_account_fill_all_fields),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun sendEmailVerification() {
        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser

        firebaseUser?.let { it ->
            it.sendEmailVerification().addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(
                        this,
                        getString(R.string.create_account_email_sent),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}