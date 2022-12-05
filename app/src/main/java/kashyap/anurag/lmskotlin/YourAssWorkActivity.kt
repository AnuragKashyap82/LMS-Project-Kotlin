package kashyap.anurag.lmskotlin

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kashyap.anurag.lmskotlin.databinding.ActivityYourAssWorkBinding

class YourAssWorkActivity : AppCompatActivity() {

    lateinit var binding: ActivityYourAssWorkBinding
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private var assignmentId: String? = null
    private var classCode: String? = null
    private var assignmentName: String? = null
    private var pdfUri: Uri? = null
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYourAssWorkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Please wait")
        progressDialog!!.setCanceledOnTouchOutside(false)

        assignmentId = intent.getStringExtra("assignmentId")
        assignmentName = intent.getStringExtra("assignmentName")
        classCode = intent.getStringExtra("classCode")

        loadMyAssignment()
    }

    private fun loadMyAssignment() {
        val documentReference = firebaseFirestore.collection("classroom").document(
            classCode!!
        ).collection("assignment").document(assignmentId!!).collection("Submission").document(
            firebaseAuth.uid!!
        )
        documentReference.addSnapshotListener { value, error ->
            val myAssignmentUrl = "" + value!!.getString("url")
            val assignmentName = "" + value.getString("assignmentName")
            binding.assignmentNameTv.text = assignmentName
            loadMaterialFromUrl(myAssignmentUrl)
        }
    }

    private fun loadMaterialFromUrl(myAssignmentUrl: String) {
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(myAssignmentUrl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                binding.myAssPdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange { page, pageCount ->
                        val currentPage = page + 1
                        binding.toolbarSubtitleTv.text = "$currentPage/$pageCount"
                    }
                    .onError { t ->
                        Toast.makeText(
                            this@YourAssWorkActivity,
                            "" + t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .onPageError { page, t ->
                        Toast.makeText(
                            this@YourAssWorkActivity,
                            "Error on page" + page + " " + t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { binding.progressBar.visibility = View.GONE }
    }
}