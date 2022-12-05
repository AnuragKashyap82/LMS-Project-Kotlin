package kashyap.anurag.lmskotlin

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.databinding.ActivityJoinClassBinding

class JoinClassActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJoinClassBinding
    private var joinCode: String? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinClassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        binding.joinClassBtn.setOnClickListener { validateData() }
    }

    private fun validateData() {
        joinCode = binding.joiningCodeEt.text.toString().trim()
        if (TextUtils.isEmpty(joinCode)) {
            Toast.makeText(
                this@JoinClassActivity,
                "Enter Class Code.....!!!!!!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            checkClassCode(joinCode!!)
        }
    }

    private fun checkClassCode(joinCode: String) {
        binding.progressBar.visibility = View.VISIBLE
        val documentReference = firebaseFirestore.collection("classroom").document(joinCode)
        documentReference.addSnapshotListener(
            this
        ) { documentSnapshot, error ->
            if (documentSnapshot!!.exists()) {
                checkAlreadyJoined(joinCode)
            } else {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@JoinClassActivity,
                    "This Class Code does not exist...!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun checkAlreadyJoined(joinCode: String) {
        binding.progressBar.visibility = View.VISIBLE
        val documentReference = firebaseFirestore.collection("Users").document(
            firebaseAuth.uid!!
        ).collection("classroom").document(joinCode)
        documentReference.addSnapshotListener(
            this
        ) { documentSnapshot, error ->
            if (documentSnapshot!!.exists()) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@JoinClassActivity,
                    "You have already joined this Class....!!!!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                joinClass(joinCode)
            }
        }
    }
    private fun joinClass(joiningCode: String) {
        binding.progressBar.visibility = View.VISIBLE
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["classCode"] = "" + joiningCode
        val documentReference = firebaseFirestore.collection("Users").document(
            firebaseAuth.uid!!
        ).collection("classroom").document(joiningCode)
        documentReference.set(hashMap)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                clearText()
                addToClassStudent(joiningCode)
                Toast.makeText(
                    this@JoinClassActivity,
                    "Class Joined Successfully....",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                clearText()
                Toast.makeText(
                    this@JoinClassActivity,
                    " Failed to Create Class to db due to" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun addToClassStudent(joiningCode: String) {
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "" + firebaseAuth.uid
        val databaseReference = FirebaseDatabase.getInstance().getReference("classroom")
        databaseReference.child(joiningCode).child("Students").child(firebaseAuth.uid!!)
            .setValue(hashMap)
    }

    private fun clearText() {
        binding.joiningCodeEt.setText("")
    }
}