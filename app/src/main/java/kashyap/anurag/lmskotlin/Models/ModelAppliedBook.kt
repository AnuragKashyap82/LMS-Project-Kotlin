package kashyap.anurag.lmskotlin.Models

class ModelAppliedBook {

    var timestamp:String = ""
    var appliedDate:String = ""
    var uid:String = ""

    constructor()
    constructor(timestamp: String, appliedDate: String, uid: String) {
        this.timestamp = timestamp
        this.appliedDate = appliedDate
        this.uid = uid
    }

}