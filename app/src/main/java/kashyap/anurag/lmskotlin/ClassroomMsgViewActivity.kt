package kashyap.anurag.lmskotlin

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kashyap.anurag.lmskotlin.databinding.ActivityClassroomMsgViewBinding
import java.util.*

class ClassroomMsgViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClassroomMsgViewBinding
    private var classCode: String? = null
    private var classMsg: String? = null
    private var timestamp: String? = null
    private var url: String? = null
    private var uid: String? = null
    private var msgCode: String? = null
    private var isAttachmentExist: Boolean? = false
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassroomMsgViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        classCode = intent.getStringExtra("classCode")
        classMsg = intent.getStringExtra("classMsg")
        timestamp = intent.getStringExtra("timestamp")
        url = intent.getStringExtra("url")
        uid = intent.getStringExtra("uid")
        isAttachmentExist = intent.getBooleanExtra("isAttachmentExist", isAttachmentExist!!)
        msgCode = intent.getStringExtra("msgCode")

        firebaseFirestore = FirebaseFirestore.getInstance()

        loadMsgSenderDetails()
        checkAttachmentExists()

        binding.msgTextTv.text = classMsg

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp!!.toLong()
        val dateFormat = DateFormat.format("dd/MM/yyyy  K:mm a", calendar).toString()
        binding.dateTv.text = dateFormat

        binding.attachPdfBtn.setOnClickListener {
            val intent =
                Intent(this@ClassroomMsgViewActivity, ClassroomAttachViewActivity::class.java)
            intent.putExtra("classCode", classCode)
            intent.putExtra("timestamp", timestamp)
            intent.putExtra("classMsg", classMsg)
            intent.putExtra("url", url)
            intent.putExtra("uid", uid)
            startActivity(intent)
        }
    }

    private fun checkAttachmentExists() {
        if (isAttachmentExist!!) {
            binding.attachmentTv.visibility = View.VISIBLE
            binding.attachPdfBtn.visibility = View.VISIBLE
        } else {
            binding.attachmentTv.visibility = View.GONE
            binding.attachPdfBtn.visibility = View.GONE
        }
    }

    private fun loadMsgSenderDetails() {
        val documentReference = firebaseFirestore.collection("Users").document(
            uid!!
        )
        documentReference.addSnapshotListener(
            this
        ) { ds, error ->
            val name = "" + ds!!.getString("name")
            val profileImage = "" + ds.getString("profileImage")
            binding.nameTv.text = name
            try {
                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray)
                    .into(binding.profileIv)
            } catch (e: Exception) {
                binding.profileIv.setImageResource(R.drawable.ic_person_gray)
            }
        }
    }
}