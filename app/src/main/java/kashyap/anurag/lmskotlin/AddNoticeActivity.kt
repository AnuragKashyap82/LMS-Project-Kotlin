package kashyap.anurag.lmskotlin

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kashyap.anurag.lmskotlin.databinding.ActivityAddNoticeBinding

class AddNoticeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoticeBinding
    private  lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var pdfUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoticeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        binding.backBtn.setOnClickListener { onBackPressed() }

        binding.pdfBtn.setOnClickListener { pdfPickIntent() }

        binding.uploadNoticeBtn.setOnClickListener { validateData() }
    }

    private var title: String? = ""
    private var number: String? = ""

    private fun validateData() {
        title = binding.noticeTitleEt.text.toString().trim()
        number = binding.noticeNoEt.text.toString().trim()
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Enter Notice Title....", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(number)) {
            Toast.makeText(this, "Enter Notice Number....", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null) {
            Toast.makeText(this, "Pick Notice Pdf", Toast.LENGTH_SHORT).show()
        } else {
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        binding.progressBar.visibility = View.VISIBLE

        val timestamp = System.currentTimeMillis()
        val filePathAndName = "Notice/$timestamp"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener {taskSnapshot ->
                Toast.makeText(this, "Uploaded Successfully....!!!!", Toast.LENGTH_SHORT).show()
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadPdfUrl = "${uriTask.result}"

                uploadPdfToDb(uploadPdfUrl, timestamp)
            }
            .addOnFailureListener{
                binding.progressBar.visibility = View.VISIBLE
                Toast.makeText(this, "Notice pdf upload failed due to", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPdfToDb(uploadedPdfUrl: String, timestamp: Long) {
        binding.progressBar.visibility = View.VISIBLE
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["NoticeId"] = "" + timestamp
        hashMap["title"] = "" + title
        hashMap["number"] = "" + number
        hashMap["timestamp"] = "" + timestamp
        hashMap["url"] = "" + uploadedPdfUrl
        val documentReference = firebaseFirestore.collection("Notice").document("" + timestamp)
        documentReference.set(hashMap)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@AddNoticeActivity,
                    "Notice Successfully uploaded....",
                    Toast.LENGTH_SHORT
                ).show()
                clearData()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@AddNoticeActivity,
                    " Failed to upload to db due to" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
    private fun clearData() {
        pdfUri = null
        binding.noticeNoEt.setText("")
        binding.noticeTitleEt.setText("")
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