package kashyap.anurag.lmskotlin

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kashyap.anurag.lmskotlin.databinding.ActivityVerifyEmailBinding
import kashyap.anurag.lmskotlin.databinding.ActivityVerifyUniqueIdBinding

class VerifyEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyEmailBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var  firebaseAuth: FirebaseAuth
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Please wait")
        progressDialog!!.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadMyInfo()

        binding.sendMailBtn.setOnClickListener {
            val user = firebaseAuth.currentUser
            user!!.sendEmailVerification().addOnSuccessListener {
                Toast.makeText(
                    this@VerifyEmailActivity,
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

        binding.logoutBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this@VerifyEmailActivity)
            builder.setTitle("Delete")
                .setMessage("Are you sure want to Logout")
                .setPositiveButton(
                    "Yes"
                ) { dialogInterface, i ->
                    makeMeOffline() }
                .setNegativeButton(
                    "No"
                ) { dialogInterface, i -> dialogInterface.dismiss() }
                .show()
        }

    }

    private fun loadMyInfo() {
        val documentReference: DocumentReference =
            firestore.collection("Users").document(firebaseAuth.uid!!)
        documentReference.addSnapshotListener(
            this
        ) { ds, error ->
            val email = "" + ds!!.getString("email")
            val name = "" + ds.getString("name")
            val profileImage = "" + ds.getString("profileImage")
            binding.nameTv.text = name
            binding.emailTv.text = email
            try {
                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray)
                    .into(binding.profileIv)
            } catch (e: Exception) {
                binding.profileIv.setImageResource(R.drawable.ic_person_gray)
            }
        }
    }

    private fun makeMeOffline() {
        progressDialog!!.setMessage("Logging out...")
        progressDialog!!.show()
        val hashMap = HashMap<String, Any>()
        hashMap["online"] = "false"
        val documentReference: DocumentReference =
            firestore.collection("Users").document(firebaseAuth.uid!!)
        documentReference.update(hashMap)
            .addOnSuccessListener {
                progressDialog!!.setMessage("Logging Out...!")
                progressDialog!!.show()
                firebaseAuth.signOut()
                startActivity(Intent(this@VerifyEmailActivity, LoginActivity::class.java))
                finish()
                progressDialog!!.dismiss()
            }
            .addOnFailureListener { e ->
                progressDialog!!.dismiss()
                Toast.makeText(this@VerifyEmailActivity, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }
}