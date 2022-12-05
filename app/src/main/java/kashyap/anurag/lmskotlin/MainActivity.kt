package kashyap.anurag.lmskotlin

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kashyap.anurag.lmskotlin.Adapters.AdapterTimeTable
import kashyap.anurag.lmskotlin.Models.ModelTimeTable
import java.util.*

class MainActivity : AppCompatActivity() {

    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var profilePic: ImageView? = null
    var profileIv: ImageView? = null
    var createPostBtn: FloatingActionButton? = null
    var headerStudentIdTv: TextView? = null
    var headerEmailTv: TextView? = null
    var nameTv: TextView? = null
    var seeAllTv: TextView? = null
    var drawerBtn: Toolbar? = null
    var timeTableRv: RecyclerView? = null

    var noticeRl: RelativeLayout? = null
    var classroomRl: RelativeLayout? = null
    var timeTableRl: RelativeLayout? = null
    var libraryRl: RelativeLayout? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    private var progressDialog: ProgressDialog? = null
    var slider: ImageSlider? = null

    private lateinit var timeTableArrayList: ArrayList<ModelTimeTable>
    private lateinit var adapterTimeTable: AdapterTimeTable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Please wait")
        progressDialog!!.setCanceledOnTouchOutside(false)

        loadMyInfo()
        loadAllClasses()
        loadSlider()

        drawerLayout = findViewById(R.id.drawer)
        navigationView = findViewById(R.id.navigationView)
//        createPostBtn = findViewById(R.id.createPostBtn)
        drawerBtn = findViewById(R.id.drawerBtn)
        nameTv = findViewById(R.id.nameTv)
        profileIv = findViewById(R.id.profileIv)
        seeAllTv = findViewById(R.id.seeAllTv)
        timeTableRv = findViewById(R.id.timeTableRv)
        slider = findViewById(R.id.slider)
        noticeRl = findViewById(R.id.noticeRl)
        classroomRl = findViewById(R.id.classroomRl)
        timeTableRl = findViewById(R.id.timeTableRl)
        libraryRl = findViewById(R.id.libraryRl)

        val headerView = navigationView!!.getHeaderView(0)
        headerStudentIdTv = headerView.findViewById<View>(R.id.headerStudentIdTv) as TextView
        headerEmailTv = headerView.findViewById<View>(R.id.headerEmailTv) as TextView
        profilePic = headerView.findViewById<View>(R.id.profileIv) as ImageView

        seeAllTv!!.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, DaysActivity::class.java))
        })
