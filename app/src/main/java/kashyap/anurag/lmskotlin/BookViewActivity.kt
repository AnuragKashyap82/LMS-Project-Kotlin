package kashyap.anurag.lmskotlin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.databinding.ActivityBookViewBinding
import java.util.*

class BookViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookViewBinding
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private var timestamp: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        timestamp = intent.getStringExtra("timestamp")
        loadBookDetails(timestamp!!)
        loadNoQuantityCount(timestamp!!)

        binding.issueBookBtn.setOnClickListener {
            issueBook(timestamp!!)
        }
    }

    override fun onStart() {
        super.onStart()
        checkAlreadyIssued(timestamp!!)
        loadNoQuantityCount(timestamp!!)
    }

    private fun checkAlreadyIssued(timestamp: String) {
        val documentReference =
            FirebaseFirestore.getInstance().collection("issuedApplied").document(
                firebaseAuth.uid!!
            )
        documentReference.collection("Books").document(timestamp)
            .addSnapshotListener { snapshot, error ->
                if (snapshot!!.exists()) {
                    binding.issueBookBtn.isEnabled = false
                    binding.issueBookBtn.text = "Already Issued"
                } else {
                    checkAlreadyIssuedDone(timestamp)
                }
            }
    }
    private fun checkAlreadyIssuedDone(timestamp: String) {
        val documentReference = FirebaseFirestore.getInstance().collection("issuedBooks").document(
            firebaseAuth.uid!!
        )
        documentReference.collection("Books").document(timestamp)
            .addSnapshotListener { snapshot, error ->
                if (snapshot!!.exists()) {
                    binding.issueBookBtn.isEnabled = false
                    binding.issueBookBtn.text = "Already Issued"
                } else {
                    checkAlreadyIssuedDone(timestamp)
                }
            }
    }

    private fun loadBookDetails(timestamp: String) {
        binding.progressBar.visibility = View.VISIBLE
        val documentReference =
            FirebaseFirestore.getInstance().collection("Books").document(timestamp)
        documentReference.addSnapshotListener { snapshot, error ->
            val subjectName = snapshot!!["subjectName"].toString()
            val bookName = snapshot["bookName"].toString()
            val authorName = snapshot["authorName"].toString()
            val bookId = snapshot["bookId"].toString()
            binding.subjectNameTv.text = subjectName
            binding.bookNameTv.text = bookName
            binding.authorNameTv.text = authorName
            binding.bookNoTv.text = bookId
        }
    }

    private fun loadNoQuantityCount(timestamp: String) {
        val databaseReference =
            FirebaseDatabase.getInstance().getReference("Books").child("" + timestamp)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val availableQuantity = snapshot.child("booksQuantity").value.toString()
                    binding.quantityTv.text = "" + availableQuantity
                    binding.progressBar.visibility = View.GONE
                    if (availableQuantity == "0") {
                        binding.issueBookBtn.isEnabled = false
                        binding.issueBookBtn.text = "Not Available"
                    } else {
                    }
                } else {
                    binding.progressBar.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun issueBook(timestamp: String) {
        binding.issueBookBtn.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val currentYear = calendar[Calendar.YEAR]
        val currentMonth = calendar[Calendar.MONTH] + 1
        val currentDay = calendar[Calendar.DAY_OF_MONTH]
        val date = "$currentDay-$currentMonth-$currentYear"

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["timestamp"] = "" + timestamp
        hashMap["appliedDate"] = "" + date
        hashMap["uid"] = "" + firebaseAuth.uid
        val documentReference =
            FirebaseFirestore.getInstance().collection("issuedApplied").document(
                firebaseAuth.uid!!
            )
        documentReference.collection("Books").document(timestamp)
            .set(hashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val hashMap1: HashMap<String, Any> = HashMap()
                    hashMap1["uid"] = "" + firebaseAuth.uid
                    val documentReference1 =
                        FirebaseFirestore.getInstance().collection("issuedApplied").document(
                            firebaseAuth.uid!!
                        )
                    documentReference1.set(hashMap1)
                    decreaseBookQuantityCount(timestamp)
                } else {
                    Toast.makeText(
                        this@BookViewActivity,
                        task.exception!!.message,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    binding.issueBookBtn.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@BookViewActivity, e.message, Toast.LENGTH_SHORT).show()
                binding.issueBookBtn.visibility = View.VISIBLE
            }
    }

    private fun decreaseBookQuantityCount(timestamp: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Books")
        databaseReference.child(timestamp)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var booksQuantity = "" + snapshot.child("booksQuantity").value
                        if (booksQuantity == "" || booksQuantity == "null") {
                            booksQuantity = "0"
                        }
                        val newBooksQuantity = booksQuantity.toLong() - 1

                        val hashMap: HashMap<String, Any> = HashMap()
                        hashMap["booksQuantity"] = "" + newBooksQuantity
                        val reference = FirebaseDatabase.getInstance().getReference("Books")
                        reference.child(timestamp).updateChildren(hashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    binding.issueBookBtn.visibility = View.VISIBLE
                                    binding.progressBar.visibility = View.GONE
                                } else {
                                }
                            }
                            .addOnFailureListener { }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}