package kashyap.anurag.lmskotlin.Models

class ModelAtteStud {

    var uid:String = ""
    var monthYear:String = ""
    var presentCount:String = ""
    var absentCount:String = ""
    var classCode:String = ""

    constructor()

    constructor(
        uid: String,
        monthYear: String,
        presentCount: String,
        absentCount: String,
        classCode: String
    ) {
        this.uid = uid
        this.monthYear = monthYear
        this.presentCount = presentCount
        this.absentCount = absentCount
        this.classCode = classCode
    }


}