package kashyap.anurag.lmskotlin.Adapters

import android.content.Context
import android.media.Image
import android.text.format.DateFormat
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kashyap.anurag.lmskotlin.Models.ModelComment
import kashyap.anurag.lmskotlin.R
import kashyap.anurag.lmskotlin.databinding.RowCommentBinding
import java.util.*

class AdapterComment: RecyclerView.Adapter<AdapterComment.HolderComment>  {

    private val context: Context
    public var commentArrayList: ArrayList<ModelComment>
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var binding: RowCommentBinding

    constructor(context: Context, commentArrayList: ArrayList<ModelComment>) : super() {
        this.context = context
        this.commentArrayList = commentArrayList
        firebaseAuth = FirebaseAuth.getInstance()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterComment.HolderComment {
        binding = RowCommentBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderComment(binding.root)
    }

    override fun onBindViewHolder(holder: AdapterComment.HolderComment, position: Int) {
        val model = commentArrayList[position]
        val commentId = model.commentId
        val materialId = model.materialId
        val comment = model.comment
        val uid = model.uid
        val timestamp = model.timestamp

        loadUserDetails(model, holder)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp.toLong()
        val dateFormat = DateFormat.format("dd/MM/yyyy", calendar).toString()

        holder.dateTv.text = dateFormat
        holder.commentTv.text = comment

        holder.itemView.setOnClickListener {
            if (firebaseAuth.getCurrentUser() != null) {
                //                    deleteComment(modelComment, holder);
                Toast.makeText(context, "Comments on This Book....!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun loadUserDetails(modelComment: ModelComment, holder: HolderComment) {
        val documentReference =
            FirebaseFirestore.getInstance().collection("Users").document(modelComment.uid)
        documentReference.addSnapshotListener { ds, error ->
            val name = "" + ds!!.getString("name")
            val uniqueId = "" + ds.getString("uniqueId")
            val profileImage = "" + ds.getString("profileImage")

            binding.nameTv.text = name

            try {
                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray)
                    .into(binding.profileTv)
            } catch (e: Exception) {
                binding.profileTv.setImageResource(R.drawable.ic_person_gray)
            }
        }
    }

    override fun getItemCount(): Int {
        return commentArrayList.size
    }

    inner class  HolderComment(itemView: View): RecyclerView.ViewHolder(itemView){
        var profileTv: ImageView = binding.profileTv
        var nameTv: TextView = binding.nameTv
        var dateTv: TextView = binding.dateTv
        var commentTv: TextView = binding.commentTv
    }
}