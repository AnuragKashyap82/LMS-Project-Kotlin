package kashyap.anurag.lmskotlin.Adapters

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kashyap.anurag.lmskotlin.BookViewActivity
import kashyap.anurag.lmskotlin.Models.ModelBooks
import kashyap.anurag.lmskotlin.databinding.RowBooksItemsBinding
import java.util.*

class AdapterBooks: RecyclerView.Adapter<AdapterBooks.HolderBooks> {

    private val context: Context
    public var booksArrayList: ArrayList<ModelBooks>

    private lateinit var binding: RowBooksItemsBinding

    constructor(context: Context, booksArrayList: ArrayList<ModelBooks>) : super() {
        this.context = context
        this.booksArrayList = booksArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterBooks.HolderBooks {
        binding = RowBooksItemsBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderBooks(binding.root)
    }

    override fun onBindViewHolder(holder: AdapterBooks.HolderBooks, position: Int) {
        val model = booksArrayList[position]
        val bookId = model.bookId
        val bookName = model.bookName
        val authorName = model.authorName
        val subjectName = model.subjectName
        val timestamp = model.timestamp

        binding.bookNameTv.setText(bookName)
        binding.subjectNameTv.setText(subjectName)
        binding.AuthorNameTv.setText(authorName)
        binding.bookId.setText("Book No "+bookId)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, BookViewActivity::class.java)
            intent.putExtra("timestamp", "" + timestamp)
            context.startActivity(intent)
        }

    }


    override fun getItemCount(): Int {
        return booksArrayList.size
    }

    inner class  HolderBooks(itemView: View): RecyclerView.ViewHolder(itemView){

        var subjectNameTv: TextView = binding.subjectNameTv
        var bookNameTv: TextView = binding.bookNameTv
        var bookId: TextView = binding.bookId
        var AuthorNameTv: TextView = binding.AuthorNameTv

    }

}