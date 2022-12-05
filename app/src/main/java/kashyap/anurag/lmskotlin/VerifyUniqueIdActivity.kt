package kashyap.anurag.lmskotlin

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.databinding.ActivityVerifyUniqueIdBinding

class VerifyUniqueIdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyUniqueIdBinding
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private var userType: String? = ""
    private var uniqueId: String? = ""
    private var isUniqueId = false
    private var isRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyUniqueIdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        binding.phoneEt.isEnabled = false
        binding.sendOtpBtn.isEnabled = false

        binding.helpBtn.setOnClickListener {
            startActivity(Intent(this, NeedHelpActivity::class.java))
        }
        binding.sendOtpBtn.setOnClickListener {
            startActivity(Intent(this, OTPActivity::class.java))
        }
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.continueBtn.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        uniqueId = binding.uniqueIdEt.text.toString().trim()
        if (TextUtils.isEmpty(uniqueId)) {
            Toast.makeText(this, "Enter Your Unique Id....!", Toast.LENGTH_SHORT).show()
        } else {
            checkExistingUniqueId()
        }
    }

    private fun checkExistingUniqueId() {
        binding.progressBar.visibility = View.VISIBLE

        val documentReference =
            firebaseFirestore.collection("RegisteredUniqueId").document(uniqueId!!)
        documentReference.addSnapshotListener { value, error ->
            isRegistered = value!!.exists()
            if (isRegistered) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@VerifyUniqueIdActivity, "This Unique_Id is already Registered...!!", Toast.LENGTH_SHORT).show()
            } else {
                verifyUniqueId()
            }
        }
    }

    private fun verifyUniqueId() {
        binding.progressBar.visibility = View.VISIBLE
        val documentReference = firebaseFirestore.collection("UniqueId").document(
            uniqueId!!
        )
        documentReference.addSnapshotListener { snapshot, error ->
            isUniqueId = snapshot!!.exists()
            if (isUniqueId) {
                binding.progressBar.visibility = View.GONE
                verifyPhoneNumber()
                binding.sendOtpBtn.isEnabled = true
                Toast.makeText(this@VerifyUniqueIdActivity, "Unique-Id Verified....!", Toast.LENGTH_SHORT).show()
            } else {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@VerifyUniqueIdActivity, "Unique-Id Not Found....!", Toast.LENGTH_SHORT).show()
            }
            val phone = "" + snapshot.getString("phone")
            userType = "" + snapshot.getString("userType")
            binding.phoneEt.setText(phone)
        }
    }

    private fun verifyPhoneNumber() {
        binding.sendOtpBtn.setOnClickListener { validatePhoneNumber() }
    }

    private var phoneNumber: String? = null
    private fun validatePhoneNumber() {
        phoneNumber = binding.phoneEt.text.toString().trim()
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Enter Your Phone Number....", Toast.LENGTH_SHORT).show()
            return
        } else {
            val intent = Intent(this@VerifyUniqueIdActivity, OTPActivity::class.java)
            intent.putExtra("phoneNumber", binding.phoneEt.text.toString())
            intent.putExtra("uniqueId", binding.uniqueIdEt.text.toString())
            intent.putExtra("userType", userType)
            startActivity(intent)
        }
    }
}