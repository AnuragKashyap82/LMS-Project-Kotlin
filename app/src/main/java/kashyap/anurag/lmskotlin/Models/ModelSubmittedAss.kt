package kashyap.anurag.lmskotlin.Models

class ModelSubmittedAss {

    var assignmentId:String = ""
    var assignmentName:String = ""
    var returnDate:String = ""
    var classCode:String = ""
    var dueDate:String = ""
    var fullMarks:String = ""
    var timestamp:String = ""
    var uid:String = ""
    var url:String = ""
    var marksObtained:String = ""
    var isSubmitted:Boolean = false

    constructor()
    constructor(
        assignmentId: String,
        assignmentName: String,
        returnDate: String,
        classCode: String,
        dueDate: String,
        fullMarks: String,
        timestamp: String,
        uid: String,
        url: String,
        marksObtained: String,
        isSubmitted: Boolean
    ) {
        this.assignmentId = assignmentId
        this.assignmentName = assignmentName
        this.returnDate = returnDate
        this.classCode = classCode
        this.dueDate = dueDate
        this.fullMarks = fullMarks
        this.timestamp = timestamp
        this.uid = uid
        this.url = url
        this.marksObtained = marksObtained
        this.isSubmitted = isSubmitted
    }


}