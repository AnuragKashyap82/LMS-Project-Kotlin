package kashyap.anurag.lmskotlin

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kashyap.anurag.lmskotlin.databinding.ActivityPostClassroomMsgBinding

class PostClassroomMsgActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostClassroomMsgBinding
    private var classMsg: String? = null
    private var classCode: String? = null
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private var progressDialog: ProgressDialog? = null
    private var pdfUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostClassroomMsgBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        classCode = intent.getStringExtra("classCode")

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Please wait")
        progressDialog!!.setCanceledOnTouchOutside(false)

        binding.postBtn.setOnClickListener { validateData() }
        binding.attachmentPickBtn.setOnClickListener {
            pdfPickIntent()
        }
        binding.pdfPickBtn.setOnClickListener {
            pdfPickIntent()
        }

    }

    private fun validateData() {
        classMsg = binding.postTextEt.text.toString().trim()
        if (TextUtils.isEmpty(classMsg)) {
            Toast.makeText(
                this@PostClassroomMsgActivity,
                "Enter Your Message....!!!!",
                Toast.LENGTH_SHORT
            ).show()
        } else if (pdfUri == null) {
            postClassMsg()
        } else {
            uploadAttachmentToStorage(classMsg!!)
        }
    }

    private fun postClassMsg() {
        progressDialog!!.setMessage("Posting Message in Classroom....")
        progressDialog!!.show()
        val timestamp = System.currentTimeMillis()
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "" + firebaseAuth.uid
        hashMap["classMsg"] = "" + classMsg
        hashMap["classCode"] = "" + classCode
        hashMap["timestamp"] = "" + timestamp
        hashMap["attachmentExist"] = false
        val documentReference = firebaseFirestore.collection("classroom").document(
            classCode!!
        ).collection("classMsg").document("" + timestamp)
        documentReference
            .set(hashMap)
            .addOnSuccessListener {
                progressDialog!!.dismiss()
                Toast.makeText(
                    this@PostClassroomMsgActivity,
                    "Message Successfully Posted....",
                    Toast.LENGTH_SHORT
                ).show()
                clearText()
            }
            .addOnFailureListener { e ->
                progressDialog!!.dismiss()
                Toast.makeText(
                    this@PostClassroomMsgActivity,
                    " Failed to Post msg due to" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

    }
    private fun clearText() {
        binding.postTextEt.setText("")
    }

    private fun uploadAttachmentToStorage(classMsg: String) {
        progressDialog!!.setMessage("Uploading Attachment")
        progressDialog!!.show()

        val timestamp = System.currentTimeMillis()
        val filePathAndName = "Classroom Attachments/$timestamp"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener {taskSnapshot ->
                Toast.makeText(this, "Uploaded Successfully....!!!!", Toast.LENGTH_SHORT).show()
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedAttachmentUrl = "${uriTask.result}"

                uploadAttachmentToDb(uploadedAttachmentUrl, timestamp, classMsg);
            }
            .addOnFailureListener{
                progressDialog!!.dismiss()
                Toast.makeText(this, "Attachment pdf upload failed due to", Toast.LENGTH_SHORT).show()
            }
    }
    private fun uploadAttachmentToDb(
        uploadedAttachmentUrl: String,
        timestamp: Long,
        classMsg: String
    ) {
        progressDialog!!.setMessage("Uploading Material Pdf into....")
        progressDialog!!.show()
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "" + firebaseAuth.uid
        hashMap["classMsg"] = "" + classMsg
        hashMap["classCode"] = "" + classCode
        hashMap["timestamp"] = "" + timestamp
        hashMap["attachmentExist"] = true
        hashMap["url"] = "" + uploadedAttachmentUrl
        val documentReference = firebaseFirestore.collection("classroom").document(
            classCode!!
        ).collection("classMsg").document("" + timestamp)
        documentReference
            .set(hashMap)
            .addOnSuccessListener {
                progressDialog!!.dismiss()
                Toast.makeText(
                    this@PostClassroomMsgActivity,
                    "Attachment Successfully uploaded....",
                    Toast.LENGTH_SHORT
                ).show()
                clearText()
            }
            .addOnFailureListener { e ->
                progressDialog!!.dismiss()
                Toast.makeText(
                    this@PostClassroomMsgActivity,
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