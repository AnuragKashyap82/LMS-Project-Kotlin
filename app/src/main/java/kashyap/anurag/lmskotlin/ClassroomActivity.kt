package kashyap.anurag.lmskotlin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Adapters.AdapterClassroom
import kashyap.anurag.lmskotlin.Models.ModelClassroom
import kashyap.anurag.lmskotlin.databinding.ActivityClassroomBinding
import java.util.*

class ClassroomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClassroomBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var classroomArrayList: ArrayList<ModelClassroom>
    private lateinit var adapterClassroom: AdapterClassroom

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassroomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        loadAllClassroom()

        binding.createClassBtn.setOnClickListener {
            startActivity(
                Intent(
                    this@ClassroomActivity,
                    CreateClassActivity::class.java
                )
            )
        }
        binding.joinClassBtn.setOnClickListener {
            startActivity(
                Intent(
                    this@ClassroomActivity,
                    JoinClassActivity::class.java
                )
            )
        }
    }

    private fun loadAllClassroom() {
        classroomArrayList = ArrayList()
        FirebaseFirestore.getInstance().collection("Users").document(firebaseAuth.uid!!)
            .collection("classroom")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val modelClassroom = document.toObject(ModelClassroom::class.java)
                        classroomArrayList.add(modelClassroom)
                    }
//                    Collections.sort(
//                        classroomArrayList,
//                        object : Comparator<ModelClassroom?> {
//                            fun compare(t1: ModelClassroom, t2: ModelClassroom): Int {
//                                return t1.classCode.compareToIgnoreCase(t2.classCode)
//                            }
//                        })
//                    Collections.reverse(classroomArrayList)
                    val layoutManager =
                        LinearLayoutManager(
                            this@ClassroomActivity,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                    binding.classroomRv.layoutManager = layoutManager
                    binding.classroomRv.layoutManager = LinearLayoutManager(this@ClassroomActivity)
                    adapterClassroom = AdapterClassroom(this@ClassroomActivity, classroomArrayList)
                    binding.classroomRv.adapter = adapterClassroom
                }
            }
    }
}