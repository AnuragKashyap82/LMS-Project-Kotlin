package kashyap.anurag.lmskotlin

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kashyap.anurag.lmskotlin.databinding.ActivitySubmittedAssViewBinding
import kashyap.anurag.lmskotlin.databinding.DialogMarksObtainedBinding

class SubmittedAssViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubmittedAssViewBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var submittedAssUrl: String? = null
    private var assignmentId: String? = null
    private var assignmentName: String? = null
    private var classCode: String? = null
    private var dueDate: String? = null
    private var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        binding = ActivitySubmittedAssViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        submittedAssUrl = intent.getStringExtra("submittedAssUrl")
        assignmentId = intent.getStringExtra("assignmentId")
        classCode = intent.getStringExtra("classCode")
        assignmentName = intent.getStringExtra("assignmentName")
        dueDate = intent.getStringExtra("dueDate")
        uid = intent.getStringExtra("uid")

        binding.assignmentNameTv.text = assignmentName

        loadSubmittedAssFromUrl(submittedAssUrl!!)
        checkUser()
        loadMarks()

        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.addMarksBtn.setOnClickListener {
            val marksAddBinding: DialogMarksObtainedBinding =
                DialogMarksObtainedBinding.inflate(LayoutInflater.from(this@SubmittedAssViewActivity))
            val builder =
                AlertDialog.Builder(this@SubmittedAssViewActivity, R.style.CustomDialog)
            builder.setView(marksAddBinding.getRoot())
            val alertDialog = builder.create()
            alertDialog.show()
            marksAddBinding.backBtn.setOnClickListener(View.OnClickListener { alertDialog.dismiss() })
            marksAddBinding.submitBtn.setOnClickListener(View.OnClickListener {
                val marksObtained: String =
                    marksAddBinding.marksEt.getText().toString().trim()
                if (TextUtils.isEmpty(marksObtained)) {
                    Toast.makeText(
                        this@SubmittedAssViewActivity,
                        "Enter His Marks Obtained....!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    alertDialog.dismiss()
                    uploadObtainedMarks(marksObtained)
                }
            })
        }
    }

    private fun uploadObtainedMarks(marksObtained: String) {
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["marksObtained"] = "" + marksObtained
        val documentReference = firebaseFirestore.collection("classroom").document(
            classCode!!
        ).collection("assignment").document(assignmentId!!).collection("Submission").document(uid!!)
        documentReference
            .update(hashMap)
            .addOnSuccessListener {
                Toast.makeText(
                    this@SubmittedAssViewActivity,
                    "Marks Uploaded....!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@SubmittedAssViewActivity,
                    " Failed to upload to db due to" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loadSubmittedAssFromUrl(submittedAssUrl: String) {
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(submittedAssUrl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                binding.submittedAssPdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange { page, pageCount ->
                        val currentPage = page + 1
                        binding.toolbarSubtitleTv.text = "$currentPage/$pageCount"
                    }
                    .onError { t ->
                        Toast.makeText(
                            this@SubmittedAssViewActivity,
                            "" + t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .onPageError { page, t ->
                        Toast.makeText(
                            this@SubmittedAssViewActivity,
                            "Error on page" + page + " " + t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { binding.progressBar.visibility = View.GONE }
    }

    private fun loadMarks() {
        val documentReference = firebaseFirestore.collection("classroom").document(
            classCode!!
        )
            .collection("assignment").document(assignmentId!!).collection("Submission")
            .document(uid!!)
        documentReference.addSnapshotListener { value, error ->
            val fullMarks = "" + value!!.getString("fullMarks")
            val marksObtained = "" + value.getString("marksObtained")
            binding.fullMarksTv.text = fullMarks
            binding.obtainedMarksTv.text = marksObtained
        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userType = "" + snapshot.child("userType").value
                    val name = "" + snapshot.child("name").value

//                        if (userType.equals("user")) {
//                            binding.nameTv.setText(name);
//                        } else if (userType.equals("admin")) {
//                            binding.nameTv.setText(name);
//                        }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}