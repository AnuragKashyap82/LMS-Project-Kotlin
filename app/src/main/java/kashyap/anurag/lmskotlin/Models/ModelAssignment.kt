package kashyap.anurag.lmskotlin.Models

class ModelAssignment {

    var assignmentId:String = ""
    var classCode:String = ""
    var assignmentName:String = ""
    var fullMarks:String = ""
    var dueDate:String = ""
    var timestamp:String = ""
    var uid:String = ""
    var url:String = ""

    constructor()
    constructor(
        assignmentId: String,
        classCode: String,
        assignmentName: String,
        fullMarks: String,
        dueDate: String,
        timestamp: String,
        uid: String,
        url: String
    ) {
        this.assignmentId = assignmentId
        this.classCode = classCode
        this.assignmentName = assignmentName
        this.fullMarks = fullMarks
        this.dueDate = dueDate
        this.timestamp = timestamp
        this.uid = uid
        this.url = url
    }


}