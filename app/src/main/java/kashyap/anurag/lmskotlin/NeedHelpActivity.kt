package kashyap.anurag.lmskotlin

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kashyap.anurag.lmskotlin.databinding.ActivityNeedHelpBinding

class NeedHelpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNeedHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNeedHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sendMailBtn.setOnClickListener {
            validateData() }

        binding.backBtn.setOnClickListener { onBackPressed() }
    }

    private fun validateData() {
        val problemDetails: String
        problemDetails = binding.problemEt.text.toString().trim()
        if (TextUtils.isEmpty(problemDetails)) {
            Toast.makeText(this, "Describe Yours Problem....", Toast.LENGTH_SHORT).show()
            return
        } else {
            val addresses = arrayOf("anuragkashyap0802@gmail.com")
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "gmail/*"
            intent.putExtra(Intent.EXTRA_EMAIL, addresses)
            intent.putExtra(Intent.EXTRA_SUBJECT, "Problem related SignIn/SignUp")
            intent.putExtra(Intent.EXTRA_TEXT, problemDetails)
            if (intent.resolveActivity(this@NeedHelpActivity.packageManager) != null) {
                startActivity(intent)
            }
        }
    }

}