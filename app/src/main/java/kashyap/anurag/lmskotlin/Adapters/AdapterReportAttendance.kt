package kashyap.anurag.lmskotlin.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Models.ModelAttendanceReport
import kashyap.anurag.lmskotlin.databinding.RowAttendanceDateBinding

class AdapterReportAttendance: RecyclerView.Adapter<AdapterReportAttendance.HolderReportAttendance> {

    private val context: Context
    public var attendanceReportArrayList: ArrayList<ModelAttendanceReport>

    private lateinit var binding: RowAttendanceDateBinding

    constructor(
        context: Context,
        attendanceReportArrayList: ArrayList<ModelAttendanceReport>
    ) : super() {
        this.context = context
        this.attendanceReportArrayList = attendanceReportArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterReportAttendance.HolderReportAttendance {
        binding = RowAttendanceDateBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderReportAttendance(binding.root)
    }

    override fun onBindViewHolder(holder: AdapterReportAttendance.HolderReportAttendance, position: Int) {
        val model = attendanceReportArrayList[position]
        val uid = model.uid
        val attendance = model.Attendance

        loadUserDetails(uid, holder, binding)

        if (attendance == "Present") {
            holder.presentTv.visibility = View.VISIBLE
            holder.absentTv.visibility = View.GONE
        } else if (attendance == "Absent") {
            holder.presentTv.visibility = View.GONE
            holder.absentTv.visibility = View.VISIBLE
        }

    }

    private fun loadUserDetails(
        uid: String,
        holder: HolderReportAttendance,
        binding: RowAttendanceDateBinding
    ) {
        val documentReference = FirebaseFirestore.getInstance().collection("Users").document(uid)
        documentReference.addSnapshotListener { snapshot, error ->
            val name = snapshot!!["name"].toString()
            val regNo = snapshot["regNo"].toString()
            holder.studentNameTv.setText(name)
            holder.studentRegNoTv.setText(regNo)
        }
    }



    override fun getItemCount(): Int {
        return attendanceReportArrayList.size
    }

    inner class  HolderReportAttendance(itemView: View): RecyclerView.ViewHolder(itemView){

        var studentNameTv: TextView = binding.studentNameTv
        var studentRegNoTv: TextView = binding.studentRegNoTv
        var presentTv: TextView = binding.presentTv
        var absentTv: TextView = binding.absentTv
    }
}