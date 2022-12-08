package kashyap.anurag.lmskotlin.Models

class ModelAttendanceReport {

    var Attendance:String = ""
    var date:String = ""
    var uid:String = ""

    constructor()
    constructor(Attendance: String, date: String, uid: String) {
        this.Attendance = Attendance
        this.date = date
        this.uid = uid
    }


}