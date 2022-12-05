package kashyap.anurag.lmskotlin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kashyap.anurag.lmskotlin.Adapters.AdapterAppliedBooks
import kashyap.anurag.lmskotlin.Models.ModelAppliedBook
import kashyap.anurag.lmskotlin.databinding.ActivityUsersAllAppliedBooksBinding

class UsersAllAppliedBooksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUsersAllAppliedBooksBinding
    private var uid: String? = null
    private lateinit var appliedBooksArrayList: ArrayList<ModelAppliedBook>
    private lateinit var adapterAppliedBooks: AdapterAppliedBooks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersAllAppliedBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uid = intent.getStringExtra("uid")
        loadUserDetails(uid)
        loadAllUsersBooks(uid)
    }

    private fun loadUserDetails(uid: String?) {
        val documentReference = FirebaseFirestore.getInstance().collection("Users").document(uid!!)
        documentReference.addSnapshotListener(this) { ds, error ->
            if (ds!!.exists()){
                val name = "" + ds.getString("name")
                val uniqueId = "" + ds.getString("uniqueId")
                val profileImage = "" + ds.getString("profileImage")

                binding.studentNameTv.text = name
                binding.studentIdTv.text = uniqueId

                try {
                    Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray)
                        .into(binding.profileIv)
                } catch (e: Exception) {
                    binding.profileIv.setImageResource(R.drawable.ic_person_gray)
                }
            }
        }
    }

    private fun loadAllUsersBooks(uid: String?) {
        binding.progressBar.visibility = View.VISIBLE
        appliedBooksArrayList = ArrayList()
        FirebaseFirestore.getInstance().collection("issuedApplied").document(uid!!)
            .collection("Books")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val modelAppliedBook: ModelAppliedBook =
                            document.toObject(ModelAppliedBook::class.java)
                        appliedBooksArrayList.add(modelAppliedBook)
                    }
                    val layoutManager =
                        LinearLayoutManager(
                            this,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                    binding.usersBooksRv.layoutManager = layoutManager
                    binding.usersBooksRv.layoutManager = LinearLayoutManager(this)
                    adapterAppliedBooks = AdapterAppliedBooks(this, appliedBooksArrayList, "ADMIN")
                    binding.usersBooksRv.adapter = adapterAppliedBooks
                    binding.progressBar.visibility = View.GONE
                }
            }
    }

}