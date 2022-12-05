package kashyap.anurag.lmskotlin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.databinding.ActivityAddBooksBinding

class AddBooksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBooksBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var subjectName: String? = ""
    private var bookName: String? = ""
    private var authorName: String? = ""
    private var bookId: String? = ""
    private var bookQuantity: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.uploadBookBtn.setOnClickListener { validateData() }

    }

    private fun validateData() {
        subjectName = binding.subjectEt.text.toString().trim()
        bookName = binding.bookNameEt.text.toString().trim()
        authorName = binding.authorNameEt.text.toString().trim()
        bookId = binding.bookNoEt.text.toString().trim()
        bookQuantity = binding.booksQuantityEt.text.toString().trim()
        if (subjectName!!.isEmpty()) {
            Toast.makeText(this, "Enter Subject Name!!!", Toast.LENGTH_SHORT).show()
        } else if (bookName!!.isEmpty()) {
            Toast.makeText(this, "Enter Book Name!!!", Toast.LENGTH_SHORT).show()
        } else if (authorName!!.isEmpty()) {
            Toast.makeText(this, "Enter Author Name!!!", Toast.LENGTH_SHORT).show()
        } else if (bookId!!.isEmpty()) {
            Toast.makeText(this, "Enter Book Id/Book No.!!!", Toast.LENGTH_SHORT).show()
        } else if (bookQuantity!!.isEmpty()) {
            Toast.makeText(this, "Enter Books Quantity!!!", Toast.LENGTH_SHORT).show()
        } else {
            uploadBookData()
        }
    }
    private fun uploadBookData() {
        binding.uploadBookBtn.visibility = View.GONE
        val timestamp = System.currentTimeMillis()
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["subjectName"] = "" + subjectName
        hashMap["bookName"] = "" + bookName
        hashMap["authorName"] = "" + authorName
        hashMap["bookId"] = "" + bookId
        hashMap["timestamp"] = "" + timestamp
        val documentReference =
            FirebaseFirestore.getInstance().collection("Books").document("" + timestamp)
        documentReference.set(hashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@AddBooksActivity,
                        "Books Uploaded Successfully!!!",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.bookNameEt.setText("")
                    binding.subjectEt.setText("")
                    binding.authorNameEt.setText("")
                    binding.bookNoEt.setText("")
                    binding.booksQuantityEt.setText("")
                    uploadBooksBooksCount(timestamp)
                } else {
                    Toast.makeText(
                        this@AddBooksActivity,
                        task.exception!!.message,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    binding.uploadBookBtn.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@AddBooksActivity, e.message, Toast.LENGTH_SHORT).show()
                binding.uploadBookBtn.visibility = View.VISIBLE
            }
    }

    private fun uploadBooksBooksCount(timestamp: Long) {
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["booksQuantity"] = bookQuantity!!
        val databaseReference =
            FirebaseDatabase.getInstance().getReference("Books").child("" + timestamp)
        databaseReference.setValue(hashMap)
        binding.uploadBookBtn.visibility = View.VISIBLE
    }
}