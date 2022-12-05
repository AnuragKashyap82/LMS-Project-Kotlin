package kashyap.anurag.lmskotlin.Models

class ModelIssuedBooks {

    var timestamp:String = ""
    var issueDate:String = ""
    var uid:String = ""

    constructor()
    constructor(timestamp: String, issueDate: String, uid: String) {
        this.timestamp = timestamp
        this.issueDate = issueDate
        this.uid = uid
    }

}