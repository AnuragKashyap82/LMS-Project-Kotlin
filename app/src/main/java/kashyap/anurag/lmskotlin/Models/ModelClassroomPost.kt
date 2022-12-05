package kashyap.anurag.lmskotlin.Models

class ModelClassroomPost {

    var uid:String = ""
    var classMsg:String = ""
    var classCode:String = ""
    var timestamp:String = ""
    var url:String = ""
    var attachmentExist:Boolean = false

    constructor()
    constructor(
        uid: String,
        classMsg: String,
        classCode: String,
        timestamp: String,
        url: String,
        attachmentExist: Boolean
    ) {
        this.uid = uid
        this.classMsg = classMsg
        this.classCode = classCode
        this.timestamp = timestamp
        this.url = url
        this.attachmentExist = attachmentExist
    }


}