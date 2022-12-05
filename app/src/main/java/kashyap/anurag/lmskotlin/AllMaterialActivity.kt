package kashyap.anurag.lmskotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kashyap.anurag.lmskotlin.Adapters.AdapterMaterial
import kashyap.anurag.lmskotlin.Models.ModelMaterial
import kashyap.anurag.lmskotlin.databinding.ActivityAllMaterialBinding
import java.util.*

class AllMaterialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllMaterialBinding
    private var branch: String? = null
    private var semester: String? = null
    private var firebaseAuth: FirebaseAuth? = null
    private lateinit var materialList: ArrayList<ModelMaterial>
    private lateinit var adapterMaterial: AdapterMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllMaterialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        branch = intent.getStringExtra("branch")
        semester = intent.getStringExtra("semester")

        binding.semesterTv.text = semester
        binding.branchTv.text = branch

        firebaseAuth = FirebaseAuth.getInstance()
        loadAllMaterial()
    }

    private fun loadAllMaterial() {
        materialList = ArrayList()
        val reference = FirebaseDatabase.getInstance().getReference("Material")
        reference.child(branch!!).child(semester!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    materialList.clear()
                    for (ds in snapshot.children) {
                        val modelMaterial = ds.getValue(ModelMaterial::class.java)
                        materialList.add(modelMaterial!!)
                    }
//                    Collections.sort(materialList, object : Comparator<ModelMaterial?> {
//                        fun compare(t1: ModelMaterial, t2: ModelMaterial): Int {
//                            return t1.getTimestamp().compareToIgnoreCase(t2.getTimestamp())
//                        }
//                    })
//                    Collections.reverse(materialList)
                    val layoutManager = LinearLayoutManager(
                        this@AllMaterialActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    binding.materialRv.layoutManager = layoutManager
                    binding.materialRv.layoutManager = LinearLayoutManager(this@AllMaterialActivity)
                    adapterMaterial = AdapterMaterial(this@AllMaterialActivity, materialList)
                    binding.materialRv.adapter = adapterMaterial
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}