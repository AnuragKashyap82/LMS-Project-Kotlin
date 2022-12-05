package kashyap.anurag.lmskotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Adapters.AdapterSubmittedAss
import kashyap.anurag.lmskotlin.Models.ModelSubmittedAss
import kashyap.anurag.lmskotlin.databinding.ActivitySubmittedAssignmentBinding
import java.util.*

class SubmittedAssignmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubmittedAssignmentBinding

    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    private var assignmentId: String? = null
    private var classCode: String? = null

    private lateinit var submittedAssArrayList: ArrayList<ModelSubmittedAss>
    private lateinit var adapterSubmittedAss: AdapterSubmittedAss

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubmittedAssignmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        assignmentId = intent.getStringExtra("assignmentId")
        classCode = intent.getStringExtra("classCode")

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        loadAllSubmittedAss(classCode!!, assignmentId!!)
        countNoOfStudentSubmitted(classCode!!, assignmentId!!)
    }


    private fun countNoOfStudentSubmitted(classCode: String, assignmentId: String) {
        val documentReference =
            firebaseFirestore.collection("classroom").document(classCode).collection("assignment")
                .document(assignmentId).collection("Submission").document(
                    firebaseAuth.uid!!
                )
        documentReference.addSnapshotListener { value, error ->
            //                long noOfStudent = value.();
//                float noOfStudents = noOfStudent / 1;
//
//                binding.noOfStudentsTv.setText(String.format("%.0f", noOfStudents));
        }
    }

    private fun loadAllSubmittedAss(classCode: String, assignmentId: String) {
        submittedAssArrayList = ArrayList()
        FirebaseFirestore.getInstance().collection("classroom").document(classCode)
            .collection("assignment").document(assignmentId).collection("Submission")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val modelSubmittedAss = document.toObject(ModelSubmittedAss::class.java)
                        submittedAssArrayList.add(modelSubmittedAss)
                    }
                    val layoutManager = LinearLayoutManager(
                        this@SubmittedAssignmentActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    binding.submittedAssignmentRv.layoutManager = layoutManager
                    binding.submittedAssignmentRv.layoutManager =
                        LinearLayoutManager(this@SubmittedAssignmentActivity)
                    adapterSubmittedAss =
                        AdapterSubmittedAss(this@SubmittedAssignmentActivity, submittedAssArrayList)
                    binding.submittedAssignmentRv.adapter = adapterSubmittedAss
                }
            }
    }
}