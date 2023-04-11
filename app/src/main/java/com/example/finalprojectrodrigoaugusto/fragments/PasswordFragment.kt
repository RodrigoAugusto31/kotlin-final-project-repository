package com.example.finalprojectrodrigoaugusto.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.example.finalprojectrodrigoaugusto.R
import com.example.finalprojectrodrigoaugusto.databinding.FragmentPasswordBinding

class PasswordFragment : Fragment() {

    private var _binding: FragmentPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var textViewDifficultyValue: TextView
    private lateinit var password: EditText
    lateinit var text: Editable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordBinding.inflate(inflater,container,false)

        textViewDifficultyValue = binding.textViewDifficultyValue
        password = binding.passwordEditTxt

        password.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    text = s
                }

                if(text.toString().isEmpty()){
                    textViewDifficultyValue.text = getString(R.string.sem_senha)
                }else if(text.toString().length < 6) {
                    textViewDifficultyValue.text = getString(R.string.senha_muito_fraca)
                }else if(text.toString().length < 8){
                    textViewDifficultyValue.text = getString(R.string.senha_fraca)
                }else if(text.toString().length < 10) {
                    textViewDifficultyValue.text = getString(R.string.senha_media)
                }else if(text.toString().length < 12) {
                    textViewDifficultyValue.text = getString(R.string.senha_forte)
                }else {
                    textViewDifficultyValue.text = getString(R.string.senha_muito_forte)
                }
            }
        })

        return binding.root
    }
}