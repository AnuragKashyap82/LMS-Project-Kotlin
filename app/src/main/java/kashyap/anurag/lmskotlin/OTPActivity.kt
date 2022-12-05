package kashyap.anurag.lmskotlin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import kashyap.anurag.lmskotlin.databinding.ActivityOtpactivityBinding
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpactivityBinding
    private lateinit var firebaseAuth: FirebaseAuth
    var verificationId: String? = ""
    var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Sending OTP...")
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()

        firebaseAuth = FirebaseAuth.getInstance()

        val phoneNumber = intent.getStringExtra("phoneNumber")
        val uniqueId = intent.getStringExtra("uniqueId")
        val userType = intent.getStringExtra("userType")

        binding.phoneTv.text = "+91$phoneNumber"

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {

                }

                override fun onVerificationFailed(e: FirebaseException) {
                    progressDialog!!.dismiss()
                    onBackPressed()
                    finish()
                }

                override fun onCodeSent(
                    verifyId: String,
                    forceResendingToken: ForceResendingToken
                ) {
                    super.onCodeSent(verifyId, forceResendingToken)
                    progressDialog!!.dismiss()
                    verificationId = verifyId
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    binding.otpView.requestFocus()
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        binding.otpView.setOtpCompletionListener { otp ->
            val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
            firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firebaseAuth.signOut()
                    val intent = Intent(this, RegisterActivity::class.java)
                    intent.putExtra("phoneNumber", phoneNumber)
                    intent.putExtra("uniqueId", uniqueId)
                    intent.putExtra("userType", userType)
                    startActivity(intent)
                    finishAffinity()
                    Toast.makeText(this, "Otp Verified.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Otp Doesn't matches", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}