package kashyap.anurag.lmskotlin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Adapters.AdapterTimeTable
import kashyap.anurag.lmskotlin.Models.ModelTimeTable
import kashyap.anurag.lmskotlin.databinding.ActivityTimeTableBinding
import java.util.*

class TimeTableActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimeTableBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var day: String? = null
    private lateinit var timeTableArrayList: ArrayList<ModelTimeTable>
    private lateinit var adapterTimeTable: AdapterTimeTable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimeTableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.dayTv.text = intent.getStringExtra("text")
        day = binding.dayTv.text.toString().trim()

        loadAllClasses()

        binding.updateTimeTableBtn.setOnClickListener { view ->
            val text = binding.dayTv.text.toString()
            val myIntent = Intent(view.context, UpdateTimeTableActivity::class.java)
            myIntent.putExtra("text", text)
            startActivity(myIntent)
        }
    }
    private fun loadAllClasses() {
        timeTableArrayList = ArrayList()
        FirebaseFirestore.getInstance().collection("timeTable").document(firebaseAuth.uid!!)
            .collection(
                day!!
            )
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val modelTimeTable: ModelTimeTable =
                            document.toObject(ModelTimeTable::class.java)
                        timeTableArrayList.add(modelTimeTable)
                    }
//                    Collections.sort(timeTableArrayList,
//                        Comparator<Any?> { t1, t2 ->
//                            t1.startTime.compareToIgnoreCase(t2.startTime)
//                        })
//                    Collections.reverse(timeTableArrayList)
                    val layoutManager =
                        LinearLayoutManager(
                            this@TimeTableActivity,
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                    binding.timeTableRv.layoutManager = layoutManager
                    binding.timeTableRv.layoutManager = LinearLayoutManager(this@TimeTableActivity)
                    adapterTimeTable = AdapterTimeTable(this@TimeTableActivity, timeTableArrayList)
                    binding.timeTableRv.adapter = adapterTimeTable
                }
            }
    }
}