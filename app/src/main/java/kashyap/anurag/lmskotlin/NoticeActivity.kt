package kashyap.anurag.lmskotlin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Adapters.AdapterNotice
import kashyap.anurag.lmskotlin.Models.ModelNotice
import kashyap.anurag.lmskotlin.databinding.ActivityNoticeBinding
import java.util.*

class NoticeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoticeBinding
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var noticeList: ArrayList<ModelNotice>
    private lateinit var adapterNotice: AdapterNotice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        loadAllNotice()

        binding.addNoticeBtn.setOnClickListener {
            startActivity(Intent(this, AddNoticeActivity::class.java))
        }
    }
    private fun loadAllNotice() {
        noticeList = ArrayList()
        FirebaseFirestore.getInstance().collection("Notice")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val modelNotice = document.toObject(ModelNotice::class.java)
                        noticeList.add(modelNotice)
                    }
//                    Collections.sort(noticeList, object : Comparator<ModelNotice?> {
//                        fun compare(t1: ModelNotice, t2: ModelNotice): Int {
//                            return t1.timestamp.compareToIgnoreCase(t2.timestamp)
//                        }
//                    })
//                    Collections.reverse(noticeList)
                    val layoutManager =
                        LinearLayoutManager(
                            this@NoticeActivity,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                    binding.noticeRv.layoutManager = layoutManager
                    binding.noticeRv.layoutManager = LinearLayoutManager(this@NoticeActivity)
                    adapterNotice = AdapterNotice(this@NoticeActivity, noticeList)
                    binding.noticeRv.adapter = adapterNotice
                }
            }
    }
}