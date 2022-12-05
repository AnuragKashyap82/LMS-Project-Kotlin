package kashyap.anurag.lmskotlin

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kashyap.anurag.lmskotlin.databinding.ActivityEditAssignmentBinding
import java.text.DecimalFormat
import java.util.*

class EditAssignmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditAssignmentBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var progressDialog: ProgressDialog? = null
    private var pdfUri: Uri? = null
    private var assignmentId: String? = null
    private var classCode: String? = null
    private var fullMarks: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAssignmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        assignmentId = intent.getStringExtra("assignmentId")
        classCode = intent.getStringExtra("classCode")

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Please wait")
        progressDialog!!.setCanceledOnTouchOutside(false)

        loadAssignmentInfo()

        binding.backBtn.setOnClickListener { onBackPressed() }

        binding.pickPdfBtn.setOnClickListener { pdfPickIntent() }
        binding.updateAssBtn.setOnClickListener { validateData() }
        binding.fullMarksTv.setOnClickListener {
            val builder = AlertDialog.Builder(this@EditAssignmentActivity)
            builder.setTitle("Select Full Marks:")
                .setItems(
                    Constants.marksCategories
                ) { dialogInterface, i ->
                    val selectedMarks = Constants.marksCategories[i]
                    binding.fullMarksTv.text = selectedMarks
                }.show()
        }
        binding.dueDateTv.setOnClickListener { datePickDialog() }

    }

    private fun loadAssignmentInfo() {
        val documentReference = firebaseFirestore.collection("classroom").document(
            classCode!!
        ).collection("assignment").document(assignmentId!!)
        documentReference.addSnapshotListener { value, error ->
            val assignmentUrl = "" + value!!.getString("url")
            fullMarks = "" + value.getString("fullMarks")
            assignmentName = "" + value.getString("assignmentName")
            dueDate = "" + value.getString("dueDate")
            binding.assNameEt.setText(assignmentName)
            binding.fullMarksTv.text = fullMarks
            binding.dueDateTv.setText(dueDate)
        }
    }

    private fun datePickDialog() {
        val calendar = Calendar.getInstance()
        val mYear = calendar[Calendar.YEAR]
        val mMonth = calendar[Calendar.MONTH] + 1
        val mDay = calendar[Calendar.DAY_OF_MONTH]
        val datePickerDialog = DatePickerDialog(this,
            { datePicker, year, monthOfYear, dayOfMonth ->
                val mFormat = DecimalFormat("00")
                val pDay = mFormat.format(dayOfMonth.toLong())
                val pMonth = mFormat.format(monthOfYear.toLong())
                val pYear = "" + year
                val pDate = "$pDay/$pMonth/$pYear"
                binding.dueDateTv.text = pDate
            }, mYear, mMonth, mDay
        )
        datePickerDialog.show()
        datePickerDialog.datePicker
    }

    private var assignmentName: String? = null
    private var dueDate: String? = null

    private fun validateData() {
        assignmentName = binding.assNameEt.text.toString().trim()
        dueDate = binding.dueDateTv.text.toString().trim()
        fullMarks = binding.fullMarksTv.text.toString().trim()
        if (TextUtils.isEmpty(assignmentName)) {
            Toast.makeText(this, "Enter Assignment Name....!", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(fullMarks)) {
            Toast.makeText(this, "Enter Full Marks....!", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(dueDate)) {
            Toast.makeText(this, "Select Due Date....!", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null) {
            Toast.makeText(this, "Pick Ass Pdf", Toast.LENGTH_SHORT).show()
        } else {
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        progressDialog!!.setMessage("Updating Assignment")
        progressDialog!!.show()

        val filePathAndName = "Assignment/$assignmentId"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener {taskSnapshot ->
                Toast.makeText(this, "Uploaded Successfully....!!!!", Toast.LENGTH_SHORT).show()
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadPdfUrl = "${uriTask.result}"

                updateAssignmentInfoToDb(uploadPdfUrl, assignmentId!!)
            }
            .addOnFailureListener{
                progressDialog!!.dismiss()
                Toast.makeText(this, "Notice pdf upload failed due to", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateAssignmentInfoToDb(uploadedAssignmentUrl: String, assignmentId: String) {
        progressDialog!!.setMessage("Updating Assignment into....")
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["assignmentId"] = "" + assignmentId
        hashMap["assignmentName"] = "" + assignmentName
        hashMap["fullMarks"] = "" + fullMarks
        hashMap["dueDate"] = "" + dueDate
        hashMap["uid"] = "" + firebaseAuth.uid
        hashMap["url"] = "" + uploadedAssignmentUrl
        val documentReference = firebaseFirestore.collection("classroom").document(
            classCode!!
        ).collection("assignment").document(assignmentId)
        documentReference
            .update(hashMap)
            .addOnSuccessListener {
                progressDialog!!.dismiss()
                Toast.makeText(
                    this@EditAssignmentActivity,
                    "Assignment Successfully Updated....",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                progressDialog!!.dismiss()
                Toast.makeText(
                    this@EditAssignmentActivity,
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