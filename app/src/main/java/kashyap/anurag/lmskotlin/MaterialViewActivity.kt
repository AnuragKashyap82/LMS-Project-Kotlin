package kashyap.anurag.lmskotlin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import kashyap.anurag.lmskotlin.databinding.ActivityMaterialViewBinding

class MaterialViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMaterialViewBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var materialId: String? = null
    private var topic: String? = null
    private var semester: String? = null
    private var branch: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaterialViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val mFirebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig.fetchAndActivate().addOnSuccessListener(OnSuccessListener<Boolean?> {
            val isDeleteMaterialEnabled: Boolean =
                mFirebaseRemoteConfig.getBoolean("isDeleteMaterialEnabled")
            if (isDeleteMaterialEnabled) {
                binding.deleteMaterialBtn.setOnClickListener {
                    val builder = AlertDialog.Builder(this@MaterialViewActivity)
                    builder.setTitle("Delete")
                        .setMessage("Are you sure want to Delete Material: $topic ?")
                        .setPositiveButton(
                            "Yes"
                        ) { dialogInterface, i -> deleteMaterial() }
                        .setNegativeButton(
                            "No"
                        ) { dialogInterface, i -> dialogInterface.dismiss() }
                        .show()
                }
            } else {
                binding.deleteMaterialBtn.setOnClickListener {
                    Toast.makeText(
                        this@MaterialViewActivity,
                        "This Features is Temporarily disabled...!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

        materialId = intent.getStringExtra("materialId")
        semester = intent.getStringExtra("semester")
        branch = intent.getStringExtra("branch")
        topic = intent.getStringExtra("topicName")

        checkUser()
        loadMaterial()
        incrementMaterialViewCount(materialId)

        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.editMaterialBtn.setOnClickListener {
            val intent = Intent(this@MaterialViewActivity, EditMaterialActivity::class.java)
            intent.putExtra("materialId", materialId)
            intent.putExtra("branch", branch)
            intent.putExtra("semester", semester)
            startActivity(intent)
            Toast.makeText(
                this@MaterialViewActivity,
                "Edit Material Btn Clicked....!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun deleteMaterial() {
        val reference = FirebaseDatabase.getInstance().getReference("Material")
        reference.child(branch!!).child(semester!!).child(materialId!!)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(
                    this@MaterialViewActivity,
                    "Material Deleted...!",
                    Toast.LENGTH_SHORT
                ).show()
                deleteFromFavorites()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@MaterialViewActivity,
                    "" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun deleteFromFavorites() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(FirebaseAuth.getInstance().uid!!).child("Favorites").child(materialId!!)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(
                    this@MaterialViewActivity,
                    "Deleted from Favorites also....!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { }
    }

    private fun loadMaterial() {
        val ref = FirebaseDatabase.getInstance().getReference("Material")
        ref.child(branch!!).child(semester!!).child(materialId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val materialUrl = "" + snapshot.child("url").value
                    val topic = "" + snapshot.child("topicName").value
                    binding.topicTv.text = topic
                    loadMaterialFromUrl(materialUrl)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadMaterialFromUrl(materialUrl: String) {
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(materialUrl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                binding.materialPdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange { page, pageCount ->
                        val currentPage = page + 1
                        binding.toolbarSubtitleTv.text = "$currentPage/$pageCount"
                    }
                    .onError { t ->
                        Toast.makeText(
                            this@MaterialViewActivity,
                            "" + t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .onPageError { page, t ->
                        Toast.makeText(
                            this@MaterialViewActivity,
                            "Error on page" + page + " " + t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { binding.progressBar.visibility = View.GONE }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userType = "" + snapshot.child("userType").value

//                        if (userType.equals("user")){
//                            binding.fb.setVisibility(View.GONE);
//                        }
//                        else if (userType.equals("admin")){
//                            binding.fb.setVisibility(View.VISIBLE);
//                        }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun incrementMaterialViewCount(materialId: String?) {
        val ref = FirebaseDatabase.getInstance().getReference("Material")
        ref.child(branch!!).child(semester!!).child(materialId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var viewsCount = "" + snapshot.child("viewsCount").value
                    if (viewsCount == "" || viewsCount == "null") {
                        viewsCount = "0"
                    }
                    val newViewsCount = viewsCount.toLong() + 1
                    val hashMap = HashMap<String, Any>()
                    hashMap["viewsCount"] = newViewsCount
                    val reference = FirebaseDatabase.getInstance().getReference("Material")
                    reference.child(branch!!).child(semester!!).child(materialId)
                        .updateChildren(hashMap)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}