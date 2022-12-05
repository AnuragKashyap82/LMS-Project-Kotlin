package kashyap.anurag.lmskotlin

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kashyap.anurag.lmskotlin.databinding.ActivityAddMaterialBinding

class AddMaterialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMaterialBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var semester: String? = null
    private var branch: String? = null
    private var subName: String? = null
    private var subTopic: String? = null
    private var progressDialog: ProgressDialog? = null
    private var pdfUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMaterialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Please wait")
        progressDialog!!.setCanceledOnTouchOutside(false)

        binding.semTv.setOnClickListener {
            val builder = AlertDialog.Builder(this@AddMaterialActivity)
            builder.setTitle("Select Semester:")
                .setItems(
                    Constants.semesterCategories
                ) { dialogInterface, i ->
                    val selectedSemester = Constants.semesterCategories[i]
                    binding.semTv.text = selectedSemester
                }.show()
        }
        binding.branchTv.setOnClickListener {
            val builder = AlertDialog.Builder(this@AddMaterialActivity)
            builder.setTitle("Select Branch:")
                .setItems(
                    Constants.branchCategories
                ) { dialogInterface, i ->
                    val selectedBranch = Constants.branchCategories[i]
                    binding.branchTv.text = selectedBranch
                }.show()
        }
        binding.pickPdfBtn.setOnClickListener(View.OnClickListener { pdfPickIntent() })
        binding.uploadMaterialBtn.setOnClickListener { validateData() }
    }

    private fun validateData() {
        subName = binding.subNameEt.text.toString().trim()
        subTopic = binding.subTopicEt.text.toString().trim()
        semester = binding.semTv.text.toString().trim()
        branch = binding.branchTv.text.toString().trim()
        if (TextUtils.isEmpty(subName)) {
            Toast.makeText(this@AddMaterialActivity, "Enter Subject Name", Toast.LENGTH_SHORT)
                .show()
        } else if (TextUtils.isEmpty(subTopic)) {
            Toast.makeText(this@AddMaterialActivity, "Enter Subject Topic", Toast.LENGTH_SHORT)
                .show()
        } else if (TextUtils.isEmpty(semester)) {
            Toast.makeText(this@AddMaterialActivity, "Select Semester", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(branch)) {
            Toast.makeText(this@AddMaterialActivity, "Select Branch", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null) {
            Toast.makeText(this@AddMaterialActivity, "Pick Material Pdf", Toast.LENGTH_SHORT).show()
        } else {
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        progressDialog!!.setMessage("Uploading Material")
        progressDialog!!.show()

        val timestamp = System.currentTimeMillis()
        val filePathAndName = "Material/$timestamp"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Toast.makeText(this, "Uploaded Successfully....!!!!", Toast.LENGTH_SHORT).show()
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadPdfUrl = "${uriTask.result}"

                uploadPdfToDb(uploadPdfUrl, timestamp)
            }
            .addOnFailureListener {
                progressDialog!!.dismiss()
                Toast.makeText(this, "Material pdf upload failed due to", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPdfToDb(uploadedMaterialUrl: String, timestamp: Long) {
        progressDialog!!.setMessage("Uploading Material Pdf into....")
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["materialId"] = "" + timestamp
        hashMap["subjectName"] = "" + subName
        hashMap["topicName"] = "" + subTopic
        hashMap["branch"] = "" + branch
        hashMap["semester"] = "" + semester
        hashMap["viewsCount"] = "0"
        hashMap["downloadsCount"] = "0"
        hashMap["timestamp"] = "" + timestamp
        hashMap["url"] = "" + uploadedMaterialUrl
        val ref = FirebaseDatabase.getInstance().getReference("Material")
        ref.child(branch!!).child(semester!!).child("" + timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog!!.dismiss()
                Toast.makeText(
                    this@AddMaterialActivity,
                    "Material Successfully uploaded....",
                    Toast.LENGTH_SHORT
                ).show()
                clearText()
            }
            .addOnFailureListener { e ->
                progressDialog!!.dismiss()
                Toast.makeText(
                    this@AddMaterialActivity,
                    " Failed to upload to db due to" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun clearText() {
        binding.subNameEt.setText("")
        binding.subTopicEt.setText("")
        binding.branchTv.text = ""
        binding.semTv.text = ""
        pdfUri = null
    }

    private fun pdfPickIntent(){
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }
    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{ result ->
            if (result.resultCode == RESULT_OK){
                pdfUri = result.data!!.data
            }else{
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

}