package kashyap.anurag.lmskotlin.Adapters

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kashyap.anurag.lmskotlin.MaterialDetailsActivity
import kashyap.anurag.lmskotlin.Models.ModelMaterial
import kashyap.anurag.lmskotlin.databinding.RowMaterialBinding
import java.util.*

class AdapterMaterial : RecyclerView.Adapter<AdapterMaterial.HolderMaterial> {

    private val context: Context
    public var materialArrayList: ArrayList<ModelMaterial>

    private lateinit var binding: RowMaterialBinding

    constructor(context: Context, materialArrayList: ArrayList<ModelMaterial>) : super() {
        this.context = context
        this.materialArrayList = materialArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterMaterial.HolderMaterial {
        binding = RowMaterialBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderMaterial(binding.root)
    }

    override fun onBindViewHolder(holder: AdapterMaterial.HolderMaterial, position: Int) {
        val model = materialArrayList[position]
        val materialId = model.materialId
        val branch = model.branch
        val semester = model.semester
        val date = model.timestamp
        val url = model.url
        val subject = model.subjectName
        val topic = model.topicName

        holder.subjectNameTv.setText(subject)
        holder.topicTv.setText(topic)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date.toLong()
        val dateFormat = DateFormat.format("dd/MM/yyyy", calendar).toString()

        holder.dateTv.text = dateFormat

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MaterialDetailsActivity::class.java)
            intent.putExtra("materialId", materialId)
            intent.putExtra("url", url)
            intent.putExtra("branch", branch)
            intent.putExtra("semester", semester)
            intent.putExtra("topicName", topic)
            intent.putExtra("subjectName", subject)
            context.startActivity(intent)
        }

    }
    override fun getItemCount(): Int {
        return materialArrayList.size
    }

    inner class  HolderMaterial(itemView: View): RecyclerView.ViewHolder(itemView){

        var subjectNameTv: TextView = binding.subjectNameTv
        var topicTv: TextView = binding.topicTv
        var dateTv: TextView = binding.dateTv
    }
}