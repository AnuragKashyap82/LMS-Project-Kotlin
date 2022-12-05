package kashyap.anurag.lmskotlin.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Adapters.AdapterIssuedBook
import kashyap.anurag.lmskotlin.Models.ModelIssuedBooks
import kashyap.anurag.lmskotlin.databinding.FragmentIssuedBinding


class IssuedFragment : Fragment() {

    private lateinit var binding: FragmentIssuedBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var issuedBooksArrayList: ArrayList<ModelIssuedBooks>
    private lateinit var adapterIssuedBook: AdapterIssuedBook

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentIssuedBinding.inflate(layoutInflater)

        firebaseAuth = FirebaseAuth.getInstance()
        loadAllIssuedBooks()

        return binding.root
    }

    private fun loadAllIssuedBooks() {
        binding.progressBar.setVisibility(View.VISIBLE)
        issuedBooksArrayList = ArrayList()
        FirebaseFirestore.getInstance().collection("issuedBooks").document(firebaseAuth.uid!!)
            .collection("Books")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val modelIssuedBooks = document.toObject(ModelIssuedBooks::class.java)
                        issuedBooksArrayList.add(modelIssuedBooks)
                    }
                    val layoutManager =
                        LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                    binding.issuedBooksRv.setLayoutManager(layoutManager)
                    binding.issuedBooksRv.setLayoutManager(LinearLayoutManager(activity))
                    adapterIssuedBook = AdapterIssuedBook(requireContext(), issuedBooksArrayList, "USER")
                    binding.issuedBooksRv.setAdapter(adapterIssuedBook)
                    binding.progressBar.setVisibility(View.GONE)
                }
            }
    }
}