//
//        headerStudentIdTv!!.setOnClickListener {
//            Toast.makeText(this, "Name Clicked.....!!!!", Toast.LENGTH_SHORT)
//                .show()
//            drawerLayout!!.closeDrawer(GravityCompat.START)
//        }
        profilePic!!.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            drawerLayout!!.closeDrawer(GravityCompat.START)
        }
        noticeRl!!.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this,
                    NoticeActivity::class.java
                )
            )
        })
        classroomRl!!.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this,
                    ClassroomActivity::class.java
                )
            )
        })
        libraryRl!!.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this,
                    LibraryActivity::class.java
                )
            )
        })
        timeTableRl!!.setOnClickListener(View.OnClickListener {
            val calendar = Calendar.getInstance()
            val day = calendar[Calendar.DAY_OF_WEEK]
            when (day) {
                Calendar.SUNDAY -> {}
                Calendar.MONDAY -> {
                    // Current day is Monday
                    val myIntent = Intent(this, TimeTableActivity::class.java)
                    myIntent.putExtra("text", "Monday")
                    startActivity(myIntent)
                }
                Calendar.TUESDAY -> {
                    val myIntent1 = Intent(this, TimeTableActivity::class.java)
                    myIntent1.putExtra("text", "Tuesday")
                    startActivity(myIntent1)
                }
                Calendar.WEDNESDAY -> {
                    val myIntent2 = Intent(this, TimeTableActivity::class.java)
                    myIntent2.putExtra("text", "Wednesday")
                    startActivity(myIntent2)
                }
                Calendar.THURSDAY -> {
                    val myIntent3 = Intent(this, TimeTableActivity::class.java)
                    myIntent3.putExtra("text", "Thursday")
                    startActivity(myIntent3)
                }
                Calendar.FRIDAY -> {
                    val myIntent4 = Intent(this, TimeTableActivity::class.java)
                    myIntent4.putExtra("text", "Friday")
                    startActivity(myIntent4)
                }
                Calendar.SATURDAY -> {
                    val myIntent5 = Intent(this, TimeTableActivity::class.java)
                    myIntent5.putExtra("text", "Saturday")
                    startActivity(myIntent5)
                }
            }
        })

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            drawerBtn,
            R.string.OpenDrawer,
            R.string.CloseDrawer
        )
        drawerLayout!!.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = resources.getColor(R.color.black)

        navigationView!!.setNavigationItemSelectedListener { item ->
            val id = item.itemId
            val fragment: Fragment? = null
            when (id) {
//                R.id.optClassroom -> startActivity(
//                    Intent(
//                        this@MainUsersActivity,
//                        ClassroomActivity::class.java
//                    )
//                )
                R.id.optTimeTable -> {
                    //                        startActivity(new Intent(MainUsersActivity.this, DaysActivity.class));
                    val calendar = Calendar.getInstance()
                    val day = calendar[Calendar.DAY_OF_WEEK]
                    when (day) {
                        Calendar.SUNDAY -> {}
                        Calendar.MONDAY -> {
                            // Current day is Monday
                            val myIntent =
                                Intent(this, TimeTableActivity::class.java)
                            myIntent.putExtra("text", "Monday")
                            startActivity(myIntent)
                        }
                        Calendar.TUESDAY -> {
                            val myIntent1 =
                                Intent(this, TimeTableActivity::class.java)
                            myIntent1.putExtra("text", "Tuesday")
                            startActivity(myIntent1)
                        }
                        Calendar.WEDNESDAY -> {
                            val myIntent2 =
                                Intent(this, TimeTableActivity::class.java)
                            myIntent2.putExtra("text", "Wednesday")
                            startActivity(myIntent2)
                        }
                        Calendar.THURSDAY -> {
                            val myIntent3 =
                                Intent(this, TimeTableActivity::class.java)
                            myIntent3.putExtra("text", "Thursday")
                            startActivity(myIntent3)
                        }
                        Calendar.FRIDAY -> {
                            val myIntent4 =
                                Intent(this, TimeTableActivity::class.java)
                            myIntent4.putExtra("text", "Friday")
                            startActivity(myIntent4)
                        }
                        Calendar.SATURDAY -> {
                            val myIntent5 =
                                Intent(this, TimeTableActivity::class.java)
                            myIntent5.putExtra("text", "Saturday")
                            startActivity(myIntent5)
                        }
                    }
                }
//                R.id.optFav -> startActivity(
//                    Intent(
//                        this@MainUsersActivity,
//                        FavouritesActivity::class.java
//                    )
//                )
//                R.id.optNotes -> startActivity(
//                    Intent(
//                        this@MainUsersActivity,
//                        NotesActivity::class.java
//                    )
//                )
                R.id.optNotice -> startActivity(
                    Intent(
                        this,
                        NoticeActivity::class.java
                    )
                )
//                R.id.optLibrary -> {}
//                R.id.optAttendance -> startActivity(
//                    Intent(
//                        this@MainUsersActivity,
//                        PresentStudentViewActivity::class.java
//                    )
//                )
//                R.id.optMaterial -> startActivity(
//                    Intent(
//                        this@MainUsersActivity,
//                        MaterialActivity::class.java
//                    )
//                )
//                R.id.optAss -> {}
//                R.id.optLectures -> startActivity(
//                    Intent(
//                        this@MainUsersActivity,
//                        LectureActivity::class.java
//                    )
//                )
//                R.id.optProfile -> startActivity(
//                    Intent(
//                        this@MainUsersActivity,
//                        ProfileActivity::class.java
//                    )
//                )
//                R.id.optChangePass -> startActivity(
//                    Intent(
//                        this@MainUsersActivity,
//                        ChangePasswordActivity::class.java
//                    )
//                )
//                R.id.optChangePhone -> startActivity(
//                    Intent(
//                        this@MainUsersActivity,
//                        ChangePhoneNoActivity::class.java
//                    )
//                )
//                R.id.optSetting -> startActivity(
//                    Intent(
//                        this@MainUsersActivity,
//                        SettingActivity::class.java
//                    )
//                )
                R.id.optAddSliderBtn -> startActivity(
                    Intent(
                        this,
                        AddSliderActivity::class.java
                    )
                )
//                R.id.optAddUniqueIdBtn -> startActivity(
//                    Intent(
//                        this@MainUsersActivity,
//                        AddUniqueIdActivity::class.java
//                    )
//                )
//                R.id.optAddStudentBtn -> startActivity(
//                    Intent(
//                        this@MainUsersActivity,
//                        AddStudentActivity::class.java
//                    )
//                )
//                R.id.optAboutUs -> startActivity(Intent(this, AboutUsActivity::class.java))
                R.id.optLogout -> {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Delete")
                        .setMessage("Are you sure want to Logout")
                        .setPositiveButton(
                            "Yes"
                        ) { dialogInterface, i ->
                            makeMeOffline()
                        }
                        .setNegativeButton(
                            "No"
                        ) { dialogInterface, i -> dialogInterface.dismiss() }
                        .show()
                }
            }
            drawerLayout!!.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            drawerLayout!!.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun loadMyInfo() {
        val documentReference = firebaseFirestore.collection("Users").document(
            firebaseAuth.uid!!
        )
        documentReference.addSnapshotListener { ds, error ->
            val profileImage = "" + ds!!.getString("profileImage")
            val userName = "" + ds.getString("name")
            val userEmail = "" + ds.getString("email")
            val uniqueId = "" + ds.getString("uniqueId")
            headerStudentIdTv!!.text = uniqueId
            headerEmailTv!!.text = userEmail
            nameTv!!.text = userName
            try {
                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray)
                    .into(profilePic)
            } catch (e: Exception) {
                profilePic!!.setImageResource(R.drawable.ic_person_gray)
            }
            try {
                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray)
                    .into(profileIv)
            } catch (e: Exception) {
                profileIv!!.setImageResource(R.drawable.ic_person_gray)
            }
        }
    }

    private fun makeMeOffline() {
        progressDialog!!.setMessage("Logging out...")
        progressDialog!!.show()
        val hashMap = HashMap<String, Any>()
        hashMap["online"] = "false"
        val documentReference = firebaseFirestore.collection("Users").document(
            firebaseAuth.uid!!
        )
        documentReference.update(hashMap)
            .addOnSuccessListener {
                progressDialog!!.setMessage("Logging Out...!")
                progressDialog!!.show()
                firebaseAuth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                progressDialog!!.dismiss()
            }
            .addOnFailureListener { e ->
                progressDialog!!.dismiss()
                Toast.makeText(this, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadAllClasses() {
        val calendar = Calendar.getInstance()
        val day = calendar[Calendar.DAY_OF_WEEK]
        when (day) {
            Calendar.SUNDAY -> {}
            Calendar.MONDAY -> {
                timeTableArrayList = ArrayList<ModelTimeTable>()
                FirebaseFirestore.getInstance().collection("timeTable").document(firebaseAuth.uid!!)
                    .collection("Monday")
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result) {
                                val modelTimeTable: ModelTimeTable =
                                    document.toObject(ModelTimeTable::class.java)
                                timeTableArrayList.add(modelTimeTable)
                            }
//                            Collections.sort(timeTableArrayList,
//                                Comparator<Any?> { t1, t2 ->
//                                    t1.getStartTime().compareToIgnoreCase(t2.getStartTime())
//                                })
                            adapterTimeTable =
                                AdapterTimeTable(this, timeTableArrayList)
                            timeTableRv!!.adapter = adapterTimeTable
                        }
                    }
            }
            Calendar.TUESDAY -> {
                timeTableArrayList = ArrayList<ModelTimeTable>()
                FirebaseFirestore.getInstance().collection("timeTable").document(firebaseAuth.uid!!)
                    .collection("Tuesday")
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result) {
                                val modelTimeTable: ModelTimeTable =
                                    document.toObject(ModelTimeTable::class.java)
                                timeTableArrayList.add(modelTimeTable)
                            }
//                            Collections.sort(timeTableArrayList,
//                                Comparator<Any?> { t1, t2 ->
//                                    t1.getStartTime().compareToIgnoreCase(t2.getStartTime())
//                                })
                            adapterTimeTable =
                                AdapterTimeTable(this, timeTableArrayList)
                            timeTableRv!!.adapter = adapterTimeTable
                        }
                    }
            }
            Calendar.WEDNESDAY -> {
                timeTableArrayList = ArrayList<ModelTimeTable>()
                FirebaseFirestore.getInstance().collection("timeTable").document(firebaseAuth.uid!!)
                    .collection("Wednesday")
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result) {
                                val modelTimeTable: ModelTimeTable =
                                    document.toObject(ModelTimeTable::class.java)
                                timeTableArrayList.add(modelTimeTable)
                            }
