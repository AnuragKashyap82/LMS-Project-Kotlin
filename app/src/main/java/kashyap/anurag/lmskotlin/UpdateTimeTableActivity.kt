package kashyap.anurag.lmskotlin

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.databinding.ActivityUpdateTimeTableBinding
import java.util.*

class UpdateTimeTableActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateTimeTableBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
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
        binding = ActivityUpdateTimeTableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        binding.dayTv.text = intent.getStringExtra("text")

        binding.assClassBtn.setOnClickListener { validateData() }
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
                this@UpdateTimeTableActivity,
                "Enter Subject Name..!!!!",
                Toast.LENGTH_SHORT
            ).show()
        } else if (TextUtils.isEmpty(teacherName)) {
            Toast.makeText(
                this@UpdateTimeTableActivity,
                "Enter Teacher Name..!!!!",
                Toast.LENGTH_SHORT
            ).show()
        } else if (TextUtils.isEmpty(startTime)) {
            Toast.makeText(
                this@UpdateTimeTableActivity,
                "Pick start time..!!!!",
                Toast.LENGTH_SHORT
            ).show()
        } else if (TextUtils.isEmpty(endTime)) {
            Toast.makeText(this@UpdateTimeTableActivity, "Pick End Time..!!!!", Toast.LENGTH_SHORT)
                .show()
        } else if (TextUtils.isEmpty(day)) {
            Toast.makeText(this@UpdateTimeTableActivity, "Pick End Time..!!!!", Toast.LENGTH_SHORT)
                .show()
        } else if (TextUtils.isEmpty(ongoingTopic)) {
            Toast.makeText(
                this@UpdateTimeTableActivity,
                "Enter OnGoing Topic..!!!!",
                Toast.LENGTH_SHORT
            ).show()
        } else if (TextUtils.isEmpty(percentSylComp)) {
            Toast.makeText(
                this@UpdateTimeTableActivity,
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
        documentReference.set(hashMap)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@UpdateTimeTableActivity,
                    "Time Table Updated....",
                    Toast.LENGTH_SHORT
                )
                    .show()
                clearData()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@UpdateTimeTableActivity,
                    " Failed to update to db due to" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun clearData() {
        binding.subjectEt.setText("")
        binding.classTeacherNameEt.setText("")
        binding.startTimeTv.text = ""
        binding.endTimeTv.text = ""
        binding.ongoingTopicEt.setText("")
        binding.syllabusCompletedEt.setText("")
    }
}