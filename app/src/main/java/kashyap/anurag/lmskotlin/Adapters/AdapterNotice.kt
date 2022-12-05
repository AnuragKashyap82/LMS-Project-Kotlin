package kashyap.anurag.lmskotlin.Adapters

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kashyap.anurag.lmskotlin.Models.ModelNotice
import kashyap.anurag.lmskotlin.NoticeViewActivity
import kashyap.anurag.lmskotlin.databinding.RowNoticeBinding
import java.util.*

class AdapterNotice : RecyclerView.Adapter<AdapterNotice.HolderNotice> {

    private val context: Context
    public var noticeArrayList: ArrayList<ModelNotice>

    private lateinit var binding: RowNoticeBinding

    constructor(context: Context, noticeArrayList: ArrayList<ModelNotice>) : super() {
        this.context = context
        this.noticeArrayList = noticeArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterNotice.HolderNotice {
        binding = RowNoticeBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderNotice(binding.root)
    }

    override fun onBindViewHolder(holder: AdapterNotice.HolderNotice, position: Int) {
        val model = noticeArrayList[position]
        val noticeId = model.NoticeId
        val title = model.title
        val number = model.number
        val noticeUrl = model.url
        val date = model.timestamp

        binding.noticeTitleTv.setText(title)
        binding.noticeNoTv.setText(number)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date.toLong()
        val dateFormat = DateFormat.format("dd/MM/yyyy", calendar).toString()

        binding.dateTv.setText(dateFormat)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, NoticeViewActivity::class.java)
            intent.putExtra("noticeId", noticeId)
            intent.putExtra("noticeUrl", noticeUrl)
            intent.putExtra("title", title)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return noticeArrayList.size
    }

    inner class  HolderNotice(itemView: View): RecyclerView.ViewHolder(itemView){

        var noticeTitleTv: TextView = binding.noticeTitleTv
        var noticeNoTv: TextView = binding.noticeNoTv
        var dateTv: TextView = binding.dateTv
    }
}