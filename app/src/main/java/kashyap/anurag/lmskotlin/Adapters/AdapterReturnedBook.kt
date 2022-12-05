package kashyap.anurag.lmskotlin.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Models.ModelReturnedBook
import kashyap.anurag.lmskotlin.databinding.RowIssuedBooksBinding

class AdapterReturnedBook : RecyclerView.Adapter<AdapterReturnedBook.HolderReturnedBook> {

    private val context: Context
    public var returnedBookArrayList: ArrayList<ModelReturnedBook>

    private lateinit var binding: RowIssuedBooksBinding

    constructor(context: Context, returnedBookArrayList: ArrayList<ModelReturnedBook>) : super() {
        this.context = context
        this.returnedBookArrayList = returnedBookArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterReturnedBook.HolderReturnedBook {
        binding = RowIssuedBooksBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderReturnedBook(binding.root)
    }

    override fun onBindViewHolder(holder: AdapterReturnedBook.HolderReturnedBook, position: Int) {
        val model = returnedBookArrayList[position]
        val uid = model.uid
        val returnDate = model.returnDate
        val timestamp = model.timestamp

        binding.yd.setText("Returned Date:")

        binding.issuedDateTv.setText(returnDate)
        loadBooksDetails(holder, timestamp)
    }

    private fun loadBooksDetails(holder: AdapterReturnedBook.HolderReturnedBook, timestamp: String) {
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


    override fun getItemCount(): Int {
        return returnedBookArrayList.size
    }

    inner class  HolderReturnedBook(itemView: View): RecyclerView.ViewHolder(itemView){

        var subjectNameTv: TextView = binding.subjectNameTv
        var bookNameTv: TextView = binding.bookNameTv
        var bookId: TextView = binding.bookId
        var AuthorNameTv: TextView = binding.AuthorNameTv
        var issuedDateTv: TextView = binding.issuedDateTv
        var yd: TextView = binding.yd

    }
}