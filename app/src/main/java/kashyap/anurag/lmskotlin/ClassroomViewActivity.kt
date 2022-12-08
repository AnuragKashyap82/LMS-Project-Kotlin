package kashyap.anurag.lmskotlin

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Adapters.AdapterClassroomPost
import kashyap.anurag.lmskotlin.Models.ModelClassroomPost
import kashyap.anurag.lmskotlin.databinding.ActivityClassroomViewBinding
import java.util.*

class ClassroomViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClassroomViewBinding
    private var classCode: String? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var classroomPostArrayList: ArrayList<ModelClassroomPost>
    private lateinit var adapterClassroomPost: AdapterClassroomPost

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassroomViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        classCode = intent.getStringExtra("classCode")
        loadClassDetails()

        loadAllClassMessage()

        binding.assBtn.setOnClickListener {
            val intent = Intent(this@ClassroomViewActivity, AllAssignmentActivity::class.java)
            intent.putExtra("classCode", classCode)
            startActivity(intent)
        }
        binding.postBtn.setOnClickListener {
            showBottomSheet()
        }
        binding.attendanceBtn.setOnClickListener {
            val intent = Intent(this@ClassroomViewActivity, AttendanceActivity::class.java)
            intent.putExtra("classCode", "" + classCode)
            startActivity(intent)
        }
//        binding.myAttendanceBtn.setOnClickListener {
//            val intent = Intent(this@ClassroomViewActivity, MyAttendanceActivity::class.java)
//            intent.putExtra("classCode", "" + classCode)
//            startActivity(intent)
//        }
    }

    private fun loadClassDetails() {
        val documentReference = firebaseFirestore.collection("classroom").document(
            classCode!!
        )
        documentReference.addSnapshotListener(
            this
        ) { ds, error ->
            val className = "" + ds!!.getString("className")
            val subjectName = "" + ds.getString("subjectName")
            val uid = "" + ds.getString("uid")
            binding.subjectNameTv.text = subjectName
            if (uid == firebaseAuth.uid) {
                binding.attendanceBtn.visibility = View.VISIBLE
                binding.myAttendanceBtn.visibility = View.GONE
            } else {
                binding.attendanceBtn.visibility = View.GONE
                binding.myAttendanceBtn.visibility = View.VISIBLE
            }
        }
    }

    private fun loadAllClassMessage() {
        classroomPostArrayList = ArrayList()
        FirebaseFirestore.getInstance().collection("classroom").document(classCode!!)
            .collection("classMsg")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val modelClassroomPost: ModelClassroomPost = document.toObject(
                            ModelClassroomPost::class.java
                        )
                        classroomPostArrayList.add(modelClassroomPost)
                    }
//                    Collections.sort(classroomPostArrayList,
//                        Comparator<Any?> { t1, t2 ->
//                            t1.getTimestamp().compareToIgnoreCase(t2.getTimestamp())
//                        })
//                    Collections.reverse(classroomPostArrayList)
                    val layoutManager = LinearLayoutManager(
                        this@ClassroomViewActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    binding.classPostRv.layoutManager = layoutManager
                    binding.classPostRv.layoutManager =
                        LinearLayoutManager(this@ClassroomViewActivity)
                    adapterClassroomPost =
                        AdapterClassroomPost(this@ClassroomViewActivity, classroomPostArrayList)
                    binding.classPostRv.adapter = adapterClassroomPost
                }
            }
    }

    private fun showBottomSheet() {
        val dialog = Dialog(this@ClassroomViewActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bs_class_post_options)
        val postMsgLl = dialog.findViewById<LinearLayout>(R.id.postMsgLl)
        val postVideoLl = dialog.findViewById<LinearLayout>(R.id.postVideoLl)
        val askDoubtLl = dialog.findViewById<LinearLayout>(R.id.askDoubtLl)
        val addAssLl = dialog.findViewById<LinearLayout>(R.id.addAssLl)
        val addAttachmentLl = dialog.findViewById<LinearLayout>(R.id.addAttachmentLl)
        dialog.show()
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.dialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
        postMsgLl.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this@ClassroomViewActivity, PostClassroomMsgActivity::class.java)
            intent.putExtra("classCode", classCode)
            startActivity(intent)
        }
        postVideoLl.setOnClickListener { dialog.dismiss() }
        askDoubtLl.setOnClickListener { dialog.dismiss() }
        addAssLl.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this@ClassroomViewActivity, AddAssignmentActivity::class.java)
            intent.putExtra("classCode", classCode)
            startActivity(intent)
        }
        addAttachmentLl.setOnClickListener { dialog.dismiss() }
    }
}