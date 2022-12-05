package kashyap.anurag.lmskotlin.Models

class ModelClassroom {

    var subjectName:String = ""
    var className:String = ""
    var classCode:String = ""
    var timestamp:String = ""
    var uid:String = ""
    var theme:String = ""

    constructor()
    constructor(
        subjectName: String,
        className: String,
        classCode: String,
        timestamp: String,
        uid: String,
        theme: String
    ) {
        this.subjectName = subjectName
        this.className = className
        this.classCode = classCode
        this.timestamp = timestamp
        this.uid = uid
        this.theme = theme
    }


}