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
import kashyap.anurag.lmskotlin.databinding.ActivityPostClassroomMsgEditBinding

class PostClassroomMsgEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostClassroomMsgEditBinding
    private var classCode: String? = null
    private var msgCode: String? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseFirestore: FirebaseFirestore? = null

    private var progressDialog: ProgressDialog? = null
    private var pdfUri: Uri? = null
    private var classMsg: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostClassroomMsgEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Please wait")
        progressDialog!!.setCanceledOnTouchOutside(false)

        classCode = intent.getStringExtra("classCode")
        msgCode = intent.getStringExtra("msgCode")

        loadPostMessage(classCode!!, msgCode!!)

        binding.updatePostBtn.setOnClickListener { validateData() }
        binding.attachmentPickBtn.setOnClickListener { pdfPickIntent() }
        binding.pdfPickBtn.setOnClickListener { pdfPickIntent() }
    }

    private fun loadPostMessage(classCode: String, msgCode: String) {
        val documentReference =
            firebaseFirestore!!.collection("classroom").document(classCode).collection("classMsg")
                .document(msgCode)
        documentReference.addSnapshotListener(
            this
        ) { ds, error ->
            classMsg = "" + ds!!.getString("classMsg")
            binding.postTextEt.setText(classMsg)
        }
    }

    private fun validateData() {
        classMsg = binding.postTextEt.text.toString().trim()
        if (TextUtils.isEmpty(classMsg)) {
            Toast.makeText(
                this@PostClassroomMsgEditActivity,
                "Enter Your Message....!!!!",
                Toast.LENGTH_SHORT
            ).show()
        } else if (pdfUri == null) {
            postClassMsg(classMsg!!)
        } else {
            uploadAttachmentToStorage(classMsg!!)
        }
    }

    private fun postClassMsg(classMsg: String) {
        progressDialog!!.setMessage("Updating Message in Classroom....")
        progressDialog!!.show()
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "" + firebaseAuth!!.uid
        hashMap["classMsg"] = "" + classMsg
        hashMap["classCode"] = "" + classCode
        hashMap["timestamp"] = "" + msgCode
        val documentReference = firebaseFirestore!!.collection("classroom").document(
            classCode!!
        ).collection("classMsg").document(msgCode!!)
        documentReference
            .update(hashMap)
            .addOnSuccessListener {
                progressDialog!!.dismiss()
                Toast.makeText(
                    this@PostClassroomMsgEditActivity,
                    "Message Successfully Updated....",
                    Toast.LENGTH_SHORT
                ).show()
                clearText()
            }
            .addOnFailureListener { e ->
                progressDialog!!.dismiss()
                Toast.makeText(
                    this@PostClassroomMsgEditActivity,
                    " Failed to Post msg due to" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun uploadAttachmentToStorage(classMsg: String) {
        progressDialog!!.setMessage("Uploading Attachment")
        progressDialog!!.show()

        val filePathAndName = "Classroom Attachments/$msgCode"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener {taskSnapshot ->
                Toast.makeText(this, "Uploaded Successfully....!!!!", Toast.LENGTH_SHORT).show()
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedAttachmentUrl = "${uriTask.result}"

                uploadAttachmentToDb(uploadedAttachmentUrl, msgCode!!, classMsg);
            }
            .addOnFailureListener{
                progressDialog!!.dismiss()
                Toast.makeText(this, "Attachment pdf upload failed due to", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadAttachmentToDb(
        uploadedAttachmentUrl: String,
        msgCode: String,
        classMsg: String
    ) {
        progressDialog!!.setMessage("Updating Material Pdf into....")
        progressDialog!!.show()
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "" + firebaseAuth!!.uid
        hashMap["classMsg"] = "" + classMsg
        hashMap["classCode"] = "" + classCode
        hashMap["timestamp"] = "" + msgCode
        hashMap["url"] = "" + uploadedAttachmentUrl
        val documentReference = firebaseFirestore!!.collection("classroom").document(
            classCode!!
        ).collection("classMsg").document(msgCode)
        documentReference
            .update(hashMap)
            .addOnSuccessListener {
                progressDialog!!.dismiss()
                Toast.makeText(
                    this@PostClassroomMsgEditActivity,
                    "Attachment Successfully Updated....",
                    Toast.LENGTH_SHORT
                ).show()
                clearText()
            }
            .addOnFailureListener { e ->
                progressDialog!!.dismiss()
                Toast.makeText(
                    this@PostClassroomMsgEditActivity,
                    " Failed to upload to db due to" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun clearText() {
        binding.postTextEt.setText("")
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