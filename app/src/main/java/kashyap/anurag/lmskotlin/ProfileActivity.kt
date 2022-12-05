package kashyap.anurag.lmskotlin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kashyap.anurag.lmskotlin.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        loadMyInfo()

        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.profileEditBtn.setOnClickListener {
            startActivity(Intent(this, ProfileEditActivity::class.java))
        }
    }
    private fun loadMyInfo() {
        val documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.uid!!)
        documentReference.addSnapshotListener(this) { ds, error ->
            if (ds!!.exists()){
                val userType = "" + ds!!.getString("userType")
                val email = "" + ds.getString("email")
                val name = "" + ds.getString("name")
                val address = "" + ds.getString("address")
                val city = "" + ds.getString("city")
                val state = "" + ds.getString("state")
                val country = "" + ds.getString("country")
                val uniqueId = "" + ds.getString("uniqueId")
                val online = "" + ds.getString("online")
                val phone = "" + ds.getString("phone")
                val profileImage = "" + ds.getString("profileImage")
                val timestamp = "" + ds.getString("timestamp")
                val uid = "" + ds.getString("uid")
                val regNo = "" + ds.getString("regNo")
                val dob = "" + ds.getString("dob")
                val fatherName = "" + ds.getString("fatherName")
                val motherName = "" + ds.getString("motherName")
                val branch = "" + ds.getString("branch")
                val semester = "" + ds.getString("semester")
                val session = "" + ds.getString("session")
                val seatType = "" + ds.getString("seatType")
                val hostler = "" + ds.getString("hostler")


                binding.nameTv.text = name
                binding.phoneTv.text = "+91$phone"
                binding.emailTv.text = email
                binding.addressTv.text = address
                binding.cityTv.text = city
                binding.stateTv.text = state
                binding.countryTv.text = country
                binding.uniqueIdTv.text = uniqueId
                binding.regNoTv.text = regNo
                binding.dobTv.text = dob
                binding.fatherNameTv.text = fatherName
                binding.motherNameTv.text = motherName
                binding.branchTv.text = branch
                binding.semesterTv.text = semester
                binding.sessionTv.text = session
                binding.seatTypeTv.text = seatType
                binding.hostlerTv.text = hostler

                if (userType == "user") {
                    binding.adminVector.visibility = View.GONE
                } else if (userType == "teachers") {
                    binding.adminVector.visibility = View.VISIBLE
                } else if (userType == "anurag") {
                    binding.adminVector.visibility = View.VISIBLE
                }
                try {
                    Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray)
                        .into(binding.profileIv)
                } catch (e: Exception) {
                    binding.profileIv.setImageResource(R.drawable.ic_person_gray)
                }
            }
        }
    }
}