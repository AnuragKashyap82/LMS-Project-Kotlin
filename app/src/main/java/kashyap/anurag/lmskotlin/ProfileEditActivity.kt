package kashyap.anurag.lmskotlin

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kashyap.anurag.lmskotlin.databinding.ActivityProfileEditBinding

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private var image_uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore  = FirebaseFirestore.getInstance()

        checkUser()

        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.profileIv.setOnClickListener {
            showImagePickDialog()
        }

        binding.updateBtn.setOnClickListener {
            inputData()
        }
    }

    private var name: String? = ""
    private var phoneNumber: String? = ""
    private var email: String? = ""
    private var country: String? = ""
    private var state: String? = ""
    private var city: String? = ""
    private var address: String? = ""
    private var regNo: String? = ""
    private var dob: String? = ""
    private var fatherName: String? = ""
    private var motherName: String? = ""
    private var branch: String? = ""
    private var semester: String? = ""
    private var session: String? = ""
    private var seatType: String? = ""

    private fun inputData() {
        name = binding.nameEt.text.toString().trim()
        phoneNumber = binding.phoneTv.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        country = binding.countryEt.text.toString().trim()
        state = binding.stateEt.text.toString().trim()
        city = binding.cityEt.text.toString().trim()
        address = binding.addressEt.text.toString().trim()
        regNo = binding.regNoEt.text.toString().trim()
        dob = binding.dobEt.text.toString().trim()
        fatherName = binding.fatherNameEt.text.toString().trim()
        motherName = binding.motherNameEt.text.toString().trim()
        branch = binding.branchEt.text.toString().trim()
        semester = binding.semEt.text.toString().trim()
        session = binding.sessionEt.text.toString().trim()
        seatType = binding.seatTypeEt.text.toString().trim()
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Enter Name....", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Enter Phone Number....", Toast.LENGTH_SHORT).show()
            return
        }
        updateProfile()
    }

    private fun updateProfile() {
        binding.progressBar.visibility = View.VISIBLE
        if (image_uri == null) {
            val hashMap: HashMap<String, Any> = HashMap()
            hashMap["name"] = "" + name
            hashMap["phone"] = "" + phoneNumber
            hashMap["email"] = "" + email
            hashMap["country"] = "" + country
            hashMap["state"] = "" + state
            hashMap["city"] = "" + city
            hashMap["address"] = "" + address
            hashMap["regNo"] = "" + regNo
            hashMap["dob"] = "" + dob
            hashMap["fatherName"] = "" + fatherName
            hashMap["motherName"] = "" + motherName
            hashMap["branch"] = "" + branch
            hashMap["semester"] = "" + semester
            hashMap["session"] = "" + session
            hashMap["seatType"] = "" + seatType
            val documentReference = firebaseFirestore.collection("Users").document(
                firebaseAuth.uid!!
            )
            documentReference.update(hashMap)
                .addOnSuccessListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@ProfileEditActivity, "Profile Updated", Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener { e ->
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@ProfileEditActivity, "" + e.message, Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            val filePathAndName = "profile_images/" + "" + firebaseAuth.uid
            val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
            storageReference.putFile(image_uri!!)
                .addOnSuccessListener { taskSnapshot ->
                    val uriTask = taskSnapshot.storage.downloadUrl
                    while (!uriTask.isSuccessful);
                    val downloadImageUri = uriTask.result
                    if (uriTask.isSuccessful) {
                        val hashMap = HashMap<String, Any>()
                        hashMap["name"] = "" + name
                        hashMap["phone"] = "" + phoneNumber
                        hashMap["email"] = "" + email
                        hashMap["country"] = "" + country
                        hashMap["state"] = "" + state
                        hashMap["city"] = "" + city
                        hashMap["address"] = "" + address
                        hashMap["regNo"] = "" + regNo
                        hashMap["dob"] = "" + dob
                        hashMap["fatherName"] = "" + fatherName
                        hashMap["motherName"] = "" + motherName
                        hashMap["branch"] = "" + branch
                        hashMap["semester"] = "" + semester
                        hashMap["session"] = "" + session
                        hashMap["seatType"] = "" + seatType
                        hashMap["profileImage"] = "" + downloadImageUri
                        val documentReference = firebaseFirestore.collection("Users").document(
                            firebaseAuth.uid!!
                        )
                        documentReference.update(hashMap)
                            .addOnSuccessListener {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    this@ProfileEditActivity,
                                    "Profile Updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    this@ProfileEditActivity,
                                    "" + e.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@ProfileEditActivity, "" + e.message, Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }
    private fun checkUser() {
        val user = firebaseAuth.currentUser
        if (user == null) {
            startActivity(Intent(applicationContext, LoginActivity::class.java))
            finish()
        } else {
            loadMyInfo()
            if (!user.isEmailVerified) {
                binding.emailNotVerifiedRl.visibility = View.VISIBLE
                binding.emailRl.visibility = View.GONE
            } else {
                binding.emailNotVerifiedRl.visibility = View.GONE
                binding.emailRl.visibility = View.VISIBLE
            }
        }
    }

    private fun loadMyInfo() {
        val documentReference = firebaseFirestore.collection("Users").document(
            firebaseAuth.uid!!
        )
        documentReference.addSnapshotListener(
            this
        ) { ds, error ->
            val userType = "" + ds!!.getString("userType")
            val email = "" + ds.getString("email")
            val name = "" + ds.getString("name")
            val address = "" + ds.getString("address")
            val city = "" + ds.getString("city")
            val state = "" + ds.getString("state")
            val country = "" + ds.getString("country")
            val uniqueId = "" + ds.getString("uniqueId")
            val online = "" + ds.getString("online")
            val phone = "" + ds.getString("phone")
            val profileImage = "" + ds.getString("profileImage")
            val timestamp = "" + ds.getString("timestamp")
            val uid = "" + ds.getString("uid")
            val regNo = "" + ds.getString("regNo")
            val dob = "" + ds.getString("dob")
            val fatherName = "" + ds.getString("fatherName")
            val motherName = "" + ds.getString("motherName")
            val branch = "" + ds.getString("branch")
            val semester = "" + ds.getString("semester")
            val session = "" + ds.getString("session")
            val seatType = "" + ds.getString("seatType")
            val hostler = "" + ds.getString("hostler")

            binding.nameEt.setText(name)
            binding.phoneTv.text = phone
            binding.emailEt.text = email
            binding.addressEt.setText(address)
            binding.cityEt.setText(city)
            binding.stateEt.setText(state)
            binding.countryEt.setText(country)
            binding.regNoEt.setText(regNo)
            binding.dobEt.setText(dob)
            binding.fatherNameEt.setText(fatherName)
            binding.motherNameEt.setText(motherName)
            binding.branchEt.setText(branch)
            binding.semEt.setText(semester)
            binding.sessionEt.setText(session)
            binding.seatTypeEt.setText(seatType)
            try {
                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray)
                    .into(binding.profileIv)
            } catch (e: Exception) {
                binding.profileIv.setImageResource(R.drawable.ic_person_gray)
            }
        }
    }
    private fun showImagePickDialog() {
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Image")
            .setItems(options) { dialogInterface, i ->
                if (i == 0) {
                    pickImageCamera()
                } else {
                    pickImageGallery()
                }
            }
            .show()
    }

    private fun pickImageCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Pick")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        cameraActivityResultLauncher.launch(intent)
    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }
    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            binding.profileIv.setImageURI(image_uri)
        } else {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            image_uri = data!!.data

            binding.profileIv.setImageURI(image_uri)
        } else {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}