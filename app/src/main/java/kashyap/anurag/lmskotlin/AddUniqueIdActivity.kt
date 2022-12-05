package kashyap.anurag.lmskotlin

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.databinding.ActivityAddUniqueIdBinding

class AddUniqueIdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddUniqueIdBinding
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var uniqueId: String? = null
    private var phone: String? = null
    private var userType: String? = null
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddUniqueIdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Please wait")
        progressDialog!!.setCanceledOnTouchOutside(false)

        firebaseFirestore = FirebaseFirestore.getInstance()

        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.userTv.setOnClickListener(View.OnClickListener {
            val builder = AlertDialog.Builder(this@AddUniqueIdActivity)
            builder.setTitle("Select User:")
                .setItems(Constants.userType,
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        val selectedUser: String = Constants.userType.get(i)
                        binding.userTv.setText(selectedUser)
                    }).show()
        })

        binding.addUniqueIdBtn.setOnClickListener { validateData() }
    }

    private fun validateData() {
        uniqueId = binding.uniqueIdEt.text.toString().trim()
        phone = binding.phoneEt.text.toString().trim()
        userType = binding.userTv.text.toString().trim()
        if (TextUtils.isEmpty(uniqueId)) {
            Toast.makeText(this, "Enter Unique Id Of the Student....!", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Enter Phone No. Of the Student....!", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(userType)) {
            Toast.makeText(this, "Select the User Type....!", Toast.LENGTH_SHORT).show()
        } else {
            addUniqueId()
        }
    }

    private fun addUniqueId() {
        progressDialog!!.setMessage("Uploading Unique_Id")
        progressDialog!!.show()
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uniqueId"] = "" + uniqueId
        hashMap["phone"] = "" + phone
        hashMap["userType"] = "" + userType
        val documentReference = firebaseFirestore.collection("UniqueId").document(
            uniqueId!!
        )
        documentReference
            .set(hashMap)
            .addOnSuccessListener {
                progressDialog!!.dismiss()
                clearText()
                Toast.makeText(
                    this@AddUniqueIdActivity,
                    "Unique_Id Successfully Uploaded....",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                progressDialog!!.dismiss()
                Toast.makeText(
                    this@AddUniqueIdActivity,
                    " Failed to upload to db due to" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun clearText() {
        binding.uniqueIdEt.setText("")
        binding.phoneEt.setText("")
        binding.userTv.text = ""
    }
}