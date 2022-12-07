package kashyap.anurag.lmskotlin

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kashyap.anurag.lmskotlin.Adapters.AdapterAssignment
import kashyap.anurag.lmskotlin.Adapters.AdapterComment
import kashyap.anurag.lmskotlin.Constants.Companion.MAX_BYTES_PDF
import kashyap.anurag.lmskotlin.Models.ModelAssignment
import kashyap.anurag.lmskotlin.Models.ModelComment
import kashyap.anurag.lmskotlin.databinding.ActivityMaterialDetailsBinding
import kashyap.anurag.lmskotlin.databinding.DialogCommentAddBinding
import java.io.FileOutputStream
import java.util.*

class MaterialDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMaterialDetailsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    var materialId: String? = null
    var materialUrl: String? = null
    var branch: String? = null
    var semester: String? = null
    var topic: String? = null
    var subject: String? = null
    var isInMyFavorite: Boolean? = false
    private lateinit var commentArrayList: ArrayList<ModelComment>
    private lateinit var adapterComment: AdapterComment

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaterialDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        materialId = intent.getStringExtra("materialId")
        materialUrl = intent.getStringExtra("url")
        branch = intent.getStringExtra("branch")
        semester = intent.getStringExtra("semester")
        topic = intent.getStringExtra("topicName")
        subject = intent.getStringExtra("subjectName")

        binding.downloadMaterialBtn.visibility = View.GONE


        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Please wait")
        progressDialog!!.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        loadMaterialDetails()
        loadComments()
        checkIsFavorite()

        binding.backBtn.setOnClickListener { onBackPressed() }

        binding.readMaterialBtn.setOnClickListener {
            val intent = Intent(this@MaterialDetailsActivity, MaterialViewActivity::class.java)
            intent.putExtra("materialId", materialId)
            intent.putExtra("materialUrl", materialUrl)
            intent.putExtra("branch", branch)
            intent.putExtra("semester", semester)
            intent.putExtra("topicName", topic)
            startActivity(intent)
        }
        binding.downloadMaterialBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this@MaterialDetailsActivity)
            builder.setTitle("Download")
                .setMessage("Do you want to Download Material: $topic ?")
                .setPositiveButton(
                    "Yes"
                ) { dialogInterface, i ->
                    if (ContextCompat.checkSelfPermission(
                            this@MaterialDetailsActivity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                       downloadBook(
                            this@MaterialDetailsActivity,
                            "" + materialId,
                            "" + topic,
                            "" + materialUrl,
                            "" + branch,
                            "" + semester
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

        binding.favoriteBtn.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(
                    this@MaterialDetailsActivity,
                    "You're not logged In",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (isInMyFavorite!!) {
                    val firebaseAuth = FirebaseAuth.getInstance()
                    if (firebaseAuth.currentUser == null) {
                        Toast.makeText(
                            this@MaterialDetailsActivity,
                            "You're not logged In",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val ref = FirebaseDatabase.getInstance().getReference("Users")
                        ref.child(firebaseAuth.uid!!).child("Favorites").child(materialId!!)
                            .removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@MaterialDetailsActivity,
                                    "Removed from  your favorites list....",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this@MaterialDetailsActivity,
                                    "Failed to remove from favorites due to" + e.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    val firebaseAuth = FirebaseAuth.getInstance()
                    if (firebaseAuth.currentUser == null) {
                        Toast.makeText(
                            this@MaterialDetailsActivity,
                            "You're not logged In",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val timestamp = System.currentTimeMillis()
                        val hashMap: HashMap<String, Any> = HashMap()
                        hashMap["materialId"] = "" + materialId
                        hashMap["branch"] = "" + branch
                        hashMap["semester"] = "" + semester
                        hashMap["topicName"] = "" + topic
                        hashMap["url"] = "" + materialUrl
                        hashMap["timestamp"] = "" + timestamp
                        val ref = FirebaseDatabase.getInstance().getReference("Users")
                        ref.child(firebaseAuth.uid!!).child("Favorites").child(materialId!!)
                            .setValue(hashMap)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@MaterialDetailsActivity,
                                    "Added to your favorites list....",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this@MaterialDetailsActivity,
                                    "Failed to add to favorites due to" + e.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            }
        }

        binding.addCommentBtn.setOnClickListener { addCommentDialog() }

    }

    private  var comment: String? = null

    private fun addCommentDialog() {
        val commentAddBinding: DialogCommentAddBinding =
            DialogCommentAddBinding.inflate(LayoutInflater.from(this))
        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setView(commentAddBinding.getRoot())
        val alertDialog = builder.create()
        alertDialog.show()
        commentAddBinding.backBtn.setOnClickListener(View.OnClickListener { alertDialog.dismiss() })
        commentAddBinding.submitBtn.setOnClickListener(View.OnClickListener {
            comment = commentAddBinding.commentEt.getText().toString().trim()
            if (TextUtils.isEmpty(comment)) {
                Toast.makeText(
                    this@MaterialDetailsActivity,
                    "Enter your comment....",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                alertDialog.dismiss()
                addComment()
            }
        })
    }

    private fun addComment() {
        progressDialog!!.setMessage("Adding comment")
        progressDialog!!.show()
        val timestamp = "" + System.currentTimeMillis()
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["commentId"] = "" + firebaseAuth.uid
        hashMap["materialId"] = "" + materialId
        hashMap["timestamp"] = "" + timestamp
        hashMap["comment"] = "" + comment
        hashMap["uid"] = "" + firebaseAuth.uid
        val ref = FirebaseDatabase.getInstance().getReference("Material")
        ref.child(branch!!).child(semester!!).child(materialId!!).child("Comments")
            .child(firebaseAuth.uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(
                    this@MaterialDetailsActivity,
                    "Comment Added....",
                    Toast.LENGTH_SHORT
                ).show()
                progressDialog!!.dismiss()
            }
            .addOnFailureListener { e ->
                progressDialog!!.dismiss()
                Toast.makeText(
                    this@MaterialDetailsActivity,
                    "Failed to add comment due to" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private val requestPermissionLauncher = registerForActivityResult<String, Boolean>(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            downloadBook(
                this@MaterialDetailsActivity,
                "" + materialId,
                "" + topic,
                "" + materialUrl,
                "" + branch,
                "" + semester
            )
        } else {
            Toast.makeText(this, "Permission was denied...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMaterialDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Material")
        ref.child(branch!!).child(semester!!).child(materialId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    materialId = "" + snapshot.child("materialId").value
                    val subject = "" + snapshot.child("subjectName").value
                    val topic = "" + snapshot.child("topicName").value
                    val branch = "" + snapshot.child("branch").value
                    val semester = "" + snapshot.child("semester").value
                    val viewsCount = "" + snapshot.child("viewsCount").value
                    val downloadsCount = "" + snapshot.child("downloadsCount").value
                    val timestamp = "" + snapshot.child("timestamp").value
                    val url = "" + snapshot.child("url").value
                    binding.downloadMaterialBtn.visibility = View.VISIBLE
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = timestamp.toLong()
                    val dateFormat = DateFormat.format("dd/MM/yyyy", calendar).toString()
                    val ref = FirebaseStorage.getInstance().getReferenceFromUrl(url)
                    ref.metadata
                        .addOnSuccessListener { storageMetadata ->
                            val bytes = storageMetadata.sizeBytes.toDouble()
                            val kb = bytes / 1024
                            val mb = kb / 1024
                            if (mb >= 1) {
                                binding.sizeTv.text = String.format("%.2f", mb) + " MB"
                            } else if (kb >= 1) {
                                binding.sizeTv.text = String.format("%.2f", kb) + " KB"
                            } else {
                                binding.sizeTv.text = String.format("%.2f", bytes) + " bytes"
                            }
                        }
                        .addOnFailureListener { }
                    binding.subjectTv.text = subject
                    binding.topicTv.text = topic
                    binding.viewsTv.text = viewsCount.replace("null", "N/A")
                    binding.downloadsTv.text = downloadsCount.replace("null", "N/A")
                    binding.dateTv.text = dateFormat
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadComments() {
        commentArrayList = ArrayList<ModelComment>()
        val reference = FirebaseDatabase.getInstance().getReference("Material")
        reference.child(branch!!).child(semester!!).child(materialId!!).child("Comments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentArrayList.clear()
                    for (ds in snapshot.children) {
                        val model: ModelComment? = ds.getValue(ModelComment::class.java)
                        commentArrayList.add(model!!)
                    }
                    adapterComment = AdapterComment(this@MaterialDetailsActivity, commentArrayList)
                    binding.commentsRv.adapter = adapterComment
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun downloadBook(
        context: Context?,
        materialId: String?,
        topic: String,
        materialUrl: String?,
        branch: String?,
        semester: String?
    ) {
        val nameWithExtension = "$topic.pdf"
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Please Wait")
        progressDialog.setMessage("Downloading$nameWithExtension....")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(materialUrl!!)
        storageReference.getBytes(MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                saveDownloadedBook(
                    context!!,
                    progressDialog,
                    bytes,
                    nameWithExtension,
                    materialId!!,
                    branch!!,
                    semester!!
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
        materialId: String,
        branch: String,
        semester: String
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
            incrementBookDownloadCount(materialId, branch, semester)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Failed saving to download folder due to" + e.message,
                Toast.LENGTH_SHORT
            ).show()
            progressDialog.dismiss()
        }
    }

    private fun incrementBookDownloadCount(materialId: String, branch: String, semester: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Material")
        ref.child(branch).child(semester).child(materialId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var downloadsCount = "" + snapshot.child("downloadsCount").value
                    if (downloadsCount == "" || downloadsCount == "null") {
                        downloadsCount = "0"
                    }
                    val newDownloadsCount = downloadsCount.toLong() + 1
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["downloadsCount"] = newDownloadsCount
                    val reference = FirebaseDatabase.getInstance().getReference("Material")
                    reference.child(branch).child(semester).child(materialId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener { }
                        .addOnFailureListener { }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun checkIsFavorite() {
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!).child("Favorites").child(materialId!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if (isInMyFavorite!!) {
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite_white,
                            0,
                            0
                        )
                        binding.favoriteBtn.text = "Remove Favorite"
                    } else {
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_favorite_border_white,
                            0,
                            0
                        )
                        binding.favoriteBtn.text = "Add Favorite"
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}