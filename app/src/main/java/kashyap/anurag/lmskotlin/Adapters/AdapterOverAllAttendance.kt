package kashyap.anurag.lmskotlin.Adapters

import android.content.Context
import android.graphics.Color.red
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Models.ModelAtteStud
import kashyap.anurag.lmskotlin.databinding.RowOverAllAttendItemBinding
import java.util.*

class AdapterOverAllAttendance: RecyclerView.Adapter<AdapterOverAllAttendance.HolderOverAllAtendance> {

    private val context: Context
    public var atteStudArrayList: ArrayList<ModelAtteStud>

    private lateinit var binding: RowOverAllAttendItemBinding

    constructor(context: Context, atteStudArrayList: ArrayList<ModelAtteStud>) : super() {
        this.context = context
        this.atteStudArrayList = atteStudArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterOverAllAttendance.HolderOverAllAtendance {
        binding = RowOverAllAttendItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderOverAllAtendance(binding.root)
    }

    override fun onBindViewHolder(holder: AdapterOverAllAttendance.HolderOverAllAtendance, position: Int) {
        val model = atteStudArrayList[position]
        val uid = model.uid
        val monthYear = model.monthYear
        var presentCount = model.presentCount
        val absentCount = model.absentCount
        val classCode = model.classCode


        loadStudentDetails(uid, holder)
        if (presentCount == null) {
            presentCount = "0"
            holder.presentDaysTv.setText(presentCount)
            loadPercentPresent(presentCount, holder, classCode)
        }else{
            holder.presentDaysTv.text = "$presentCount Day"
            loadPercentPresent(presentCount, holder, classCode)
        }

    }

    private fun loadStudentDetails(uid: String, holder: HolderOverAllAtendance) {
        val documentReference = FirebaseFirestore.getInstance().collection("Users").document(uid)
        documentReference.addSnapshotListener { snapshot, error ->
            val name = snapshot!!["name"].toString()
            holder.studentNameTv.setText(name)
        }
    }

    private fun loadPercentPresent(
        presentCount: String,
        holder: HolderOverAllAtendance,
        classCode: String
    ) {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val currentYear = calendar[Calendar.YEAR]
        val currentMonth = calendar[Calendar.MONTH] + 1
        val currentDay = calendar[Calendar.DAY_OF_MONTH]
        val monthYear = "" + currentMonth + currentYear
        val databaseReference = FirebaseDatabase.getInstance().getReference("classroom")
        databaseReference.child(classCode).child("noOfClass").child("" + monthYear)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val totalNoClass = snapshot.child("noOfClass").value.toString()
                        val presentPercent = presentCount.toInt() * 100 / totalNoClass.toInt()
                        holder.presentPercentTv.setText("$presentPercent%")
                        if (presentPercent < 75) {
//                            holder.presentPercentTv.setTextColor(context.resources.getColor(R.color.red))
                        } else {
//                            holder.presentPercentTv.setTextColor(context.resources.getColor(R.color.green))
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun getItemCount(): Int {
        return atteStudArrayList.size
    }

    inner class  HolderOverAllAtendance(itemView: View): RecyclerView.ViewHolder(itemView){

        var studentNameTv: TextView = binding.studentNameTv
        var presentDaysTv: TextView = binding.presentDaysTv
        var presentPercentTv: TextView = binding.presentPercentTv

    }
}