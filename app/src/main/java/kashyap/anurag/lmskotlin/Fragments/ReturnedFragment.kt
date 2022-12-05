package kashyap.anurag.lmskotlin.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Adapters.AdapterReturnedBook
import kashyap.anurag.lmskotlin.Models.ModelReturnedBook
import kashyap.anurag.lmskotlin.databinding.FragmentReturnedBinding

class ReturnedFragment : Fragment() {

    private lateinit var binding: FragmentReturnedBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var returnedBooksArrayList: ArrayList<ModelReturnedBook>
    private lateinit var adapterReturnedBook: AdapterReturnedBook

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentReturnedBinding.inflate(layoutInflater)

        firebaseAuth = FirebaseAuth.getInstance()
        loadAllReturnedBooks()

        return binding.root
    }

    private fun loadAllReturnedBooks() {
        binding.progressBar.visibility = View.VISIBLE
        returnedBooksArrayList = ArrayList()
        FirebaseFirestore.getInstance().collection("returnedBooks").document(firebaseAuth.uid!!)
            .collection("Books")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val modelReturnBook: ModelReturnedBook =
                            document.toObject(ModelReturnedBook::class.java)
                        returnedBooksArrayList.add(modelReturnBook)
                    }
                    val layoutManager =
                        LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                    binding.returnedBookRv.layoutManager = layoutManager
                    binding.returnedBookRv.layoutManager = LinearLayoutManager(activity)
                    adapterReturnedBook = AdapterReturnedBook(requireContext(), returnedBooksArrayList)
                    binding.returnedBookRv.adapter = adapterReturnedBook
                    binding.progressBar.visibility = View.GONE
                }
            }
    }

}