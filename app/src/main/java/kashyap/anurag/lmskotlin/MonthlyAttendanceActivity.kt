package kashyap.anurag.lmskotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Adapters.AdapterAssignment
import kashyap.anurag.lmskotlin.Adapters.AdapterOverAllAttendance
import kashyap.anurag.lmskotlin.Models.ModelAssignment
import kashyap.anurag.lmskotlin.Models.ModelAtteStud
import kashyap.anurag.lmskotlin.databinding.ActivityMonthlyAttendanceBinding
import java.util.*

class MonthlyAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMonthlyAttendanceBinding
    private var classCode: String? = null
    private lateinit var atteStudArrayList: ArrayList<ModelAtteStud>
    private lateinit var adapterOverAllAttendance: AdapterOverAllAttendance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonthlyAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val calendar = Calendar.getInstance(TimeZone.getDefault())

        val currentYear = calendar[Calendar.YEAR]
        val currentMonth = calendar[Calendar.MONTH] + 1
        val monthYear = "" + currentMonth + currentYear

        binding.monthTv.setText("" + currentMonth)
        binding.yearTv.setText("" + currentYear)

        classCode = intent.getStringExtra("classCode")
        loadClassDetails(classCode!!)
        loadAllStudents(classCode!!, monthYear)
        loadTotalNoClass(classCode!!)
    }

    private fun loadTotalNoClass(classCode: String) {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val currentYear = calendar[Calendar.YEAR]
        val currentMonth = calendar[Calendar.MONTH] + 1
        val monthYear = "" + currentMonth + currentYear
        val databaseReference = FirebaseDatabase.getInstance().getReference("classroom")
        databaseReference.child(classCode).child("noOfClass").child("" + monthYear)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val totalNoClass = snapshot.child("noOfClass").value.toString()
                    binding.totalClassCountTv.text = totalNoClass
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadClassDetails(classCode: String) {
        val documentReference =
            FirebaseFirestore.getInstance().collection("classroom").document(classCode)
        documentReference.addSnapshotListener(
            this
        ) { ds, error ->
            val className = "" + ds!!.getString("className")
            val subjectName = "" + ds.getString("subjectName")
            binding.subjectNameTv.text = subjectName
            binding.classNameTv.text = className
        }
    }

    private fun loadAllStudents(classCode: String, monthYear: String) {
        atteStudArrayList = ArrayList<ModelAtteStud>()
        val reference = FirebaseDatabase.getInstance().getReference("Classroom").child(classCode)
            .child("Students").child(monthYear)
        reference
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    atteStudArrayList.clear()
                    for (ds in snapshot.children) {
                        val model: ModelAtteStud? = ds.getValue(ModelAtteStud::class.java)
                        atteStudArrayList.add(model!!)
                    }
                    val layoutManager = LinearLayoutManager(
                        this@MonthlyAttendanceActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    binding.studentsRv.layoutManager = layoutManager
                    binding.studentsRv.layoutManager =
                        LinearLayoutManager(this@MonthlyAttendanceActivity)
                    adapterOverAllAttendance =
                        AdapterOverAllAttendance(this@MonthlyAttendanceActivity, atteStudArrayList)
                    binding.studentsRv.adapter = adapterOverAllAttendance
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}