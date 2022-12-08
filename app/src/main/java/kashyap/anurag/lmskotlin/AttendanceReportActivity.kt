package kashyap.anurag.lmskotlin

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Adapters.AdapterReportAttendance
import kashyap.anurag.lmskotlin.Models.ModelAttendanceReport
import kashyap.anurag.lmskotlin.databinding.ActivityAttendanceReportBinding
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class AttendanceReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceReportBinding
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var classCode: String? = null
    private lateinit var attendanceReportArrayList: ArrayList<ModelAttendanceReport>
    private lateinit var adapterReportAttendance: AdapterReportAttendance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseFirestore = FirebaseFirestore.getInstance()

        classCode = intent.getStringExtra("classCode")

        loadClassDetails(classCode!!)
        loadCurrentDate(classCode!!)

        binding.dateTv.setOnClickListener { datePickDialog() }
        binding.moreBtn.setOnClickListener {
            val intent = Intent(this@AttendanceReportActivity, MonthlyAttendanceActivity::class.java)
            intent.putExtra("classCode", "" + classCode)
            startActivity(intent)
        }
    }

    private fun loadCurrentDate(classCode: String) {
        val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy")
        val calendar = Calendar.getInstance()
        val date = dateFormat.format(calendar.time)
        binding.dateTv.text = date
        loadAllDates(classCode, date)
    }

    private fun datePickDialog() {
        val calendar = Calendar.getInstance()
        val mYear = calendar[Calendar.YEAR]
        val mMonth = calendar[Calendar.MONTH] + 1
        val mDay = calendar[Calendar.DAY_OF_MONTH]
        val datePickerDialog = DatePickerDialog(this,
            { datePicker, year, monthOfYear, dayOfMonth ->
                val mFormat = DecimalFormat("00")
                val pDay = mFormat.format(dayOfMonth.toLong())
                val pMonth = mFormat.format((monthOfYear + 1).toLong())
                val pYear = "" + year
                val pDate = "$pDay-$pMonth-$pYear"
                binding.dateTv.text = pDate
                loadAllDates(classCode!!, pDate)
            }, mYear, mMonth, mDay
        )
        datePickerDialog.show()
        datePickerDialog.datePicker
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

    private fun loadAllDates(classCode: String, pDate: String) {
        binding.progressBar.visibility = View.VISIBLE
        attendanceReportArrayList = ArrayList()
        FirebaseFirestore.getInstance().collection("classroom").document(classCode)
            .collection("Attendance").document("Date").collection("" + pDate)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val modelAttendanceReport = document.toObject(
                            ModelAttendanceReport::class.java
                        )
                        attendanceReportArrayList.add(modelAttendanceReport)
                    }
                    val layoutManager = LinearLayoutManager(
                        this@AttendanceReportActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    binding.dateRv.layoutManager = layoutManager
                    binding.dateRv.layoutManager =
                        LinearLayoutManager(this@AttendanceReportActivity)
                    adapterReportAttendance =
                        AdapterReportAttendance(
                            this@AttendanceReportActivity,
                            attendanceReportArrayList
                        )
                    binding.dateRv.adapter = adapterReportAttendance
                    binding.progressBar.visibility = View.GONE
                }else{

                }
            }
    }
}