package kashyap.anurag.lmskotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Adapters.AdapterAssignment
import kashyap.anurag.lmskotlin.Models.ModelAssignment
import kashyap.anurag.lmskotlin.databinding.ActivityAllAssignmentBinding
import java.util.*
import kotlin.collections.ArrayList

class AllAssignmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllAssignmentBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var classCode: String? = null
    private lateinit var assignmentList: ArrayList<ModelAssignment>
    private lateinit var adapterAssignment: AdapterAssignment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllAssignmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        classCode = intent.getStringExtra("classCode")

        loadAllAssignment()

    }

    private fun loadAllAssignment() {
        assignmentList = ArrayList()
        FirebaseFirestore.getInstance().collection("classroom").document(classCode!!)
            .collection("assignment")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val modelAssignment = document.toObject(ModelAssignment::class.java)
                        assignmentList.add(modelAssignment)
                    }
//                    Collections.sort(
//                        assignmentList,
//                        object : Comparator<ModelAssignment?> {
//                            fun compare(t1: ModelAssignment, t2: ModelAssignment): Int {
//                                return t1.getTimestamp().compareToIgnoreCase(t2.getTimestamp())
//                            }
//                        })
//                    Collections.reverse(assignmentList)
                    val layoutManager = LinearLayoutManager(
                        this@AllAssignmentActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    binding.assRv.layoutManager = layoutManager
                    binding.assRv.layoutManager = LinearLayoutManager(this@AllAssignmentActivity)
                    adapterAssignment =
                        AdapterAssignment(this@AllAssignmentActivity, assignmentList)
                    binding.assRv.adapter = adapterAssignment
                }
            }
    }
}