package kashyap.anurag.lmskotlin.Adapters

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Models.ModelIssuedBooks
import kashyap.anurag.lmskotlin.R
import kashyap.anurag.lmskotlin.databinding.RowIssuedBooksBinding
import java.util.*

class AdapterIssuedBook : RecyclerView.Adapter<AdapterIssuedBook.HolderIssuedBooks> {

    private val context: Context
    public var issuedBookArrayList: ArrayList<ModelIssuedBooks>
    private var ADMIN_CODE: String? = ""

    private lateinit var binding: RowIssuedBooksBinding

    constructor(
        context: Context,
        issuedBookArrayList: ArrayList<ModelIssuedBooks>,
        ADMIN_CODE: String?
    ) : super() {
        this.context = context
        this.issuedBookArrayList = issuedBookArrayList
        this.ADMIN_CODE = ADMIN_CODE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterIssuedBook.HolderIssuedBooks {
        binding = RowIssuedBooksBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderIssuedBooks(binding.root)
    }

    override fun onBindViewHolder(holder: AdapterIssuedBook.HolderIssuedBooks, position: Int) {
        val model = issuedBookArrayList[position]
        val uid = model.uid
        val issuedDate = model.issueDate
        val timestamp = model.timestamp

        binding.issuedDateTv.setText(issuedDate)
        loadBookDetails(holder, timestamp, binding)

        if (ADMIN_CODE == "ADMIN") {
            holder.itemView.setOnClickListener {
                showReturnBookDialog(timestamp, uid)
            }
        } else if (ADMIN_CODE == "USER") {
        }
    }

    private fun loadBookDetails(
        holder: HolderIssuedBooks,
        timestamp: String,
        binding: RowIssuedBooksBinding
    ) {
        val documentReference =
            FirebaseFirestore.getInstance().collection("Books").document(timestamp)
        documentReference.addSnapshotListener { snapshot, error ->
            val subjectName = snapshot!!["subjectName"].toString()
            val bookName = snapshot!!["bookName"].toString()
            val authorName = snapshot!!["authorName"].toString()
            val bookId = snapshot!!["bookId"].toString()

            binding.subjectNameTv.setText(subjectName)
            holder.bookNameTv.setText(bookName)
            holder.AuthorNameTv.setText(authorName)
            holder.bookId.setText("Book No:"+bookId)
        }
    }

    private fun showReturnBookDialog(timestamp: String, uid: String) {
        val myDialog = AlertDialog.Builder(context, R.style.BottomSheetStyle)
        val inflater = LayoutInflater.from(context)
        val view1: View = inflater.inflate(R.layout.confirm_issue_layout, null)
        myDialog.setView(view1)
        val dialog = myDialog.create()
        dialog.setCancelable(false)
        val cancelBtn = view1.findViewById<Button>(R.id.cancelBtn)
        val issueBookBtn = view1.findViewById<Button>(R.id.issueBookBtn)
        val bookNameTv = view1.findViewById<TextView>(R.id.bookNameTv)
        val tv3 = view1.findViewById<TextView>(R.id.tv3)
        val bookIdTv = view1.findViewById<TextView>(R.id.bookIdTv)
        val progressBar = view1.findViewById<ProgressBar>(R.id.progressBar)
        tv3.text = "Return Book"
        val documentReference =
            FirebaseFirestore.getInstance().collection("Books").document(timestamp)
        documentReference.addSnapshotListener { snapshot, error ->
            val subjectName = snapshot!!["subjectName"].toString()
            val bookName = snapshot["bookName"].toString()
            val authorName = snapshot["authorName"].toString()
            val bookId = snapshot["bookId"].toString()
            bookNameTv.text = bookName
            bookIdTv.text = bookId
        }
        cancelBtn.setOnClickListener { dialog.dismiss() }
        issueBookBtn.setOnClickListener {
            returnBook(timestamp, uid, progressBar, dialog) }
        dialog.show()
    }

    private fun returnBook(
        timestamp: String,
        uid: String,
        progressBar: ProgressBar,
        dialog: AlertDialog
    ) {
        progressBar.visibility = View.VISIBLE
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val currentYear = calendar[Calendar.YEAR]
        val currentMonth = calendar[Calendar.MONTH] + 1
        val currentDay = calendar[Calendar.DAY_OF_MONTH]
        val date = "$currentDay-$currentMonth-$currentYear"
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["timestamp"] = "" + timestamp
        hashMap["returnDate"] = "" + date
        hashMap["uid"] = "" + uid
        val documentReference =
            FirebaseFirestore.getInstance().collection("returnedBooks").document(uid)
        documentReference.collection("Books").document(timestamp)
            .set(hashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val hashMap1 = HashMap<String, Any>()
                    hashMap1["uid"] = "" + uid
                    val documentReference1 =
                        FirebaseFirestore.getInstance().collection("returnedBooks").document(uid)
                    documentReference1.set(hashMap1)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Handler().postDelayed({
                                    deleteIssuedApplied(
                                        timestamp,
                                        uid,
                                        progressBar,
                                        dialog
                                    )
                                }, 2000)
                            } else {
                            }
                        }
                        .addOnFailureListener { }
                } else {
                    Toast.makeText(context, task.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteIssuedApplied(
        timestamp: String,
        uid: String,
        progressBar: ProgressBar,
        dialog: AlertDialog
    ) {
        val documentReference =
            FirebaseFirestore.getInstance().collection("issuedBooks").document(uid)
        documentReference.collection("Books").document(timestamp).delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkAllBookIssued(uid, dialog, progressBar)
                } else {
                    Toast.makeText(context, task.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkAllBookIssued(uid: String, dialog: AlertDialog, progressBar: ProgressBar) {
        val documentReference =
            FirebaseFirestore.getInstance().collection("issuedBooks").document(uid)
        documentReference.collection("Books")
            .addSnapshotListener { snapshot, error ->
                if (snapshot!!.isEmpty) {
                    val documentReference1 =
                        FirebaseFirestore.getInstance().collection("issuedBooks").document(uid)
                        documentReference1.delete().addOnCompleteListener {
                        progressBar.visibility = View.GONE
                        dialog.dismiss()
                    }
                        .addOnFailureListener { }
                } else {
                    Toast.makeText(context, "Snapshot exists!!!!", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    dialog.dismiss()
                }
            }
    }

    override fun getItemCount(): Int {
        return issuedBookArrayList.size
    }

    inner class  HolderIssuedBooks(itemView: View): RecyclerView.ViewHolder(itemView){

        var subjectNameTv: TextView = binding.subjectNameTv
        var bookNameTv: TextView = binding.bookNameTv
        var bookId: TextView = binding.bookId
        var AuthorNameTv: TextView = binding.AuthorNameTv
        var issuedDateTv: TextView = binding.issuedDateTv

    }

}