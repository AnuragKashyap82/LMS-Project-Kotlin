package kashyap.anurag.lmskotlin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Adapters.AdapterAttenStud
import kashyap.anurag.lmskotlin.Constants.Companion.joiningCode
import kashyap.anurag.lmskotlin.Models.ModelAtteStud
import kashyap.anurag.lmskotlin.databinding.ActivityAttendanceBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceBinding
    private var currentDate: String? = null
    private var name: String? = null
    private val isCompleted: Boolean? = null
    private lateinit var atteStudArrayList: ArrayList<ModelAtteStud>
    private lateinit var adapterAttenStud: AdapterAttenStud


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        joiningCode = intent.getStringExtra("classCode").toString()

        loadClassDetails()

        val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy")
        val calendar = Calendar.getInstance()
        currentDate = dateFormat.format(calendar.time)

        checkAttendanceCreated()

        binding.reportBtn.setOnClickListener {
            val intent = Intent(this@AttendanceActivity, AttendanceReportActivity::class.java)
            intent.putExtra("classCode", "" + joiningCode)
            startActivity(intent)
        }
        binding.backBtn.setOnClickListener { }
        binding.createAttendanceBtn.setOnClickListener {
            showCreateAttendanceDialog()
        }
    }
    override fun onStart() {
        super.onStart()
        checkAttendanceCreated()
    }

    private fun checkAttendanceCreated() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("classroom")
            .child(joiningCode!!)
        databaseReference.child("classes").child("" + currentDate)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        binding.createAttendanceBtn.visibility = View.GONE
                        loadAllStudents(joiningCode)
                    } else {
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadClassDetails() {
        val documentReference = FirebaseFirestore.getInstance().collection("classroom")
            .document(joiningCode!!)
        documentReference.addSnapshotListener(
            this
        ) { ds, error ->
            val className = "" + ds!!.getString("className")
            val subjectName = "" + ds.getString("subjectName")
            val uid = "" + ds.getString("uid")
            binding.subjectNameTv.text = subjectName
            binding.classNameTv.text = className
            loadTeacherDetails(uid)
        }
    }
    private fun loadTeacherDetails(uid: String) {
        val documentReference = FirebaseFirestore.getInstance().collection("Users").document(uid)
        documentReference.addSnapshotListener { snapshot, error ->
            if (snapshot!!.exists()) {
                name = snapshot["name"].toString()
            }
        }
    }

    private fun loadAllStudents(joiningCode: String) {
        binding.progressBar.visibility = View.VISIBLE
        atteStudArrayList = ArrayList()
        val reference = FirebaseDatabase.getInstance().getReference("classroom").child(joiningCode)
            .child("Students")
        reference
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    atteStudArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(ModelAtteStud::class.java)
                        atteStudArrayList.add(model!!)
                        val noOfStudents = atteStudArrayList.size
                        binding.noOfStudentsTv.text = "Total No. of Students: $noOfStudents"
                    }
                    val layoutManager = LinearLayoutManager(
                        this@AttendanceActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    binding.studentsRv.layoutManager = layoutManager
                    binding.studentsRv.layoutManager = LinearLayoutManager(this@AttendanceActivity)
                    adapterAttenStud = AdapterAttenStud(this@AttendanceActivity, atteStudArrayList)
                    binding.studentsRv.adapter = adapterAttenStud
                    binding.progressBar.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showCreateAttendanceDialog() {
        val myDialog = AlertDialog.Builder(this, R.style.BottomSheetStyle)
        val inflater = LayoutInflater.from(this)
        val view1: View = inflater.inflate(R.layout.teacher_attendance_dialog, null)
        myDialog.setView(view1)
        val dialog = myDialog.create()
        dialog.setCancelable(true)
        val createClassBtn = view1.findViewById<Button>(R.id.createClassBtn)
        val dateTv = view1.findViewById<TextView>(R.id.dateTv)
        val teacherNameTv = view1.findViewById<TextView>(R.id.teacherNameTv)
        dateTv.text = currentDate
        teacherNameTv.text = name
        createClassBtn.setOnClickListener {
            dialog.dismiss()
            binding.progressBar.visibility = View.VISIBLE
            createTodayAttendance()
        }
        dialog.show()
    }

    private fun createTodayAttendance() {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val currentYear = calendar[Calendar.YEAR]
        val currentMonth = calendar[Calendar.MONTH] + 1
        val currentDay = calendar[Calendar.DAY_OF_MONTH]
        val monthYear = "" + currentMonth + currentYear
        val hashMap = HashMap<String, Any>()
        hashMap["date"] = "" + currentDate
        val databaseReference = FirebaseDatabase.getInstance().getReference("classroom")
        databaseReference.child(joiningCode!!).child("classes")
            .child("" + currentDate).setValue(hashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.progressBar.visibility = View.GONE
                    increaseNoOfClasses(joiningCode!!)
                    val databaseReference = FirebaseDatabase.getInstance().getReference("classroom")
                    databaseReference.child(joiningCode!!).child("noOfClass")
                        .child("" + monthYear)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    var noOfClass = "" + snapshot.child("noOfClass").value
                                    if (noOfClass == "" || noOfClass == "null") {
                                        noOfClass = "0"
                                    }
                                    val newNoClassCount = noOfClass.toLong() + 1
                                    val hashMap = HashMap<String, Any>()
                                    hashMap["noOfClass"] = newNoClassCount
                                    val reference =
                                        FirebaseDatabase.getInstance().getReference("classroom")
                                    reference.child(joiningCode!!)
                                        .child("noOfClass")
                                        .child("" + monthYear).updateChildren(hashMap)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                            } else {
                                            }
                                        }
                                        .addOnFailureListener { }
                                } else {
                                    val newNoClassCount: Long = 1
                                    val hashMap = HashMap<String, Any>()
                                    hashMap["noOfClass"] = newNoClassCount
                                    val reference =
                                        FirebaseDatabase.getInstance().getReference("classroom")
                                    reference.child(joiningCode!!)
                                        .child("noOfClass")
                                        .child("" + monthYear).updateChildren(hashMap)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                            } else {
                                            }
                                        }
                                        .addOnFailureListener { }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                } else {
                    binding.progressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener { binding.progressBar.visibility = View.GONE }
    }

    private fun increaseNoOfClasses(joiningCode: String) {}
}