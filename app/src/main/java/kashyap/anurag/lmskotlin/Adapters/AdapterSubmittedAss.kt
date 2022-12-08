package kashyap.anurag.lmskotlin.Adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kashyap.anurag.lmskotlin.Models.ModelSubmittedAss
import kashyap.anurag.lmskotlin.R
import kashyap.anurag.lmskotlin.SubmittedAssViewActivity
import kashyap.anurag.lmskotlin.databinding.RowSubmittedAssBinding
import java.util.*

class AdapterSubmittedAss: RecyclerView.Adapter<AdapterSubmittedAss.HolderSubmittedAss>  {

    private val context: Context
    public var submittedAssArrayList: ArrayList<ModelSubmittedAss>
    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var binding: RowSubmittedAssBinding

    constructor(context: Context, submittedAssArrayList: ArrayList<ModelSubmittedAss>) : super() {
        this.context = context
        this.submittedAssArrayList = submittedAssArrayList
        firebaseFirestore = FirebaseFirestore.getInstance()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterSubmittedAss.HolderSubmittedAss {
        binding = RowSubmittedAssBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderSubmittedAss(binding.root)
    }

    override fun onBindViewHolder(holder: AdapterSubmittedAss.HolderSubmittedAss, position: Int) {
        val model = submittedAssArrayList[position]
        val date = model.timestamp
        val classCode = model.classCode
        val assignmentName = model.assignmentName
        val submittedAssUrl = model.url
        val assignmentId = model.assignmentId
        val dueDate = model.dueDate
        val uid = model.uid
        val fullMarks = model.fullMarks
        val marksObtained = model.marksObtained

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date.toLong()
        val dateFormat = DateFormat.format("dd/MM/yyyy", calendar).toString()

        loadStudentsDetails(model, holder, binding)

        binding.dateTv.setText(dateFormat)
        binding.obtainedMarksTv.setText(marksObtained)
        binding.fullMarksTv.setText(fullMarks)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, SubmittedAssViewActivity::class.java)
            intent.putExtra("submittedAssUrl", submittedAssUrl)
            intent.putExtra("assignmentId", assignmentId)
            intent.putExtra("dueDate", dueDate)
            intent.putExtra("assignmentName", assignmentName)
            intent.putExtra("classCode", classCode)
            intent.putExtra("uid", uid)
            context.startActivity(intent)
        }
    }

    private fun loadStudentsDetails(
        modelAssignment: ModelSubmittedAss,
        holder: HolderSubmittedAss,
        binding: RowSubmittedAssBinding
    ) {
        val uid: String = modelAssignment.uid
        val documentReference = firebaseFirestore.collection("Users").document(uid)
        documentReference.addSnapshotListener(
            (context as Activity)
        ) { ds, error ->
            val name = "" + ds!!.getString("name")
            val profileImage = "" + ds.getString("profileImage")
            holder.nameTv.text = name
            try {
                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray)
                    .into(holder.profileTv)
            } catch (e: Exception) {
                holder.profileTv.setImageResource(R.drawable.ic_person_gray)
            }
        }
    }

    override fun getItemCount(): Int {
        return submittedAssArrayList.size
    }

    inner class  HolderSubmittedAss(itemView: View): RecyclerView.ViewHolder(itemView){

        var nameTv: TextView = binding.nameTv
        var dateTv: TextView = binding.dateTv
        var profileTv: ImageView = binding.profileTv
        var obtainedMarksTv: TextView = binding.obtainedMarksTv
        var fullMarksTv: TextView = binding.fullMarksTv

    }


}