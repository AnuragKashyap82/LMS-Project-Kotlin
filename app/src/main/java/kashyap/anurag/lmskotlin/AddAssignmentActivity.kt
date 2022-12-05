package kashyap.anurag.lmskotlin

import android.app.DatePickerDialog
import android.content.DialogInterface
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kashyap.anurag.lmskotlin.databinding.ActivityAddAssignmentBinding
import java.text.DecimalFormat
import java.util.*

class AddAssignmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAssignmentBinding
    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseFirestore: FirebaseFirestore? = null

    private var pdfUri: Uri? = null
    private var classCode: String? = null
    private var assignmentName: String? = null
    private var dueDate: String? = null
    private var fullMarks: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAssignmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        classCode = intent.getStringExtra("classCode")

        binding.backBtn.setOnClickListener { onBackPressed() }

        binding.pickPdfBtn.setOnClickListener { pdfPickIntent() }
        binding.uploadAssBtn.setOnClickListener { validateData() }
        binding.fullMarksTv.setOnClickListener {
            val builder = AlertDialog.Builder(this@AddAssignmentActivity)
            builder.setTitle("Select Full Marks:")
                .setItems(Constants.marksCategories,
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        val selectedMarks: String = Constants.marksCategories.get(i)
                        binding.fullMarksTv.text = selectedMarks
                    }).show()
        }
        binding.dueDateTv.setOnClickListener { datePickDialog() }

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

    private fun validateData() {
        assignmentName = binding.assNameEt.text.toString().trim()
        fullMarks = binding.fullMarksTv.text.toString().trim()
        dueDate = binding.dueDateTv.text.toString().trim()
        if (TextUtils.isEmpty(assignmentName)) {
            Toast.makeText(this, "Enter Assignment Name....!", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(fullMarks)) {
            Toast.makeText(this, "Select Full Marks....!", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(dueDate)) {
            Toast.makeText(this, "Select Due Date....!", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null) {
            Toast.makeText(this, "Pick Result Pdf", Toast.LENGTH_SHORT).show()
        } else {
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
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

                uploadAssignmentInfoToDb(uploadedAssignmentUrl, timestamp)
            }
            .addOnFailureListener{
                binding.progressBar.visibility = View.VISIBLE
                Toast.makeText(this, "Assignment pdf upload failed due to", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadAssignmentInfoToDb(uploadedAssignmentUrl: String, timestamp: Long) {
        binding.progressBar.visibility = View.VISIBLE
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["assignmentId"] = "" + timestamp
        hashMap["classCode"] = "" + classCode
        hashMap["assignmentName"] = "" + assignmentName
        hashMap["fullMarks"] = "" + fullMarks
        hashMap["dueDate"] = "" + dueDate
        hashMap["timestamp"] = "" + timestamp
        hashMap["uid"] = "" + firebaseAuth!!.uid
        hashMap["url"] = "" + uploadedAssignmentUrl
        val documentReference = firebaseFirestore!!.collection("classroom").document(
            classCode!!
        ).collection("assignment").document("" + timestamp)
        documentReference.set(hashMap)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                clearText()

                Toast.makeText(
                    this@AddAssignmentActivity,
                    "Assignment Successfully uploaded....",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@AddAssignmentActivity,
                    " Failed to upload to db due to" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun clearText() {
        binding.assNameEt.setText("")
        binding.dueDateTv.text = ""
        binding.fullMarksTv.text = ""
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