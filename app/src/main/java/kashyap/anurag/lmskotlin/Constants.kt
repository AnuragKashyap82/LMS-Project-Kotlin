package kashyap.anurag.lmskotlin

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kashyap.anurag.lmskotlin.Adapters.AdapterClassroom
import kashyap.anurag.lmskotlin.databinding.RowClassBinding

class Constants {

    companion object {

         var joiningCode: String = ""

        const val MAX_BYTES_PDF: Long = 50000000

        val marksCategories = arrayOf(
            "10",
            "20",
            "25",
            "40",
            "50",
            "75",
            "100"
        )

        val userType = arrayOf(
            "user",
            "teachers"
        )

        val branchCategories = arrayOf(
            "CSE",
            "ME",
            "EE",
            "ECE",
            "CIVIL"
        )
        val semesterCategories = arrayOf(
            "1st Semester",
            "2nt Semester",
            "3rd Semester",
            "4th Semester",
            "5th Semester",
            "6th Semester",
            "7th Semester",
            "8th Semester"
        )
    }



}