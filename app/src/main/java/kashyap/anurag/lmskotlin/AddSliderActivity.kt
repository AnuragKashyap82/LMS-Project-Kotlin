package kashyap.anurag.lmskotlin

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kashyap.anurag.lmskotlin.databinding.ActivityAddSliderBinding

class AddSliderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddSliderBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var progressDialog: ProgressDialog? = null

    private val CAMERA_REQUEST_CODE = 100
    private val STORAGE_REQUEST_CODE = 200

    private var image_uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSliderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Please Wait")
        progressDialog!!.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.addSliderBtn.setOnClickListener {
            validateData()
        }
        binding.sliderImage.setOnClickListener {
            showImagePickDialog()
        }
    }

    private fun validateData() {
        if (image_uri == null) {
            Toast.makeText(this, "Add Slider Image to be Uploaded....!", Toast.LENGTH_SHORT).show()
            return
        } else {
            addSlider()
        }
    }
    private fun addSlider() {
        progressDialog!!.setMessage("Updating Slider")
        progressDialog!!.show()
        val timestamp = "" + System.currentTimeMillis()
        val filePathAndName = "slider_images/$timestamp"
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(image_uri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "" + uriTask.result
                if (uriTask.isSuccessful) {
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["url"] = "" + uploadedImageUrl
                    hashMap["sliderId"] = "" + timestamp
                    hashMap["uid"] = "" + firebaseAuth.uid
                    val ref = FirebaseDatabase.getInstance().getReference("Slider")
                    ref.child(timestamp)
                        .setValue(hashMap)
                        .addOnSuccessListener {
                            progressDialog!!.dismiss()
                            Toast.makeText(
                                this@AddSliderActivity,
                                "Slider Added Successfully...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            progressDialog!!.dismiss()
                            Toast.makeText(
                                this@AddSliderActivity,
                                "" + e.message,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                }
            }
            .addOnFailureListener { e ->

                progressDialog!!.dismiss()
                Toast.makeText(
                    this,
                    "Failed to upload image due to\"+e.getMessage()",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener(OnFailureListener { e ->
                progressDialog!!.dismiss()
                Toast.makeText(this@AddSliderActivity, "" + e.message, Toast.LENGTH_SHORT).show()
            })
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
            binding.sliderImage.setImageURI(image_uri)
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

            binding.sliderImage.setImageURI(image_uri)
        } else {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}