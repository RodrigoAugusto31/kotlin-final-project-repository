package com.example.finalprojectrodrigoaugusto.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.finalprojectrodrigoaugusto.R
import com.example.finalprojectrodrigoaugusto.databinding.FragmentPasswordBinding
import java.util.regex.Pattern

class PasswordFragment : Fragment() {

    private var _binding: FragmentPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var textViewDifficultyValue: TextView
    private lateinit var emailTextViewDifficultyValue: TextView
    private lateinit var email: EditText
    private lateinit var password: EditText
    lateinit var passwordText: Editable
    lateinit var emailText: Editable

    private val EMAIL_ADDRESS_PATTERN: Pattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordBinding.inflate(inflater,container,false)

        textViewDifficultyValue = binding.textViewDifficultyValue
        password = binding.passwordEditTxt
        emailTextViewDifficultyValue = binding.emailTextViewDifficultyValue
        email = binding.emailEditTxt

        setPasswordFieldListener()
        setEmailFieldListener()


        return binding.root
    }

    private fun checkEmail(email: String): Boolean {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
    }

    private fun setEmailFieldListener() {
        email.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(_email: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(_email: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(_email: Editable?) {
                if (_email != null) {
                    emailText = _email
                }

                if(emailText.toString().isEmpty()){
                    emailTextViewDifficultyValue.text = getString(R.string.sem_email)
                } else if(!checkEmail(emailText.toString())) {
                    emailTextViewDifficultyValue.text = getString(R.string.email_invalido)
                } else {
                    emailTextViewDifficultyValue.text = getString(R.string.email_valido)
                }
            }
        })
    }

    private fun setPasswordFieldListener() {
        password.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(_password: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(_password: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(_password: Editable?) {
                if (_password != null) {
                    passwordText = _password
                }

                if(passwordText.toString().isEmpty()){
                    textViewDifficultyValue.text = getString(R.string.sem_senha)
                }else if(passwordText.toString().length < 6) {
                    textViewDifficultyValue.text = getString(R.string.senha_muito_fraca)
                }else if(passwordText.toString().length < 8){
                    textViewDifficultyValue.text = getString(R.string.senha_fraca)
                }else if(passwordText.toString().length < 10) {
                    textViewDifficultyValue.text = getString(R.string.senha_media)
                }else if(passwordText.toString().length < 12) {
                    textViewDifficultyValue.text = getString(R.string.senha_forte)
                }else {
                    textViewDifficultyValue.text = getString(R.string.senha_muito_forte)
                }
            }
        })
    }
}