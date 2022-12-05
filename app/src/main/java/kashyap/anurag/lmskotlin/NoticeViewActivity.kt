package kashyap.anurag.lmskotlin

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kashyap.anurag.lmskotlin.Constants.Companion.MAX_BYTES_PDF
import kashyap.anurag.lmskotlin.databinding.ActivityNoticeViewBinding
import java.io.FileOutputStream

class NoticeViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoticeViewBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var noticeId: String? = ""
    private var noticeUrl: String? = ""
    private var title: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        noticeId = intent.getStringExtra("noticeId")
        noticeUrl = intent.getStringExtra("noticeUrl")
        title = intent.getStringExtra("title")

        loadNoticeDetails()

        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.deleteNoticeBtn.setOnClickListener {
           showDeleteDialog()
        }

        binding.downloadNoticeBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this@NoticeViewActivity)
            builder.setTitle("Download")
                .setMessage("Do you want to Download Notice: $title ?")
                .setPositiveButton(
                    "Yes"
                ) { dialogInterface, i ->
                    if (ContextCompat.checkSelfPermission(
                            this@NoticeViewActivity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        downloadBook(
                            this@NoticeViewActivity,
                            "" + noticeId,
                            "" + title,
                            "" + noticeUrl
                        )
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
                .setNegativeButton(
                    "No"
                ) { dialogInterface, i -> dialogInterface.dismiss() }
                .show()
        }
    }

    private fun showDeleteDialog() {
        val builder = AlertDialog.Builder(this@NoticeViewActivity)
        builder.setTitle("Delete")
            .setMessage("Are you sure want to Delete Notice: $title ?")
            .setPositiveButton(
                "Yes"
            ) { dialogInterface, i -> deleteNotice() }
            .setNegativeButton(
                "No"
            ) { dialogInterface, i -> dialogInterface.dismiss() }
            .show()
    }

    private fun deleteNotice() {
        val documentReference = firebaseFirestore.collection("Notice").document(noticeId!!)
        documentReference.delete()
            .addOnSuccessListener {
                Toast.makeText(
                    this@NoticeViewActivity,
                    "Notice Deleted...!",
                    Toast.LENGTH_SHORT
                ).show()

            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@NoticeViewActivity,
                    "" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loadNoticeDetails() {
        val documentReference = firebaseFirestore.collection("Notice").document(
            noticeId!!
        )
        documentReference.addSnapshotListener(
            this
        ) { value, error ->
            val noticeUrl = "" + value!!.getString("url")
            val noticeTitle = "" + value.getString("title")
            binding.noticeTitleTv.text = noticeTitle
            loadNoticeFromUrl(noticeUrl)
        }
    }

    private fun loadNoticeFromUrl(pdfUrl: String) {
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange { page, pageCount ->
                        val currentPage = page + 1
                        binding.toolbarSubtitleTv.text = "$currentPage/$pageCount"
                    }
                    .onError { t ->
                        Toast.makeText(
                            this@NoticeViewActivity,
                            "" + t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .onPageError { page, t ->
                        Toast.makeText(
                            this@NoticeViewActivity,
                            "Error on page" + page + " " + t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { binding.progressBar.visibility = View.GONE }
    }

    private val requestPermissionLauncher = registerForActivityResult<String, Boolean>(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            downloadBook(this@NoticeViewActivity, "" + noticeId, "" + title, "" + noticeUrl)
        } else {
            Toast.makeText(this, "Permission was denied...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadBook(context: Context, noticeId: String, title: String, noticeUrl: String?) {
        val nameWithExtension = "$title.pdf"
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Please Wait")
        progressDialog.setMessage("Downloading$nameWithExtension....")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(noticeUrl!!)
        storageReference.getBytes(MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                saveDownloadedBook(
                    context,
                    progressDialog,
                    bytes,
                    nameWithExtension,
                    noticeId,
                    title
                )
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(context, "Failed to download due to" + e.message, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun saveDownloadedBook(
        context: Context,
        progressDialog: ProgressDialog,
        bytes: ByteArray,
        nameWithExtension: String,
        noticeId: String,
        title: String
    ) {
        try {
            val downloadsFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdir()
            val filePath = downloadsFolder.path + "/" + nameWithExtension
            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()
            Toast.makeText(context, "Saved to Download Folder", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Failed saving to download folder due to" + e.message,
                Toast.LENGTH_SHORT
            ).show()
            progressDialog.dismiss()
        }
    }
}