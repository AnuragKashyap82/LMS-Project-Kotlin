package kashyap.anurag.lmskotlin

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kashyap.anurag.lmskotlin.databinding.ActivityEditTimeTableBinding
import java.util.*

class EditTimeTableActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTimeTableBinding
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private var subName: String? = ""
    private var teacherName: String? = ""
    private var endTime: String? = ""
    private var startTime: String? = ""
    private var day: String? = ""
    private var ongoingTopic: String? = ""
    private var percentSylComp: String? = ""

    private var hour = 0
    private var minute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTimeTableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        day = intent.getStringExtra("day")
        subName = intent.getStringExtra("subName")

        loadClassDetails()

        binding.updateTimeTableBtn.setOnClickListener { validateData() }
        binding.startTimeTv.setOnClickListener { openStartTimePicker() }
        binding.endTimeTv.setOnClickListener { openEndTimePicker() }
    }

    private fun openStartTimePicker() {
        val onTimeSetListener =
            OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                hour = selectedHour
                minute = selectedMinute
                binding.startTimeTv.text =
                    String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            }
        val timePickerDialog =
            TimePickerDialog(this,  /*style,*/onTimeSetListener, hour, minute, true)
        timePickerDialog.setTitle("Select Time")
        timePickerDialog.show()
    }

    private fun openEndTimePicker() {
        val onTimeSetListener =
            OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                hour = selectedHour
                minute = selectedMinute
                binding.endTimeTv.text =
                    String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            }
        val timePickerDialog =
            TimePickerDialog(this,  /*style,*/onTimeSetListener, hour, minute, true)
        timePickerDialog.setTitle("Select Time")
        timePickerDialog.show()
    }

    private fun loadClassDetails() {
        val documentReference = firebaseFirestore.collection("timeTable").document(firebaseAuth.uid!!).collection(day!!).document(subName!!)
        documentReference.addSnapshotListener(this) { ds, error ->
               if (ds!!.exists()){
                   val subName = "" + ds.getString("subName")
                   val startTime = "" + ds.getString("startTime")
                   val teacherName = "" + ds.getString("teacherName")
                   val endTime = "" + ds.getString("endTime")
                   val day = "" + ds.getString("day")
                   val ongoingTopic = "" + ds.getString("ongoingTopic")
                   val percentSylComp = "" + ds.getString("percentSylComp")

                   binding.subjectEt.setText(subName)
                   binding.classTeacherNameEt.setText(teacherName)
                   binding.startTimeTv.setText(startTime)
                   binding.endTimeTv.setText(endTime)
                   binding.dayTv.setText(day)
                   binding.ongoingTopicEt.setText(ongoingTopic)
                   binding.syllabusCompletedEt.setText(percentSylComp)
               }


        }
    }

    private fun validateData() {
        subName = binding.subjectEt.text.toString().trim()
        teacherName = binding.classTeacherNameEt.text.toString().trim()
        startTime = binding.startTimeTv.text.toString().trim()
        endTime = binding.endTimeTv.text.toString().trim()
        day = binding.dayTv.text.toString().trim()
        ongoingTopic = binding.ongoingTopicEt.text.toString().trim()
        percentSylComp = binding.syllabusCompletedEt.text.toString().trim()
        if (TextUtils.isEmpty(subName)) {
            Toast.makeText(
                this@EditTimeTableActivity,
                "Enter Subject Name..!!!!",
                Toast.LENGTH_SHORT
            ).show()
        } else if (TextUtils.isEmpty(teacherName)) {
            Toast.makeText(
                this@EditTimeTableActivity,
                "Enter Teacher Name..!!!!",
                Toast.LENGTH_SHORT
            ).show()
        } else if (TextUtils.isEmpty(startTime)) {
            Toast.makeText(this@EditTimeTableActivity, "Pick start time..!!!!", Toast.LENGTH_SHORT)
                .show()
        } else if (TextUtils.isEmpty(endTime)) {
            Toast.makeText(this@EditTimeTableActivity, "Pick End Time..!!!!", Toast.LENGTH_SHORT)
                .show()
        } else if (TextUtils.isEmpty(day)) {
            Toast.makeText(this@EditTimeTableActivity, "Pick End Time..!!!!", Toast.LENGTH_SHORT)
                .show()
        } else if (TextUtils.isEmpty(ongoingTopic)) {
            Toast.makeText(
                this@EditTimeTableActivity,
                "Enter OnGoing Topic..!!!!",
                Toast.LENGTH_SHORT
            ).show()
        } else if (TextUtils.isEmpty(percentSylComp)) {
            Toast.makeText(
                this@EditTimeTableActivity,
                "Enter % Syllabus Completed..!!!!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            updateTimeTable()
        }
    }

    private fun updateTimeTable() {
        binding.progressBar.visibility = View.VISIBLE
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["subName"] = "" + subName
        hashMap["teacherName"] = "" + teacherName
        hashMap["startTime"] = "" + startTime
        hashMap["endTime"] = "" + endTime
        hashMap["day"] = "" + day
        hashMap["ongoingTopic"] = "" + ongoingTopic
        hashMap["percentSylComp"] = "" + percentSylComp
        val documentReference = firebaseFirestore.collection("timeTable").document(
            firebaseAuth.uid!!
        ).collection(day!!).document(subName!!)
        documentReference.update(hashMap)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@EditTimeTableActivity,
                    "Time Table Updated....",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@EditTimeTableActivity,
                    " Failed to update to db due to" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}