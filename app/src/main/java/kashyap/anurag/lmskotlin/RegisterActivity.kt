package kashyap.anurag.lmskotlin

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var uniqueId: String? = ""
    private var userType: String? = ""
    private var phoneNumber: String? = ""
    private var fullName: String? = ""
    private var email: String? = ""
    private var password: String? = ""
    private var confirmPassword: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        uniqueId = intent.getStringExtra("uniqueId")
        phoneNumber = intent.getStringExtra("phoneNumber")
        userType = intent.getStringExtra("userType")

        binding.registerBtn.setOnClickListener {
            validateData()
        }

    }

    private fun validateData() {
        fullName = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        confirmPassword = binding.cPasswordEt.text.toString().trim()

        if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Enter Name....", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email....", Toast.LENGTH_SHORT).show()
            return
        }
        if (password!!.length < 6) {
            Toast.makeText(
                this,
                "Password must be atleast 6 character long....",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Password doesn't match....", Toast.LENGTH_SHORT).show()
            return
        }
        createUserAccount()
    }

    private fun createUserAccount() {
        binding.progressBar.visibility = View.VISIBLE

        firebaseAuth.createUserWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val user = firebaseAuth.currentUser
                    user!!.sendEmailVerification().addOnSuccessListener {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Verification Email has been sent.Please verify",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                        .addOnFailureListener {
                            Log.d(
                                ContentValues.TAG,
                                "onFailure: Email not sent"
                            )
                        }
                }
            }
            .addOnSuccessListener {
                saveFirebaseData();
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@RegisterActivity, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveFirebaseData() {
        binding.progressBar.visibility = View.VISIBLE
        val timestamp = "" + System.currentTimeMillis()

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = firebaseAuth.uid!!
        hashMap["email"] = email!!
        hashMap["name"] = fullName!!
        hashMap["uniqueId"] = uniqueId!!
        hashMap["phone"] = phoneNumber!!
        hashMap["userType"] = userType!!
        hashMap["timestamp"] = timestamp
        hashMap["online"] = "true"
        hashMap["profileImage"] = ""

        val documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.uid!!)
        documentReference.set(hashMap)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                addUniqueIdToRegisteredUniqueId(uniqueId!!)
                //                            startActivity(new Intent(RegisterActivity.this, MainUsersActivity.class));
                startActivity(Intent(this@RegisterActivity, VerifyEmailActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@RegisterActivity, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun addUniqueIdToRegisteredUniqueId(uniqueId: String) {
        val hashMap = java.util.HashMap<String, Any>()
        hashMap["uid"] = "" + firebaseAuth.uid
        hashMap["email"] = "" + email
        hashMap["name"] = "" + fullName
        hashMap["uniqueId"] = "" + uniqueId
        hashMap["phone"] = "" + phoneNumber
        hashMap["userType"] = "" + userType

        val documentReference =
            firebaseFirestore.collection("RegisteredUniqueId").document(uniqueId)
        documentReference
            .set(hashMap)
            .addOnSuccessListener {

            }
            .addOnFailureListener {

            }
    }
}