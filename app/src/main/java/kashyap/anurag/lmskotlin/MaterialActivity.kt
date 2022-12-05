package kashyap.anurag.lmskotlin

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kashyap.anurag.lmskotlin.databinding.ActivityMaterialBinding

class MaterialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMaterialBinding
    private var branch: String? = null
    private var semester: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaterialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.semTv.setOnClickListener {
            val builder = AlertDialog.Builder(this@MaterialActivity)
            builder.setTitle("Select Semester:")
                .setItems(Constants.semesterCategories,
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        val selectedUser: String = Constants.semesterCategories.get(i)
                        binding.semTv.text = selectedUser
                    }).show()
        }
        binding.branchTv.setOnClickListener {
            val builder = AlertDialog.Builder(this@MaterialActivity)
            builder.setTitle("Select Branch:")
                .setItems(Constants.branchCategories,
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        val selectedUser: String = Constants.branchCategories.get(i)
                        binding.branchTv.text = selectedUser
                    }).show()
        }
        binding.searchMaterialBtn.setOnClickListener { validateData() }
        binding.addMaterialBtn.setOnClickListener {
            startActivity(
                Intent(
                    this@MaterialActivity,
                    AddMaterialActivity::class.java
                )
            )
        }
    }
    private fun validateData() {
        branch = binding.branchTv.text.toString().trim()
        semester = binding.semTv.text.toString().trim()
        if (TextUtils.isEmpty(branch)) {
            Toast.makeText(this@MaterialActivity, "Select Your Branch!!!!!", Toast.LENGTH_SHORT)
                .show()
        } else if (TextUtils.isEmpty(semester)) {
            Toast.makeText(this@MaterialActivity, "Select Your Semester!!!!!", Toast.LENGTH_SHORT)
                .show()
        } else {
            searchMaterial()
        }
    }

    private fun searchMaterial() {
        val intent = Intent(this@MaterialActivity, AllMaterialActivity::class.java)
        intent.putExtra("branch", branch)
        intent.putExtra("semester", semester)
        startActivity(intent)
    }
}