//                            Collections.sort(timeTableArrayList,
//                                Comparator<Any?> { t1, t2 ->
//                                    t1.getStartTime().compareToIgnoreCase(t2.getStartTime())
//                                })
                            adapterTimeTable =
                                AdapterTimeTable(this, timeTableArrayList)
                            timeTableRv!!.adapter = adapterTimeTable
                        }
                    }
            }
            Calendar.THURSDAY -> {
                timeTableArrayList = ArrayList<ModelTimeTable>()
                FirebaseFirestore.getInstance().collection("timeTable").document(firebaseAuth.uid!!)
                    .collection("Thursday")
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result) {
                                val modelTimeTable: ModelTimeTable =
                                    document.toObject(ModelTimeTable::class.java)
                                timeTableArrayList.add(modelTimeTable)
                            }
//                            Collections.sort(timeTableArrayList,
//                                Comparator<Any?> { t1, t2 ->
//                                    t1.getStartTime().compareToIgnoreCase(t2.getStartTime())
//                                })
                            adapterTimeTable =
                                AdapterTimeTable(this, timeTableArrayList)
                            timeTableRv!!.adapter = adapterTimeTable
                        }
                    }
            }
            Calendar.FRIDAY -> {
                timeTableArrayList = ArrayList<ModelTimeTable>()
                FirebaseFirestore.getInstance().collection("timeTable").document(firebaseAuth.uid!!)
                    .collection("Friday")
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result) {
                                val modelTimeTable: ModelTimeTable =
                                    document.toObject(ModelTimeTable::class.java)
                                timeTableArrayList.add(modelTimeTable)
                            }
