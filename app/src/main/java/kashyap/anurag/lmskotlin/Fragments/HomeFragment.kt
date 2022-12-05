package kashyap.anurag.lmskotlin.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Adapters.AdapterAppliedBooks
import kashyap.anurag.lmskotlin.Adapters.AdapterNotice
import kashyap.anurag.lmskotlin.Models.ModelAppliedBook
import kashyap.anurag.lmskotlin.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var appliedBooksArrayList: ArrayList<ModelAppliedBook>
    private lateinit var adapterAppliedBooks: AdapterAppliedBooks

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater)

        firebaseAuth = FirebaseAuth.getInstance()
        loadAllAppliedBooks()

        return  binding.root
    }

    private fun loadAllAppliedBooks() {
        binding.progressBar.visibility = View.VISIBLE
        appliedBooksArrayList = ArrayList()
        FirebaseFirestore.getInstance().collection("issuedApplied").document(firebaseAuth.uid!!)
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
                            activity,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                    binding.issuedBooksRv.layoutManager = layoutManager
                    binding.issuedBooksRv.layoutManager = LinearLayoutManager(activity)
                    adapterAppliedBooks = AdapterAppliedBooks(requireContext(), appliedBooksArrayList, "USER")
                    binding.issuedBooksRv.adapter = adapterAppliedBooks
                    binding.progressBar.visibility = View.GONE
                }
            }
    }
}