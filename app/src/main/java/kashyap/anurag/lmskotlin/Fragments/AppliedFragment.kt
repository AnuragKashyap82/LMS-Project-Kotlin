package kashyap.anurag.lmskotlin.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Adapters.AdapterAppliedUser
import kashyap.anurag.lmskotlin.Models.ModelAppliedUser
import kashyap.anurag.lmskotlin.databinding.FragmentAppliedBinding

class AppliedFragment : Fragment() {

    private lateinit var binding: FragmentAppliedBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var appliedUserArrayList: ArrayList<ModelAppliedUser>
    private lateinit var adapterAppliedUser: AdapterAppliedUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentAppliedBinding.inflate(layoutInflater)

        firebaseAuth  = FirebaseAuth.getInstance()
        loadAllAppliedUsers()

        return binding.root
    }

    private fun loadAllAppliedUsers() {
        binding.progressBar.visibility = View.VISIBLE
        appliedUserArrayList = java.util.ArrayList()
        FirebaseFirestore.getInstance().collection("issuedApplied")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val modelAppliedUser = document.toObject(ModelAppliedUser::class.java)
                        appliedUserArrayList.add(modelAppliedUser)
                    }
                    val layoutManager =
                        LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                    binding.issuedBooksRv.layoutManager = layoutManager
                    binding.issuedBooksRv.layoutManager = LinearLayoutManager(activity)
                    adapterAppliedUser =
                        AdapterAppliedUser(requireContext(), appliedUserArrayList, "ADMIN")
                    binding.issuedBooksRv.adapter = adapterAppliedUser
                    binding.progressBar.visibility = View.GONE
                }
            }
    }

}