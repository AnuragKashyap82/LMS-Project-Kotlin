package kashyap.anurag.lmskotlin.Adapters

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.EditTimeTableActivity
import kashyap.anurag.lmskotlin.Models.ModelTimeTable
import kashyap.anurag.lmskotlin.R
import kashyap.anurag.lmskotlin.databinding.RowTimetableBinding

class AdapterTimeTable : RecyclerView.Adapter<AdapterTimeTable.HolderTimeTable>{

    private val context: Context
    public var timeTableArrayList: ArrayList<ModelTimeTable>
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var binding: RowTimetableBinding

    constructor(context: Context, timeTableArrayList: ArrayList<ModelTimeTable>) {
        this.context = context
        this.timeTableArrayList = timeTableArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderTimeTable {
        binding = RowTimetableBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderTimeTable(binding.root)
    }

    override fun onBindViewHolder(holder: HolderTimeTable, position: Int) {
        val model = timeTableArrayList[position]
        val subName = model.subName
        val teacherName = model.teacherName
        val startTime = model.startTime
        val endTime = model.endTime
        val day = model.day
        val ongoingTopic = model.ongoingTopic
        val percentSylComp = model.percentSylComp

        binding.subNameTv.text = subName
        binding.teacherNameTv.text = teacherName
        binding.startTimeTv.text = ""+ startTime + "\n" + "Hr"
        binding.endTimeTv.text = "$endTime\nHr"
        binding.ongoingTopicTv.text = ongoingTopic
        binding.syllabusCompletedTv.text = percentSylComp

        firebaseAuth = FirebaseAuth.getInstance()

        holder.itemView.setOnClickListener {
            showBottomSheetDialog(model, holder)
        }
    }

    private fun showBottomSheetDialog(model: ModelTimeTable, holder: AdapterTimeTable.HolderTimeTable) {
        val day: String = model.day
        val subName: String = model.subName
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bs_more_feed_options)
        val editPostLL = dialog.findViewById<LinearLayout>(R.id.editPostLL)
        val deletePostLL = dialog.findViewById<LinearLayout>(R.id.deletePostLL)
        val sharePostLL = dialog.findViewById<LinearLayout>(R.id.sharePostLL)
        val favPostLL = dialog.findViewById<LinearLayout>(R.id.favPostLL)
        val blockPostLL = dialog.findViewById<LinearLayout>(R.id.blockPostLL)
        editPostLL.visibility = View.VISIBLE
        deletePostLL.visibility = View.VISIBLE
        sharePostLL.visibility = View.GONE
        favPostLL.visibility = View.GONE
        blockPostLL.visibility = View.GONE
        dialog.show()
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.dialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
        editPostLL.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(context, EditTimeTableActivity::class.java)
            intent.putExtra("day", day)
            intent.putExtra("subName", subName)
            context.startActivity(intent)
        }
        deletePostLL.setOnClickListener {
            dialog.dismiss()
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are you sure want to Delete this class")
                .setPositiveButton(
                    "Yes"
                ) { dialogInterface, i ->
                    val documentReference = FirebaseFirestore.getInstance().collection("timeTable")
                        .document(firebaseAuth.uid!!).collection(day).document(subName)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Class Deleted...!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "" + e.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .setNegativeButton(
                    "No"
                ) { dialogInterface, i -> dialogInterface.dismiss() }
                .show()
        }
    }

    override fun getItemCount(): Int {
        return timeTableArrayList.size
    }

    inner class  HolderTimeTable(itemView: View): RecyclerView.ViewHolder(itemView){

        var subNameTv: TextView = binding.subNameTv
        var teacherNameTv: TextView = binding.teacherNameTv
        var startTimeTv: TextView = binding.startTimeTv
        var endTimeTv: TextView = binding.endTimeTv
        var ongoingTopicTv: TextView = binding.ongoingTopicTv
        var syllabusCompletedTv: TextView = binding.syllabusCompletedTv
    }
}