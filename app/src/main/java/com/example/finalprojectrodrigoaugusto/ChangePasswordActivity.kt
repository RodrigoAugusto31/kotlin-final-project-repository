package com.example.finalprojectrodrigoaugusto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.example.finalprojectrodrigoaugusto.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var _binding: ActivityChangePasswordBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setChangeButtonListener()
    }

    private fun setChangeButtonListener() {
        binding.changePasswordBtChangePassword.setOnClickListener {
            changePassword()
        }
    }

    private fun notEmpty(): Boolean = binding.changePasswordEditTxtPassword.text.toString().trim().isNotEmpty() &&
            binding.changePasswordEditTxtConfirmPassword.text.toString().trim().isNotEmpty()

    private fun verifySizePassword(): Boolean{

        password = binding.changePasswordEditTxtPassword
        confirmPassword = binding.changePasswordEditTxtConfirmPassword

        var correct = false
        if(password.text.toString().trim().length >= 6) {
            correct = true
        }
        return correct
    }

    private fun changePassword(){
        if(notEmpty()) {
            if (verifySizePassword()) {
                if (password.text.toString().trim() == confirmPassword.text.toString().trim()) {
                    val user = firebaseAuth.currentUser

                    user!!.updatePassword(password.text.toString().trim())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, R.string.change_password_success, Toast.LENGTH_SHORT).show()
                            }
                        }

                    val activity = Intent(this, ProfileActivity::class.java)
                    startActivity(activity)
                    finish()
                } else {
                    Toast.makeText(this, R.string.create_account_password_do_not_match, Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, R.string.change_password_less_then_6, Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, R.string.create_account_fill_all_fields, Toast.LENGTH_SHORT).show()
        }
    }
}