package kashyap.anurag.lmskotlin.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kashyap.anurag.lmskotlin.Models.ModelAppliedUser
import kashyap.anurag.lmskotlin.R
import kashyap.anurag.lmskotlin.UsersAllAppliedBooksActivity
import kashyap.anurag.lmskotlin.UsersAllIssuedBooksActivity
import kashyap.anurag.lmskotlin.databinding.RowAppliedBookUserBinding

class AdapterAppliedUser: RecyclerView.Adapter<AdapterAppliedUser.HolderAppliedUser>  {

    private val context: Context
    public var appliedUserArrayList: ArrayList<ModelAppliedUser>
    private var LAYOUT_CODE: String? = ""
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var binding: RowAppliedBookUserBinding

    constructor(
        context: Context,
        appliedUserArrayList: ArrayList<ModelAppliedUser>,
        LAYOUT_CODE: String?
    ) : super() {
        this.context = context
        this.appliedUserArrayList = appliedUserArrayList
        this.LAYOUT_CODE = LAYOUT_CODE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterAppliedUser.HolderAppliedUser {
        binding = RowAppliedBookUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderAppliedUser(binding.root)
    }

    override fun onBindViewHolder(holder: AdapterAppliedUser.HolderAppliedUser, position: Int) {
        val model = appliedUserArrayList[position]
        val uid = model.uid

        loadUserDetails(uid, holder)

        if (LAYOUT_CODE == "ADMIN") {
            holder.itemView.setOnClickListener(View.OnClickListener {
                val intent = Intent(context, UsersAllAppliedBooksActivity::class.java)
                intent.putExtra("uid", uid)
                context.startActivity(intent)
            })
        } else if (LAYOUT_CODE == "USER") {
            holder.itemView.setOnClickListener(View.OnClickListener {
                val intent = Intent(context, UsersAllIssuedBooksActivity::class.java)
                intent.putExtra("uid", uid)
                context.startActivity(intent)
            })
        }
    }

    private fun loadUserDetails(uid: String, holder: HolderAppliedUser) {
        val documentReference =
            FirebaseFirestore.getInstance().collection("Users").document(uid)
        documentReference.addSnapshotListener { ds, error ->
            val name = "" + ds!!.getString("name")
            val uniqueId = "" + ds.getString("uniqueId")
            val profileImage = "" + ds.getString("profileImage")

            binding.userNameTv.text = name
            binding.studentIdTv.text = uniqueId

            try {
                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray)
                    .into(binding.profileIv)
            } catch (e: Exception) {
                binding.profileIv.setImageResource(R.drawable.ic_person_gray)
            }
        }
    }

    override fun getItemCount(): Int {
        return appliedUserArrayList.size
    }

    inner class  HolderAppliedUser(itemView: View): RecyclerView.ViewHolder(itemView){

        var userNameTv: TextView = binding.userNameTv
        var profileIv: ImageView = binding.profileIv
        var studentIdTv: TextView = binding.studentIdTv

    }
}