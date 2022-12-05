package kashyap.anurag.lmskotlin

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kashyap.anurag.lmskotlin.Constants.Companion.MAX_BYTES_PDF
import kashyap.anurag.lmskotlin.databinding.ActivityAssignmentViewBinding
import java.io.FileOutputStream

class AssignmentViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssignmentViewBinding
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private var pdfUri: Uri? = null
    private var assignmentId: String? = null
    private var assignmentName: String? = null
    private var assignmentUrl: String? = null
    private var dueDate: String? = null
    private var fullMarks: String? = null
    private var classCode: String? = null
    private var isCompletedAssignment: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignmentViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        assignmentUrl = intent.getStringExtra("assignmentUrl")
        assignmentName = intent.getStringExtra("assignmentName")
        classCode = intent.getStringExtra("classCode")
        assignmentId = intent.getStringExtra("assignmentId")
        dueDate = intent.getStringExtra("dueDate")
        fullMarks = intent.getStringExtra("fullMarks")

        checkUser()
        loadAssignment()

        binding.deleteAssignmentBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this@AssignmentViewActivity)
            builder.setTitle("Delete")
                .setMessage("Are you sure want to Delete Assignment: $assignmentId ?")
                .setPositiveButton(
                    "Yes"
                ) { dialogInterface, i -> deleteAssignment() }
                .setNegativeButton(
                    "No"
                ) { dialogInterface, i -> dialogInterface.dismiss() }
                .show()
        }
        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.editAssignmentBtn.setOnClickListener {
            val intent = Intent(this@AssignmentViewActivity, EditAssignmentActivity::class.java)
            intent.putExtra("assignmentId", assignmentId)
            intent.putExtra("classCode", classCode)
            startActivity(intent)
            Toast.makeText(
                this@AssignmentViewActivity,
                "Edit Material Btn Clicked....!",
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.downloadAssignmentBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this@AssignmentViewActivity)
            builder.setTitle("Download")
                .setMessage("Do you want to Download Notice: $assignmentId ?")
                .setPositiveButton(
                    "Yes"
                ) { dialogInterface, i ->
                    if (ContextCompat.checkSelfPermission(
                            this@AssignmentViewActivity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        downloadAssignment(
                            this@AssignmentViewActivity,
                            "" + classCode,
                            "" + assignmentId,
                            "" + assignmentUrl
                        )
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
                .setNegativeButton(
                    "No"
                ) { dialogInterface, i -> dialogInterface.dismiss() }
                .show()
        }
        binding.addAssBtn.setOnClickListener { detailsBottomSheet(dueDate!!, fullMarks!!) }
    }

    private fun detailsBottomSheet(dueDate: String, fullMarks: String) {
        val dialog = Dialog(this@AssignmentViewActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bs_add_assignment)
        val dueDateTv = dialog.findViewById<TextView>(R.id.dueDateTv)
        val submitAssignmentBtn = dialog.findViewById<Button>(R.id.submitAssignmentBtn)
        val submittedAssignmentBtn = dialog.findViewById<Button>(R.id.submittedAssignmentBtn)
        val assWorkViewBtn = dialog.findViewById<Button>(R.id.assWorkViewBtn)
        val pdfPickBtn = dialog.findViewById<ImageButton>(R.id.pdfPickBtn)
        val completedTv = dialog.findViewById<TextView>(R.id.completedTv)
        val notCompletedTv = dialog.findViewById<TextView>(R.id.notCompletedTv)
        val fullMarksTv = dialog.findViewById<TextView>(R.id.fullMarksTv)
        val obtainedMarksTv = dialog.findViewById<TextView>(R.id.obtainedMarksTv)
        dialog.show()
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.dialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dueDateTv.text = dueDate
        fullMarksTv.text = fullMarks

        loadMarks(obtainedMarksTv)

        completedTv.visibility = View.GONE

        checkCompleted(completedTv, notCompletedTv, submitAssignmentBtn, assWorkViewBtn)

        pdfPickBtn.setOnClickListener { pdfPickIntent() }

        submitAssignmentBtn.setOnClickListener {
            validateData()
            dialog.dismiss()
        }
        assWorkViewBtn.setOnClickListener {
            val intent = Intent(this@AssignmentViewActivity, YourAssWorkActivity::class.java)
            intent.putExtra("assignmentId", assignmentId)
            intent.putExtra("assignmentName", assignmentName)
            intent.putExtra("classCode", classCode)
            startActivity(intent)
            dialog.dismiss()
        }
        submittedAssignmentBtn.setOnClickListener {
            val intent =
                Intent(this@AssignmentViewActivity, SubmittedAssignmentActivity::class.java)
            intent.putExtra("assignmentId", assignmentId)
            intent.putExtra("classCode", classCode)
            intent.putExtra("assignmentName", assignmentName)
            startActivity(intent)
            dialog.dismiss()
        }
    }

    private fun checkCompleted(
        completedTv: TextView,
        notCompletedTv: TextView,
        submitAssignmentBtn: Button,
        assWorkViewBtn: Button
    ) {
        val documentReference = firebaseFirestore.collection("classroom").document(
            classCode!!
        ).collection("assignment").document(assignmentId!!).collection("Submission").document(
            firebaseAuth.uid!!
        )
        documentReference.addSnapshotListener { value, error ->
            isCompletedAssignment = value!!.exists()
            if (isCompletedAssignment!!) {
                completedTv.visibility = View.VISIBLE
                notCompletedTv.visibility = View.GONE
                submitAssignmentBtn.text = "Replace Your Work"
                assWorkViewBtn.visibility = View.VISIBLE
            } else {
                completedTv.visibility = View.GONE
                notCompletedTv.visibility = View.VISIBLE
                submitAssignmentBtn.visibility = View.VISIBLE
                assWorkViewBtn.visibility = View.GONE
            }
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

    private fun validateData() {
        if (pdfUri == null) {
            Toast.makeText(this, "Pick Your Assignment....!", Toast.LENGTH_SHORT).show()
        } else {
            uploadAssignmentToStorage()
        }
    }

    private fun uploadAssignmentToStorage() {
        binding.progressBar.visibility = View.VISIBLE

        val timestamp = System.currentTimeMillis()
        val filePathAndName = "Assignment/$timestamp"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener {taskSnapshot ->
                Toast.makeText(this, "Uploaded Successfully....!!!!", Toast.LENGTH_SHORT).show()
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedAssignmentUrl = "${uriTask.result}"

                uploadAssignmentWorkInfoToDb(uploadedAssignmentUrl, timestamp)
            }
            .addOnFailureListener{
                binding.progressBar.visibility = View.VISIBLE
                Toast.makeText(this, "Assignment pdf upload failed due to", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadAssignmentWorkInfoToDb(uploadedAssignmentUrl: String, timestamp: Long) {
        binding.progressBar.visibility = View.VISIBLE
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["assignmentId"] = "" + assignmentId
        hashMap["classCode"] = "" + classCode
        hashMap["dueDate"] = "" + dueDate
        hashMap["fullMarks"] = "" + fullMarks
        hashMap["assignmentName"] = "" + assignmentName
        hashMap["marksObtained"] = ""
        hashMap["timestamp"] = "" + timestamp
        hashMap["uid"] = "" + firebaseAuth.uid
        hashMap["url"] = "" + uploadedAssignmentUrl
        val documentReference = firebaseFirestore.collection("classroom").document(
            classCode!!
        ).collection("assignment").document(assignmentId!!).collection("Submission").document(
            firebaseAuth.uid!!
        )
        documentReference
            .set(hashMap)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@AssignmentViewActivity,
                    "Work Successfully uploaded....",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@AssignmentViewActivity,
                    " Failed to upload to db due to" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun deleteAssignment() {
        val documentReference = firebaseFirestore.collection("classroom").document(
            classCode!!
        ).collection("assignment").document(assignmentId!!)
        documentReference
            .delete()
            .addOnSuccessListener {
                Toast.makeText(
                    this@AssignmentViewActivity,
                    "Assignment Deleted...!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@AssignmentViewActivity,
                    "" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loadAssignment() {
        val documentReference = firebaseFirestore.collection("classroom").document(
            classCode!!
        ).collection("assignment").document(assignmentId!!)
        documentReference.addSnapshotListener { value, error ->
            val assignmentUrl = "" + value!!.getString("url")
            val assignmentName = "" + value.getString("assignmentName")
            dueDate = "" + value.getString("dueDate")
            binding.assignmentNameTv.text = assignmentName
            loadMaterialFromUrl(assignmentUrl)
        }
    }

    private fun loadMaterialFromUrl(assignmentUrl: String) {
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(assignmentUrl)
        reference.getBytes(MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                binding.assignmentPdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange { page, pageCount ->
                        val currentPage = page + 1
                        binding.toolbarSubtitleTv.text = "$currentPage/$pageCount"
                    }
                    .onError { t ->
                        Toast.makeText(
                            this@AssignmentViewActivity,
                            "" + t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .onPageError { page, t ->
                        Toast.makeText(
                            this@AssignmentViewActivity,
                            "Error on page" + page + " " + t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { binding.progressBar.visibility = View.GONE }
    }

    private fun loadMarks(obtainedMarksTv: TextView) {
        val documentReference = firebaseFirestore.collection("classroom").document(
            classCode!!
        ).collection("assignment").document(assignmentId!!).collection("Submission").document(
            firebaseAuth.uid!!
        )
        documentReference.addSnapshotListener { value, error ->
            val marksObtained = "" + value!!.getString("marksObtained")
            obtainedMarksTv.text = marksObtained
        }
    }

    private val requestPermissionLauncher = registerForActivityResult<String, Boolean>(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            downloadAssignment(
                this@AssignmentViewActivity,
                "" + classCode,
                "" + assignmentId,
                "" + assignmentUrl
            )
        } else {
            Toast.makeText(this, "Permission was denied...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadAssignment(
        assignmentViewActivity: AssignmentViewActivity,
        classCode: String,
        assignmentId: String,
        assignmentUrl: String
    ) {
        val nameWithExtension = "$classCode.pdf"
        val progressDialog = ProgressDialog(this@AssignmentViewActivity)
        progressDialog.setTitle("Please Wait")
        progressDialog.setMessage("Downloading$nameWithExtension....")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(assignmentUrl)
        storageReference.getBytes(MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                saveDownloadedBook(
                    this@AssignmentViewActivity,
                    progressDialog,
                    bytes,
                    nameWithExtension,
                    classCode,
                    assignmentId
                )
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this@AssignmentViewActivity,
                    "Failed to download due to" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun saveDownloadedBook(
        assignmentViewActivity: AssignmentViewActivity,
        progressDialog: ProgressDialog,
        bytes: ByteArray,
        nameWithExtension: String,
        classCode: String,
        assignmentId: String
    ) {
        try {
            val downloadsFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdir()
            val filePath = downloadsFolder.path + "/" + nameWithExtension
            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()
            Toast.makeText(
                this@AssignmentViewActivity,
                "Saved to Download Folder",
                Toast.LENGTH_SHORT
            ).show()
            progressDialog.dismiss()
        } catch (e: Exception) {
            Toast.makeText(
                this@AssignmentViewActivity,
                "Failed saving to download folder due to" + e.message,
                Toast.LENGTH_SHORT
            ).show()
            progressDialog.dismiss()
        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userType = "" + snapshot.child("userType").value

//                        if (userType.equals("user")){
//                            binding.fb.setVisibility(View.GONE);
//                        }
//                        else if (userType.equals("admin")){
//                            binding.fb.setVisibility(View.VISIBLE);
//                        }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}