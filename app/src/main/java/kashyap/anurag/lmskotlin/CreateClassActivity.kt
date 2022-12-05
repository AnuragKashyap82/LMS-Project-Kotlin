package kashyap.anurag.lmskotlin

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.databinding.ActivityCreateClassBinding

class CreateClassActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateClassBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var className: String? = null
    private var subjectName: String? = null
    private var theme: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateClassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        binding.createClassBtn.setOnClickListener { validateData() }
        binding.themeEt.setOnClickListener { showThemeDialog() }

    }

    private fun showThemeDialog() {
        val myDialog = AlertDialog.Builder(this@CreateClassActivity, R.style.BottomSheetStyle)
        val inflater = LayoutInflater.from(this@CreateClassActivity)
        val view1: View = inflater.inflate(R.layout.select_theme_dialog, null)
        myDialog.setView(view1)
        val dialog = myDialog.create()
        dialog.setCancelable(false)
        val themeOne = view1.findViewById<ImageView>(R.id.themeOne)
        val themeTwo = view1.findViewById<ImageView>(R.id.themeTwo)
        val themeThree = view1.findViewById<ImageView>(R.id.themeThree)
        val themeFour = view1.findViewById<ImageView>(R.id.themeFour)
        val themeFive = view1.findViewById<ImageView>(R.id.themeFive)
        themeOne.setOnClickListener {
            dialog.dismiss()
            binding.themeEt.text = "1"
        }
        themeTwo.setOnClickListener {
            dialog.dismiss()
            binding.themeEt.text = "2"
        }
        themeThree.setOnClickListener {
            dialog.dismiss()
            binding.themeEt.text = "3"
        }
        themeFour.setOnClickListener {
            dialog.dismiss()
            binding.themeEt.text = "4"
        }
        themeFive.setOnClickListener {
            dialog.dismiss()
            binding.themeEt.text = "5"
        }
        dialog.show()
    }

    private fun validateData() {
        subjectName = binding.subjectEt.text.toString().trim()
        className = binding.classNameEt.text.toString().trim()
        theme = binding.themeEt.text.toString().trim()
        if (TextUtils.isEmpty(subjectName)) {
            Toast.makeText(this@CreateClassActivity, "Enter Subject Name....!", Toast.LENGTH_SHORT)
                .show()
        } else if (TextUtils.isEmpty(className)) {
            Toast.makeText(this@CreateClassActivity, "Enter Class Name....!", Toast.LENGTH_SHORT)
                .show()
        } else if (TextUtils.isEmpty(theme)) {
            Toast.makeText(this@CreateClassActivity, "Theme!!!!!", Toast.LENGTH_SHORT).show()
        } else {
            createClass()
        }
    }

    private fun createClass() {
        binding.progressBar.visibility = View.VISIBLE
        val timestamp = System.currentTimeMillis()
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["subjectName"] = "" + subjectName
        hashMap["className"] = "" + className
        hashMap["theme"] = "" + theme
        hashMap["classCode"] = "" + timestamp
        hashMap["uid"] = "" + firebaseAuth.uid
        hashMap["timestamp"] = "" + timestamp
        val documentReference = firebaseFirestore.collection("classroom").document("" + timestamp)
        documentReference.set(hashMap)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                clearText()
                createOwnClass(timestamp, subjectName!!, className!!)
                Toast.makeText(
                    this@CreateClassActivity,
                    "Class Created Successfully....",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@CreateClassActivity,
                    " Failed to Create Class to db due to" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun createOwnClass(timestamp: Long, subjectName: String, className: String) {
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["subjectName"] = "" + subjectName
        hashMap["className"] = "" + className
        hashMap["theme"] = "" + theme
        hashMap["classCode"] = "" + timestamp
        hashMap["uid"] = "" + firebaseAuth.uid
        hashMap["timestamp"] = "" + timestamp
        val documentReference = firebaseFirestore.collection("Users").document(
            firebaseAuth.uid!!
        ).collection("classroom").document("" + timestamp)
        documentReference.set(hashMap)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@CreateClassActivity,
                    "Class Created Successfully....",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(
                    this@CreateClassActivity,
                    " Failed to Create Class to db due to" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun clearText() {
        binding.classNameEt.setText("")
        binding.subjectEt.setText("")
        binding.themeEt.text = ""
    }
}