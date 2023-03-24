package com.example.finalprojectrodrigoaugusto

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.finalprojectrodrigoaugusto.databinding.ActivityProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import android.Manifest
import android.graphics.BitmapFactory
import android.widget.ImageView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class ProfileActivity : AppCompatActivity() {

    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var mGoogleSignClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    private val PERMISSION_REQUEST_CAMERA = 0
    private val PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1

    private var _image: Bitmap? = null

    companion object {
        private const val REQUEST_IMAGE_GALLERY = 1
        private const val REQUEST_IMAGE_CAPTURE = 2
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setButtonsListener()
        checkPermissions()

        setUpGoogleSignIn()
        loadDataFireBaseAuth()
    }

    private fun setButtonsListener() {
        binding.profileBtHome.setOnClickListener {
            val activity = Intent(this, MainActivity::class.java)
            startActivity(activity)
        }

        binding.profileBtLogOut.setOnClickListener {
            firebaseAuth.signOut()
            mGoogleSignClient.signOut()

            val activity = Intent(this, LoginScreen::class.java)
            startActivity(activity)
            finish()
        }

        binding.profileBtChangePassword.setOnClickListener {
            val activity = Intent(this, ChangePasswordActivity::class.java)
            startActivity(activity)
            finish()
        }

        binding.profileBtUpdateProfile.setOnClickListener {
            saveProfile()
        }

        binding.profileBtCamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }

        binding.profileBtGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
        }
    }

    private fun setUpGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignClient = GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun loadDataFireBaseAuth() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            val displayName = user.displayName
            val email = user.email
            val photoUrl = user.photoUrl

            if (displayName.toString() != "") {
                val nameSplit = displayName.toString().split(" ")
                if (nameSplit.size > 1) {
                    binding.profileEditTxtName.setText(nameSplit[0])
                    binding.profileEditTxtLastName.setText(nameSplit[1])
                } else {
                    binding.profileEditTxtName.setText(displayName.toString())
                }
            }
            binding.profileEditTxtEmail.setText(email)

            if (photoUrl != null) {
                Thread {
                    val file = saveLocalFile(photoUrl.toString())
                    runOnUiThread {
                        val bitmap = BitmapFactory.decodeFile(file.path)
                        findViewById<ImageView>(R.id.profile_image).setImageBitmap(bitmap)
                    }
                }.start()
            }
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CAMERA
            )
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun saveLocalFile(_url: String): File {
        val url = URL(_url)
        val connection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()

        val input = connection.inputStream
        val dir = File(getExternalFilesDir(null), "images")
        if (!dir.exists()) {
            dir.mkdir()
        }

        val file = File(dir, "imagem.jpg")
        val output = FileOutputStream(file)

        val buffer = ByteArray(1024)
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            output.write(buffer, 0, read)
        }

        output.flush()
        output.close()
        input.close()

        return file
    }

    private fun saveProfile() {
        val name = binding.profileEditTxtName.text.toString()
        val lastName = binding.profileEditTxtLastName.text.toString()

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        val imageRef = storageRef.child("profile_images/${uid}")
        val baos = ByteArrayOutputStream()
        this._image?.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        val data = baos.toByteArray()
        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnFailureListener {
            Toast.makeText(this, "Falha ao salvar imagem", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val profileUpdates = userProfileChangeRequest {
                    displayName = "$name $lastName"
                    photoUri = Uri.parse(uri.toString())
                }

                user!!.updateProfile(profileUpdates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Perfil atualizado com sucesso", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, "Falha ao atualizar perfil", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_GALLERY -> {
                    val selectedImage: Uri? = data?.data
                    val imageBitmap =
                        MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)

                    binding.profileImage.setImageBitmap(imageBitmap)
                    this._image = imageBitmap
                }
                REQUEST_IMAGE_CAPTURE -> {
                    val imageCaptured = data?.extras?.get("data") as Bitmap

                    binding.profileImage.setImageBitmap(imageCaptured)
                    this._image = imageCaptured
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão de camera concedida", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissão de camera negada", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão de galeria concedida", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissão de galeria negada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}