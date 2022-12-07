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
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kashyap.anurag.lmskotlin.databinding.ActivityEditMaterialBinding

class EditMaterialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditMaterialBinding
    private var materialId: String? = null
    private var semester: String? = null
    private var branch: String? = null
    private var topic: String? = null
    private var subject: String? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private var progressDialog: ProgressDialog? = null
    private var pdfUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditMaterialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        materialId = intent.getStringExtra("materialId")
        branch = intent.getStringExtra("branch")
        semester = intent.getStringExtra("semester")

        loadMaterial()

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Please wait")
        progressDialog!!.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener { onBackPressed() }

        binding.pickPdfBtn.setOnClickListener(View.OnClickListener { pdfPickIntent() })
        binding.updateMaterialBtn.setOnClickListener(View.OnClickListener { validateData() })

    }

    private fun loadMaterial() {
        val ref = FirebaseDatabase.getInstance().getReference("Material")
        ref.child(branch!!).child(semester!!).child(materialId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val materialUrl = "" + snapshot.child("url").value
                    branch = "" + snapshot.child("branch").value
                    semester = "" + snapshot.child("semester").value
                    topic = "" + snapshot.child("topicName").value
                    subject = "" + snapshot.child("subjectName").value
                    binding.subNameEt.setText(subject)
                    binding.subTopicEt.setText(topic)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun validateData() {
        subject = binding.subNameEt.text.toString().trim()
        topic = binding.subTopicEt.text.toString().trim()
        if (TextUtils.isEmpty(subject)) {
            Toast.makeText(this, "Enter Subject Name....!", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(topic)) {
            Toast.makeText(this, "Enter Topic....!", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null) {
            Toast.makeText(this, "Pick Material Pdf", Toast.LENGTH_SHORT).show()
        } else {
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        progressDialog!!.setMessage("Uploading Material")
        progressDialog!!.show()

        val filePathAndName = "Material/$materialId"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Toast.makeText(this, "Uploaded Successfully....!!!!", Toast.LENGTH_SHORT).show()
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadPdfUrl = "${uriTask.result}"

                uploadPdfToDb(uploadPdfUrl, materialId!!)
            }
            .addOnFailureListener {
                progressDialog!!.dismiss()
                Toast.makeText(this, "Material pdf upload failed due to", Toast.LENGTH_SHORT).show()
            }
    }
    private fun uploadPdfToDb(uploadedMaterialUrl: String, materialId: String) {
        progressDialog!!.setMessage("Uploading Material Pdf into....")
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["materialId"] = "" + materialId
        hashMap["subjectName"] = "" + subject
        hashMap["topicName"] = "" + topic
        hashMap["branch"] = "" + branch
        hashMap["semester"] = "" + semester
        hashMap["timestamp"] = "" + materialId
        hashMap["url"] = "" + uploadedMaterialUrl
        val ref = FirebaseDatabase.getInstance().getReference("Material")
        ref.child(branch!!).child(semester!!).child(materialId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog!!.dismiss()
                Toast.makeText(
                    this@EditMaterialActivity,
                    "Material Successfully Updated....",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                progressDialog!!.dismiss()
                Toast.makeText(
                    this@EditMaterialActivity,
                    " Failed to upload to db due to" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
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