package kashyap.anurag.lmskotlin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.databinding.ActivityDaysBinding

class DaysActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDaysBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaysBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mondayRl.setOnClickListener { view ->
            val text = binding.monday.text.toString()
            val myIntent = Intent(view.context, TimeTableActivity::class.java)
            myIntent.putExtra("text", text)
            startActivity(myIntent)
        }
        binding.tuesdayRl.setOnClickListener { view ->
            val text = binding.tuesday.text.toString()
            val myIntent = Intent(view.context, TimeTableActivity::class.java)
            myIntent.putExtra("text", text)
            startActivity(myIntent)
        }
        binding.wednesdayRl.setOnClickListener { view ->
            val text = binding.wednesday.text.toString()
            val myIntent = Intent(view.context, TimeTableActivity::class.java)
            myIntent.putExtra("text", text)
            startActivity(myIntent)
        }
        binding.thurRl.setOnClickListener { view ->
            val text = binding.thursday.text.toString()
            val myIntent = Intent(view.context, TimeTableActivity::class.java)
            myIntent.putExtra("text", text)
            startActivity(myIntent)
        }
        binding.fridayRl.setOnClickListener { view ->
            val text = binding.friday.text.toString()
            val myIntent = Intent(view.context, TimeTableActivity::class.java)
            myIntent.putExtra("text", text)
            startActivity(myIntent)
        }
        binding.satRl.setOnClickListener { view ->
            val text = binding.saturday.text.toString()
            val myIntent = Intent(view.context, TimeTableActivity::class.java)
            myIntent.putExtra("text", text)
            startActivity(myIntent)
        }
    }
}