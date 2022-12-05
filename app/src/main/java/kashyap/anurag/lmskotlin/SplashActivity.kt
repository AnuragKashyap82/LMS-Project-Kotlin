package kashyap.anurag.lmskotlin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        Handler().postDelayed(Runnable {
            checkUsers();
        }, 2000)

    }

    private fun checkUsers() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        } else {
            val documentReference = firebaseFirestore.collection("Users").document(firebaseUser.uid)
            documentReference.addSnapshotListener(
                this
            ) { value, error ->
                val userType = "" + value!!.getString("userType")
                if (userType == "teachers") {
                    if (!firebaseUser.isEmailVerified) {
                        startActivity(Intent(this@SplashActivity, VerifyEmailActivity::class.java))
                        finishAffinity()
                    } else {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    }
                } else if (userType == "user") {
                    if (!firebaseUser.isEmailVerified) {
                        startActivity(Intent(this@SplashActivity, VerifyEmailActivity::class.java))
                        finishAffinity()
                    } else {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    }
                } else if (userType == "anurag") {
                    if (!firebaseUser.isEmailVerified) {
                        startActivity(Intent(this@SplashActivity, VerifyEmailActivity::class.java))
                        finishAffinity()
                    } else {
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}