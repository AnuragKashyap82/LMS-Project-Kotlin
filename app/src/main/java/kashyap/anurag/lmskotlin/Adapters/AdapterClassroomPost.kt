package kashyap.anurag.lmskotlin.Adapters

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.format.DateFormat
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kashyap.anurag.lmskotlin.ClassroomMsgViewActivity
import kashyap.anurag.lmskotlin.Models.ModelClassroomPost
import kashyap.anurag.lmskotlin.PostClassroomMsgEditActivity
import kashyap.anurag.lmskotlin.R
import kashyap.anurag.lmskotlin.databinding.RowClasspostBinding
import java.util.*

class AdapterClassroomPost: RecyclerView.Adapter<AdapterClassroomPost.HolderClassroomPost>  {

    private val context: Context
    public var classroomPostArrayList: ArrayList<ModelClassroomPost>
    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var binding: RowClasspostBinding

    constructor(context: Context, classroomPostArrayList: ArrayList<ModelClassroomPost>) : super() {
        this.context = context
        this.classroomPostArrayList = classroomPostArrayList
        firebaseFirestore  = FirebaseFirestore.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterClassroomPost.HolderClassroomPost {
        binding = RowClasspostBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderClassroomPost(binding.root)
    }

    override fun onBindViewHolder(holder: AdapterClassroomPost.HolderClassroomPost, position: Int) {
        val model = classroomPostArrayList[position]
        val classCode = model.classCode
        val classMsg = model.classMsg
        val timestamp = model.timestamp
        val url = model.url
        val uid = model.uid
        val isAttachmentExist = model.attachmentExist

        loadMsgSenderDetails(model, holder, binding)

        checkAttachment(model, holder)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp.toLong()
        val dateFormat = DateFormat.format("dd/MM/yyyy  K:mm a", calendar).toString()

        holder.dateTv.text = dateFormat
        holder.postTextTv.text = classMsg

        holder.moreBtn.setOnClickListener { checkUser(model, holder) }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ClassroomMsgViewActivity::class.java)
            intent.putExtra("classCode", classCode)
            intent.putExtra("classMsg", classMsg)
            intent.putExtra("timestamp", timestamp)
            intent.putExtra("url", url)
            intent.putExtra("uid", uid)
            intent.putExtra("isAttachmentExist", isAttachmentExist)
            intent.putExtra("msgCode", timestamp)
            context.startActivity(intent)
        }
    }

    private fun loadMsgSenderDetails(
        model: ModelClassroomPost,
        holder: HolderClassroomPost,
        binding: RowClasspostBinding
    ) {
        val documentReference = FirebaseFirestore.getInstance().collection("Users").document(model.uid)
        documentReference.addSnapshotListener { ds, error ->
            val name = "" + ds!!.getString("name")
            val profileImage = "" + ds.getString("profileImage")

            binding.nameTv.text = name

            try {
                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray)
                    .into(binding.profileIv)
            } catch (e: Exception) {
                binding.profileIv.setImageResource(R.drawable.ic_person_gray)
            }
        }
    }

    private fun checkAttachment(
        modelClassroomPost: ModelClassroomPost,
        holder: HolderClassroomPost
    ) {
        val classCode: String = modelClassroomPost.classCode
        val msgCode: String = modelClassroomPost.timestamp
        val documentReference =
            firebaseFirestore.collection("classroom").document(classCode).collection("classMsg")
                .document(msgCode)
        documentReference.addSnapshotListener(
            (context as Activity)
        ) { ds, error ->
            if (modelClassroomPost.attachmentExist) {
                holder.attachmentTv.visibility = View.VISIBLE
            } else {
                holder.attachmentTv.visibility = View.GONE
            }
        }
    }

    private fun checkUser(modelClassroomPost: ModelClassroomPost, holder: HolderClassroomPost) {
        val uid: String = modelClassroomPost.uid
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (uid == firebaseUser!!.uid) {
            showBottomSheetDialog(modelClassroomPost, holder)
        } else {
        }
    }

    private fun showBottomSheetDialog(
        modelClassroomPost: ModelClassroomPost,
        holder: HolderClassroomPost
    ) {
        val classCode: String = modelClassroomPost.classCode
        val msgCode: String = modelClassroomPost.timestamp
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bs_post_type_options)
        val postImageLl = dialog.findViewById<LinearLayout>(R.id.postImageLl)
        val postVideoLl = dialog.findViewById<LinearLayout>(R.id.postVideoLl)
        val askDoubtLl = dialog.findViewById<LinearLayout>(R.id.askDoubtLl)
        val addAchievementLl = dialog.findViewById<LinearLayout>(R.id.addAchievementLl)
        val addDocLl = dialog.findViewById<LinearLayout>(R.id.addDocLl)
        val unEnrollClassroomLl = dialog.findViewById<LinearLayout>(R.id.unEnrollClassroomLl)
        val deleteClassroomPostLl = dialog.findViewById<LinearLayout>(R.id.deleteClassroomPostLl)
        val editClassroomPostLl = dialog.findViewById<LinearLayout>(R.id.editClassroomPostLl)
        postImageLl.visibility = View.GONE
        postVideoLl.visibility = View.GONE
        askDoubtLl.visibility = View.GONE
        addAchievementLl.visibility = View.GONE
        addDocLl.visibility = View.GONE
        unEnrollClassroomLl.visibility = View.GONE
        deleteClassroomPostLl.visibility = View.VISIBLE
        editClassroomPostLl.visibility = View.VISIBLE
        dialog.show()
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.dialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
        deleteClassroomPostLl.setOnClickListener {
            dialog.dismiss()
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are you sure want to Delete Post")
                .setPositiveButton(
                    "Yes"
                ) { dialogInterface, i ->
                    val documentReference =
                        firebaseFirestore.collection("classroom").document(classCode)
                            .collection("classMsg").document(msgCode)
                    documentReference
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Msg Deleted...!",
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
        editClassroomPostLl.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(context, PostClassroomMsgEditActivity::class.java)
            intent.putExtra("classCode", classCode)
            intent.putExtra("msgCode", msgCode)
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int {
        return classroomPostArrayList.size
    }

    inner class  HolderClassroomPost(itemView: View): RecyclerView.ViewHolder(itemView){

        var nameTv: TextView = binding.nameTv
        var postTextTv: TextView = binding.postTextTv
        var dateTv: TextView = binding.dateTv
        var moreBtn: ImageView = binding.moreBtn
        var profileIv: ImageView = binding.profileIv
        var attachmentTv: TextView = binding.attachmentTv
    }
}