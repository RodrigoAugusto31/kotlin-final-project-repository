package com.example.finalprojectrodrigoaugusto

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseBooleanArray
import android.widget.ArrayAdapter
import com.example.finalprojectrodrigoaugusto.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private lateinit var mGoogleSignClient: GoogleSignInClient

    private lateinit var firebaseAuth: FirebaseAuth

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        sharedPreferences = getSharedPreferences("lista de tarefas",Context.MODE_PRIVATE)

        firebaseAuth = FirebaseAuth.getInstance()

        loginVerification()
        setUpGoogleSignIn()
        setListViewAndButtonsFunctions()
        setProfileButtonListener()
        setLogOutButtonListener(firebaseAuth)
    }

    private fun loginVerification() {
        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser
        if (firebaseUser == null) {
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
        }
    }

    private fun setUpGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setLogOutButtonListener(firebaseAuth: FirebaseAuth) {
        binding.mainBtLogOut.setOnClickListener {
            firebaseAuth.signOut()
            mGoogleSignClient.signOut()
            val activity = Intent(this, LoginScreen::class.java)
            startActivity(activity)
        }
    }

    private fun setProfileButtonListener() {
        binding.mainBtProfile?.setOnClickListener {
            val activity = Intent(this, ProfileActivity::class.java)
            startActivity(activity)
        }
    }

    private fun setListViewAndButtonsFunctions() {
        val listView = binding.listView
        val editText = binding.mainEditTxtAddItem

        val itemList = getSharedPreferences()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, itemList)

        listView!!.adapter = adapter
        adapter.notifyDataSetChanged()

        binding.mainBtAdd.setOnClickListener {
            itemList.add(editText.text.toString())
            listView.adapter = adapter
            saveSharedPreferences(itemList)

            adapter.notifyDataSetChanged()
            editText.text.clear()
        }

        binding.mainBtDelete.setOnClickListener {
            val position: SparseBooleanArray = listView.checkedItemPositions
            val count = listView.count
            var item  = count -1
            while (item >= 0) {
                if (position.get(item)) {
                    adapter.remove(itemList[item])
                }
                item --
            }
            saveSharedPreferences(itemList)
            position.clear()
            adapter.notifyDataSetChanged()
        }

        binding.mainBtClear.setOnClickListener {
            itemList.clear()
            saveSharedPreferences(itemList)
            adapter.notifyDataSetChanged()
        }
    }

    private fun saveSharedPreferences(array: ArrayList<String>) {
        val arrayJson = gson.toJson(array)
        val editor = sharedPreferences.edit()
        editor.putString("lista",arrayJson)
        editor.apply()
    }

    private fun getSharedPreferences(): ArrayList<String> {
        val arrayJson = sharedPreferences.getString("lista",null)
        return if (arrayJson.isNullOrEmpty()) {
            arrayListOf()
        } else {
            gson.fromJson(arrayJson, object :TypeToken<ArrayList<String>>(){}.type)
        }
    }
}