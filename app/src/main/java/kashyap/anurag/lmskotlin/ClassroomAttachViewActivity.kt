package kashyap.anurag.lmskotlin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import kashyap.anurag.lmskotlin.Constants.Companion.MAX_BYTES_PDF
import kashyap.anurag.lmskotlin.databinding.ActivityClassroomAttachViewBinding

class ClassroomAttachViewActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityClassroomAttachViewBinding
    private var classCode: String? = null
    private var timestamp: String? = null
    private var classMsg: String? = null
    private var url: String? = null
    private var uid: String? = null

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassroomAttachViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        classCode = intent.getStringExtra("classCode")
        timestamp = intent.getStringExtra("timestamp")
        classMsg = intent.getStringExtra("classMsg")
        url = intent.getStringExtra("url")
        uid = intent.getStringExtra("uid")

        loadAttachmentFromUrl(url!!)
    }

    private fun loadAttachmentFromUrl(url: String) {
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(url)
        reference.getBytes(MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                binding.attachmentPdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange { page, pageCount ->
                        val currentPage = page + 1
                        binding.toolbarSubtitleTv.text = "$currentPage/$pageCount"
                    }
                    .onError { t ->
                        Toast.makeText(
                            this@ClassroomAttachViewActivity,
                            "" + t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .onPageError { page, t ->
                        Toast.makeText(
                            this@ClassroomAttachViewActivity,
                            "Error on page" + page + " " + t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { binding.progressBar.visibility = View.GONE }
    }
}