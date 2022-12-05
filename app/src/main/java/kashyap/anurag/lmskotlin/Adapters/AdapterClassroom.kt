package kashyap.anurag.lmskotlin.Adapters

import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kashyap.anurag.lmskotlin.ClassroomViewActivity
import kashyap.anurag.lmskotlin.Models.ModelClassroom
import kashyap.anurag.lmskotlin.R
import kashyap.anurag.lmskotlin.databinding.RowClassBinding

class AdapterClassroom: RecyclerView.Adapter<AdapterClassroom.HolderClassroom> {

    private val context: Context
    public var classroomArrayList: ArrayList<ModelClassroom>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var binding: RowClassBinding

    constructor(context: Context, classroomArrayList: ArrayList<ModelClassroom>) : super() {
        this.context = context
        this.classroomArrayList = classroomArrayList
        firebaseAuth  = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterClassroom.HolderClassroom {
        binding = RowClassBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderClassroom(binding.root)
    }

    override fun onBindViewHolder(holder: AdapterClassroom.HolderClassroom, position: Int) {
        val model = classroomArrayList[position]
        val classCode = model.classCode
        val className = model.className
        val subjectName = model.subjectName
        val theme = model.theme
        val uid = model.uid
        val timestamp = model.timestamp

        binding.classCode.setText(classCode)
        loadClassDetails(model, holder)

        holder.copyClassCodeBtn.setOnClickListener(View.OnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("classCode", holder.classCode.text)
            if (clipboard == null || clip == null) {
                holder.copyClassCodeBtn.visibility = View.VISIBLE
                holder.copiedClassCodeBtn.visibility = View.GONE
                return@OnClickListener
            }
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Class Code copied...!!!!", Toast.LENGTH_SHORT).show()
            holder.copyClassCodeBtn.visibility = View.GONE
            holder.copiedClassCodeBtn.visibility = View.VISIBLE
        })
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ClassroomViewActivity::class.java)
            intent.putExtra("classCode", classCode)
            context.startActivity(intent)
        }

    }

    private fun loadClassDetails(model: ModelClassroom, holder: HolderClassroom) {
        val documentReference =
            FirebaseFirestore.getInstance().collection("classroom").document(model.classCode)
        documentReference.addSnapshotListener { ds, error ->

            val subjectName = "" + ds!!.getString("subjectName")
            val className = "" + ds.getString("className")
            val classCode = "" + ds.getString("classCode")
            val uid = "" + ds.getString("uid")
            val theme = "" + ds.getString("theme")
            val timestamp = "" + ds.getString("timestamp")

            binding.classNameTv.text = className
            binding.subjectNameTv.text = subjectName

            loadClassTeacherDetails(uid, holder)

            holder.moreBtn.setOnClickListener {
                if (uid == firebaseAuth.uid) {
                } else {
                    showBottomSheetDialog(model, holder)
                }
            }

            if (theme == "1") {
                holder.classBg.setImageResource(R.drawable.class_bg_one)
            } else if (theme == "2") {
                holder.classBg.setImageResource(R.drawable.class_bg_two)
            } else if (theme == "3") {
                holder.classBg.setImageResource(R.drawable.class_bd_three)
            } else if (theme == "4") {
                holder.classBg.setImageResource(R.drawable.class_bg_four)
            } else if (theme == "5") {
                holder.classBg.setImageResource(R.drawable.class_bg_five)
            }
        }
    }

    private fun loadClassTeacherDetails(uid: String, holder: HolderClassroom) {
        val documentReference =
            FirebaseFirestore.getInstance().collection("Users").document(uid)
        documentReference.addSnapshotListener { ds, error ->

            val name = "" + ds!!.getString("name")
            val profileImage = "" + ds.getString("profileImage")


            binding.classTeacherTv.text = "($name)"

            try {
                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray)
                    .into(binding.profileIv)
            } catch (e: Exception) {
                binding.profileIv.setImageResource(R.drawable.ic_person_gray)
            }
        }
    }

    private fun showBottomSheetDialog(modelClassroom: ModelClassroom, holder: HolderClassroom) {
        val classCode: String = modelClassroom.classCode
        val uid: String = modelClassroom.uid
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bs_post_type_options)
        val postImageLl = dialog.findViewById<LinearLayout>(R.id.postImageLl)
        val postVideoLl = dialog.findViewById<LinearLayout>(R.id.postVideoLl)
        val askDoubtLl = dialog.findViewById<LinearLayout>(R.id.askDoubtLl)
        val addAchievementLl = dialog.findViewById<LinearLayout>(R.id.addAchievementLl)
        val addDocLl = dialog.findViewById<LinearLayout>(R.id.addDocLl)
        val unEnrollClassroomLl = dialog.findViewById<LinearLayout>(R.id.unEnrollClassroomLl)
        postImageLl.visibility = View.GONE
        postVideoLl.visibility = View.GONE
        askDoubtLl.visibility = View.GONE
        addAchievementLl.visibility = View.GONE
        addDocLl.visibility = View.GONE
        unEnrollClassroomLl.visibility = View.VISIBLE
        dialog.show()
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.dialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
        unEnrollClassroomLl.setOnClickListener {
            dialog.dismiss()
            val builder = AlertDialog.Builder(context)
            builder.setTitle("UnEnroll")
                .setMessage("Are you sure want to UnEnroll")
                .setPositiveButton(
                    "Yes"
                ) { dialogInterface, i ->
                    unEnrollClassroom(modelClassroom, holder)
                }
                .setNegativeButton(
                    "No"
                ) { dialogInterface, i -> dialogInterface.dismiss() }
                .show()
        }
    }

    private fun unEnrollClassroom(modelClassroom: ModelClassroom, holder: HolderClassroom) {
        val documentReference = firebaseFirestore.collection("Users").document(
            firebaseAuth.uid!!
        ).collection("classroom").document(modelClassroom.classCode)
        documentReference.delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Class UnEnroll...!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int {
        return classroomArrayList.size
    }

    inner class  HolderClassroom(itemView: View): RecyclerView.ViewHolder(itemView){

        var classNameTv: TextView = binding.classNameTv
        var subjectNameTv: TextView = binding.subjectNameTv
        var classCode: TextView = binding.classCode
        var classTeacherTv: TextView = binding.classTeacherTv
        var copyClassCodeBtn: ImageView = binding.copyClassCodeBtn
        var copiedClassCodeBtn: ImageView = binding.copiedClassCodeBtn
        var moreBtn: ImageView = binding.moreBtn
        var classBg: ImageView = binding.classBg
        var profileIv: ImageView = binding.profileIv
    }
}