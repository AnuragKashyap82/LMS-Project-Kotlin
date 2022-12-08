package kashyap.anurag.lmskotlin.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Constants.Companion.joiningCode
import kashyap.anurag.lmskotlin.Models.ModelAtteStud
import kashyap.anurag.lmskotlin.databinding.RowAttendanceItemBinding
import java.util.*

class AdapterAttenStud: RecyclerView.Adapter<AdapterAttenStud.HolderAttendStud> {

    private val context: Context
    public var atteStudArrayList: ArrayList<ModelAtteStud>

    private lateinit var binding: RowAttendanceItemBinding

    constructor(context: Context, atteStudArrayList: ArrayList<ModelAtteStud>) : super() {
        this.context = context
        this.atteStudArrayList = atteStudArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterAttenStud.HolderAttendStud {
        binding = RowAttendanceItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderAttendStud(binding.root)
    }

    override fun onBindViewHolder(holder: AdapterAttenStud.HolderAttendStud, position: Int) {
        val model = atteStudArrayList[position]
        val uid = model.uid


        loadUserDetails(uid, holder, binding)
        checkForCheckBox(uid, holder)

        holder.absentBtn.setOnClickListener {
            markAttendanceAsAbsent(uid, holder)
            holder.absentBtn.setChecked(true)
            if (holder.presentBtn.isChecked()) {
                holder.presentBtn.setChecked(false)
            }
        }
        holder.presentBtn.setOnClickListener {
            markAttendanceAsPresent(uid, holder)
            holder.presentBtn.setChecked(true)
            if (holder.absentBtn.isChecked()) {
                holder.absentBtn.setChecked(false)
            }
        }

    }

    private fun markAttendanceAsAbsent(uid: String, holder: HolderAttendStud) {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val currentYear = calendar[Calendar.YEAR]
        val currentMonth = calendar[Calendar.MONTH] + 1
        val currentDay = calendar[Calendar.DAY_OF_MONTH]
        val date = "$currentDay-$currentMonth-$currentYear"
        val monthYear = "" + currentMonth + currentYear

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["date"] = "" + date
        hashMap["monthYear"] = "" + currentMonth + currentYear
        hashMap["uid"] = "" + uid
        hashMap["Attendance"] = "Absent"
        val documentReference = FirebaseFirestore.getInstance().collection("classroom")
            .document(joiningCode).collection("Attendance").document("Date").collection("" + date)
            .document(uid)
        documentReference.set(hashMap)
            .addOnSuccessListener {
                val databaseReference = FirebaseDatabase.getInstance().getReference("Classroom")
                databaseReference.child(joiningCode).child("Students").child("" + monthYear)
                    .child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                var absentCount = "" + snapshot.child("absentCount").value
                                var presentCount = "" + snapshot.child("presentCount").value
                                if (absentCount == "" || absentCount == "null") {
                                    absentCount = "0"
                                }
                                if (presentCount == "" || presentCount == "null") {
                                    presentCount = "0"
                                }
                                val newAbsentCount = absentCount.toLong() + 1
                                val hashMap: HashMap<String, Any> = HashMap()
                                hashMap["absentCount"] = "" + newAbsentCount
                                hashMap["absentCount"] = "" + presentCount
                                hashMap["monthYear"] = "" + monthYear
                                hashMap["classCode"] = "" + joiningCode
                                hashMap["uid"] = "" + uid
                                val reference =
                                    FirebaseDatabase.getInstance().getReference("Classroom")
                                reference.child(joiningCode).child("Students").child("" + monthYear)
                                    .child(uid).updateChildren(hashMap)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            checkForCheckBox(uid, holder)
                                            Toast.makeText(context, "Absent!!!", Toast.LENGTH_SHORT)
                                                .show()
                                        } else {
                                        }
                                    }
                                    .addOnFailureListener { }
                            } else {
                                val newPresentCount: Long = 1
                                val hashMap: HashMap<String, Any> = HashMap()
                                hashMap["absentCount"] = "" + newPresentCount
                                hashMap["monthYear"] = "" + monthYear
                                hashMap["classCode"] = "" + joiningCode
                                hashMap["uid"] = "" + uid
                                val reference =
                                    FirebaseDatabase.getInstance().getReference("Classroom")
                                reference.child(joiningCode).child("Students").child("" + monthYear)
                                    .child(uid).setValue(hashMap)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            checkForCheckBox(uid, holder)
                                            Toast.makeText(context, "Absent!!!", Toast.LENGTH_SHORT)
                                                .show()
                                        } else {
                                        }
                                    }
                                    .addOnFailureListener { }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
            .addOnFailureListener { }
    }

    private fun markAttendanceAsPresent(uid: String, holder: HolderAttendStud) {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val currentYear = calendar[Calendar.YEAR]
        val currentMonth = calendar[Calendar.MONTH] + 1
        val currentDay = calendar[Calendar.DAY_OF_MONTH]
        val date = "$currentDay-$currentMonth-$currentYear"
        val monthYear = "" + currentMonth + currentYear

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["date"] = "" + date
        hashMap["monthYear"] = "" + currentMonth + currentYear
        hashMap["uid"] = "" + uid
        hashMap["Attendance"] = "Present"
        val documentReference = FirebaseFirestore.getInstance().collection("classroom")
            .document(joiningCode).collection("Attendance").document("Date").collection("" + date)
            .document(uid)
        documentReference.set(hashMap)
            .addOnSuccessListener {
                val databaseReference = FirebaseDatabase.getInstance().getReference("Classroom")
                databaseReference.child(joiningCode).child("Students").child("" + monthYear)
                    .child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                var presentCount = "" + snapshot.child("presentCount").value
                                if (presentCount == "" || presentCount == "null") {
                                    presentCount = "0"
                                }
                                val newPresentCount = presentCount.toLong() + 1
                                val hashMap: HashMap<String, Any> = HashMap()
                                hashMap["presentCount"] = "" + newPresentCount
                                hashMap["monthYear"] = "" + monthYear
                                hashMap["classCode"] = "" + joiningCode
                                hashMap["uid"] = "" + uid
                                val reference =
                                    FirebaseDatabase.getInstance().getReference("Classroom")
                                reference.child(joiningCode).child("Students").child("" + monthYear)
                                    .child(uid).updateChildren(hashMap)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            checkForCheckBox(uid, holder)
                                            Toast.makeText(
                                                context,
                                                "Present!!!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                        }
                                    }
                                    .addOnFailureListener { }
                            } else {
                                val newPresentCount: Long = 1
                                val hashMap: HashMap<String, Any> = HashMap()
                                hashMap["presentCount"] = "" + newPresentCount
                                hashMap["monthYear"] = "" + monthYear
                                hashMap["classCode"] = "" + joiningCode
                                hashMap["uid"] = "" + uid
                                val reference =
                                    FirebaseDatabase.getInstance().getReference("Classroom")
                                reference.child(joiningCode).child("Students").child("" + monthYear)
                                    .child(uid).setValue(hashMap)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            checkForCheckBox(uid, holder)
                                            Toast.makeText(
                                                context,
                                                "Present!!!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                        }
                                    }
                                    .addOnFailureListener { }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
            .addOnFailureListener { }
    }

    private fun checkForCheckBox(uid: String, holder: HolderAttendStud) {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val currentYear = calendar[Calendar.YEAR]
        val currentMonth = calendar[Calendar.MONTH] + 1
        val currentDay = calendar[Calendar.DAY_OF_MONTH]
        val date = "$currentDay-$currentMonth-$currentYear"
        val documentReference = FirebaseFirestore.getInstance().collection("classroom")
            .document(joiningCode).collection("Attendance").document("Date").collection("" + date)
            .document(uid)
        documentReference.addSnapshotListener { snapshot, error ->
            if (snapshot!!.exists()) {
                val attendance = snapshot.getString("Attendance")
                if (attendance == "Present") {
                    holder.presentBtn.setChecked(true)
                    if (holder.absentBtn.isChecked()) {
                        holder.absentBtn.setChecked(false)
                    }
                } else if (attendance == "Absent") {
                    holder.absentBtn.setChecked(true)
                    if (holder.presentBtn.isChecked()) {
                        holder.presentBtn.setChecked(false)
                    }
                }
            }
        }
    }

    private fun loadUserDetails(
        uid: String,
        holder: HolderAttendStud,
        binding: RowAttendanceItemBinding
    ) {
        val documentReference = FirebaseFirestore.getInstance().collection("Users").document(uid)
        documentReference.addSnapshotListener { snapshot, error ->
            val name = snapshot!!["name"].toString()
            val regNo = snapshot["regNo"].toString()
            holder.studentNameTv.setText(name)
            holder.studentRegNoTv.setText(regNo)
        }
    }

    override fun getItemCount(): Int {
        return atteStudArrayList.size
    }

    inner class  HolderAttendStud(itemView: View): RecyclerView.ViewHolder(itemView){

        var studentNameTv: TextView = binding.studentNameTv
        var studentRegNoTv: TextView = binding.studentRegNoTv
        var presentBtn: CheckBox = binding.presentBtn
        var absentBtn: CheckBox = binding.absentBtn

    }

}