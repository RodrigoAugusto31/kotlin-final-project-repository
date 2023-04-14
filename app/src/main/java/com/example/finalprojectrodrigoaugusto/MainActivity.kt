package com.example.finalprojectrodrigoaugusto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.finalprojectrodrigoaugusto.activitys.LoginScreen
import com.example.finalprojectrodrigoaugusto.activitys.ProfileActivity
import com.example.finalprojectrodrigoaugusto.activitys.TaskActivity
import com.example.finalprojectrodrigoaugusto.databinding.ActivityMainBinding
import com.example.finalprojectrodrigoaugusto.fragments.WeatherFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var mGoogleSignClient: GoogleSignInClient

    private lateinit var firebaseAuth: FirebaseAuth

    private val uid = FirebaseAuth.getInstance().currentUser?.uid
    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/tasks")
    private val listItems = ArrayList<String>()

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, WeatherFragment()).commit()

        supportActionBar?.hide()

        firebaseAuth = FirebaseAuth.getInstance()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
        val listView = binding.listView
        listView?.adapter = adapter

        loginVerification()
        setUpGoogleSignIn()
        setButtonsListeners()
        setLogOutButtonListener(firebaseAuth)

        ref.addValueEventListener(object: ValueEventListener {
            val ctx = this@MainActivity

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listItems.clear()

                for(child in dataSnapshot.children){
                    listItems.add(child.child("titulo").value.toString())
                }

                adapter.notifyDataSetChanged()

                listView?.setOnItemLongClickListener { _, _, position, _ ->
                    val itemId =  dataSnapshot.children.toList()[position].key

                    if(itemId != null){
                        AlertDialog.Builder(ctx)
                            .setTitle(getString(R.string.deletar_tarefa))
                            .setMessage(getString(R.string.confirma_deletar_tarefa))
                            .setPositiveButton(getString(R.string.sim)){ _, _ ->
                                ref.child(itemId).removeValue()
                                Toast.makeText(ctx, getString(R.string.tarefa_deletada), Toast.LENGTH_SHORT).show()
                            }
                            .setNegativeButton(getString(R.string.nao)){ dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }

                    true
                }
                listView?.setOnItemClickListener { _, _, position, _ ->
                    val itemId =  dataSnapshot.children.toList()[position].key

                    val activity = Intent(ctx, TaskActivity::class.java)
                    activity.putExtra("id", itemId)
                    startActivity(activity)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(ctx, getString(R.string.erro_ao_carregar_tarefas), Toast.LENGTH_SHORT).show()
            }
        })
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

    private fun setButtonsListeners() {
        binding.mainBtProfile?.setOnClickListener {
            val activity = Intent(this, ProfileActivity::class.java)
            startActivity(activity)
        }

        binding.floatBtAddTask?.setOnClickListener {
            val activity = Intent(this, TaskActivity::class.java)
            startActivity(activity)
        }
    }
}