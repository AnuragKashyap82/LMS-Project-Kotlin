package kashyap.anurag.lmskotlin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kashyap.anurag.lmskotlin.Adapters.AdapterIssuedBook
import kashyap.anurag.lmskotlin.Models.ModelAppliedBook
import kashyap.anurag.lmskotlin.Models.ModelIssuedBooks
import kashyap.anurag.lmskotlin.databinding.ActivityUsersAllIssuedBooksBinding

class UsersAllIssuedBooksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUsersAllIssuedBooksBinding
    private var uid: String? = null
    private lateinit var issuedBooksArrayList: ArrayList<ModelIssuedBooks>
    private lateinit var adapterIssuedBook: AdapterIssuedBook

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersAllIssuedBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uid = intent.getStringExtra("uid")
        loadUserDetails(uid)
        loadAllUsersBooks(uid!!)
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
    private fun loadAllUsersBooks(uid: String) {
        binding.progressBar.visibility = View.VISIBLE
        issuedBooksArrayList = ArrayList()
        FirebaseFirestore.getInstance().collection("issuedBooks").document(uid).collection("Books")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val modelIssuedBooks: ModelIssuedBooks =
                            document.toObject(ModelIssuedBooks::class.java)
                        issuedBooksArrayList.add(modelIssuedBooks)
                    }
                    val layoutManager = LinearLayoutManager(
                        this@UsersAllIssuedBooksActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    binding.usersBooksRv.layoutManager = layoutManager
                    binding.usersBooksRv.layoutManager =
                        LinearLayoutManager(this@UsersAllIssuedBooksActivity)
                    adapterIssuedBook =
                        AdapterIssuedBook(
                            this@UsersAllIssuedBooksActivity,
                            issuedBooksArrayList,
                            "ADMIN"
                        )
                    binding.usersBooksRv.adapter = adapterIssuedBook
                    binding.progressBar.visibility = View.GONE
                }
            }
    }
}