//                            Collections.sort(timeTableArrayList,
//                                Comparator<Any?> { t1, t2 ->
//                                    t1.getStartTime().compareToIgnoreCase(t2.getStartTime())
//                                })
                            adapterTimeTable =
                                AdapterTimeTable(this, timeTableArrayList)
                            timeTableRv!!.adapter = adapterTimeTable
                        }
                    }
            }
            Calendar.SATURDAY -> {
                timeTableArrayList = ArrayList<ModelTimeTable>()
                FirebaseFirestore.getInstance().collection("timeTable").document(firebaseAuth.uid!!)
                    .collection("Saturday")
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result) {
                                val modelTimeTable: ModelTimeTable =
                                    document.toObject(ModelTimeTable::class.java)
                                timeTableArrayList.add(modelTimeTable)
                            }
//                            Collections.sort(timeTableArrayList,
//                                Comparator<Any?> { t1, t2 ->
//                                    t1.getStartTime().compareToIgnoreCase(t2.getStartTime())
//                                })
                            adapterTimeTable =
                                AdapterTimeTable(this, timeTableArrayList)
                            timeTableRv!!.adapter = adapterTimeTable
                        }
                    }
            }
        }
    }

    val banner = ArrayList<SlideModel>()
    private fun loadSlider() {
        val ref = FirebaseDatabase.getInstance().getReference("Slider")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.getChildren()) banner.add(
                    SlideModel(
                        data.child("url").getValue().toString(), ScaleTypes.CENTER_CROP
                    )
                )
                slider!!.setImageList(banner, ScaleTypes.CENTER_CROP)
                slider!!.setItemClickListener(object : ItemClickListener {
                    override fun onItemSelected(i: Int) {
                        showBottomSheetDialog()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun showBottomSheetDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bs_more_feed_options)
        val editPostLL = dialog.findViewById<LinearLayout>(R.id.editPostLL)
        val deletePostLL = dialog.findViewById<LinearLayout>(R.id.deletePostLL)
        val sharePostLL = dialog.findViewById<LinearLayout>(R.id.sharePostLL)
        val favPostLL = dialog.findViewById<LinearLayout>(R.id.favPostLL)
        val blockPostLL = dialog.findViewById<LinearLayout>(R.id.blockPostLL)
        editPostLL.visibility = View.VISIBLE
        deletePostLL.visibility = View.VISIBLE
        sharePostLL.visibility = View.GONE
        favPostLL.visibility = View.GONE
        blockPostLL.visibility = View.GONE
        dialog.show()
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.dialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
//        editPostLL.setOnClickListener {
//            dialog.dismiss()
//            val intent = Intent(this, AddSliderActivity::class.java)
//            startActivity(intent)
//        }
        deletePostLL.setOnClickListener {
            dialog.dismiss()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete")
                .setMessage("Are you sure want to Delete this Slider Image")
                .setPositiveButton(
                    "Yes"
                ) { dialogInterface, i ->
                    progressDialog!!.setMessage("Deleting Slider")
                    progressDialog!!.show()
                    val ref: DatabaseReference =
                        FirebaseDatabase.getInstance().getReference("Slider")
                    ref
                        .removeValue()
                        .addOnCompleteListener(OnCompleteListener<Void?> {
                            progressDialog!!.dismiss()
                            Toast.makeText(
                                this,
                                "Slider Deleted...!",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                        .addOnFailureListener(OnFailureListener { e ->
                            progressDialog!!.dismiss()
                            Toast.makeText(
                                this,
                                "" + e.message,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        })
                }
                .setNegativeButton(
                    "No"
                ) { dialogInterface, i -> dialogInterface.dismiss() }
                .show()
        }
    }

}