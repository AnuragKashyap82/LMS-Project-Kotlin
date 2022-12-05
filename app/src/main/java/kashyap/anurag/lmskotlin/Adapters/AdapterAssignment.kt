package kashyap.anurag.lmskotlin.Adapters

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.AssignmentViewActivity
import kashyap.anurag.lmskotlin.Models.ModelAssignment
import kashyap.anurag.lmskotlin.databinding.RowAssignmentBinding
import java.util.*

class AdapterAssignment: RecyclerView.Adapter<AdapterAssignment.HolderAssignment> {

    private val context: Context
    public var assignmentArrayList: ArrayList<ModelAssignment>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var isCompletedAssignment: Boolean? = false

    private lateinit var binding: RowAssignmentBinding

    constructor(context: Context, assignmentArrayList: ArrayList<ModelAssignment>) : super() {
        this.context = context
        this.assignmentArrayList = assignmentArrayList
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterAssignment.HolderAssignment {
        binding = RowAssignmentBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderAssignment(binding.root)
    }

    override fun onBindViewHolder(holder: AdapterAssignment.HolderAssignment, position: Int) {
        val model = assignmentArrayList[position]
        val assignmentName = model.assignmentName
        val classCode = model.classCode
        val date = model.timestamp
        val assignmentUrl = model.url
        val assignmentId = model.assignmentId
        val dueDate = model.dueDate
        val fullMarks = model.fullMarks
        val uid = model.uid

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date.toLong()
        val dateFormat = DateFormat.format("dd/MM/yyyy", calendar).toString()

        holder.dateTv.text = dateFormat
        holder.dueDateTv.text = dueDate
        holder.assignmentName.text = assignmentName

        checkSubmittedAss(model, holder)
        checkTeacherDetails(model, holder)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, AssignmentViewActivity::class.java)
            intent.putExtra("assignmentUrl", assignmentUrl)
            intent.putExtra("assignmentName", assignmentName)
            intent.putExtra("classCode", classCode)
            intent.putExtra("assignmentId", assignmentId)
            intent.putExtra("dueDate", dueDate)
            intent.putExtra("fullMarks", fullMarks)
            context.startActivity(intent)
        }

    }

    private fun checkTeacherDetails(modelAssignment: ModelAssignment, holder: HolderAssignment) {
        val uid: String = modelAssignment.uid
        val documentReference: DocumentReference =
            firebaseFirestore.collection("Users").document(uid)
        documentReference.addSnapshotListener { value, error ->
            val assignedBy = "" + value!!.getString("name")
            holder.assignedByTv.text = assignedBy
        }
    }

    private fun checkSubmittedAss(modelAssignment: ModelAssignment, holder: HolderAssignment) {
        val classCode: String = modelAssignment.classCode
        val assignmentId: String = modelAssignment.assignmentId
        val documentReference =
            firebaseFirestore.collection("classroom").document(classCode).collection("assignment")
                .document(assignmentId).collection("Submission").document(
                    firebaseAuth.uid!!
                )
        documentReference.addSnapshotListener { value, error ->
            isCompletedAssignment = value!!.exists()
            if (isCompletedAssignment!!) {
                holder.profileIv.visibility = View.GONE
                holder.submittedIv.visibility = View.VISIBLE
            } else {
                holder.profileIv.visibility = View.VISIBLE
                holder.submittedIv.visibility = View.GONE
            }
        }
    }


    override fun getItemCount(): Int {
        return assignmentArrayList.size
    }

    inner class  HolderAssignment(itemView: View): RecyclerView.ViewHolder(itemView){

        var assignmentName: TextView = binding.assignmentName
        var assignedByTv: TextView = binding.assignedByTv
        var dateTv: TextView = binding.dateTv
        var dueDateTv: TextView = binding.dueDateTv
        var profileIv: ImageView = binding.profileIv
        var submittedIv: ImageView = binding.submittedIv

    }

}