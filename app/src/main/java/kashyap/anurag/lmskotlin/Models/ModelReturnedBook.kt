package kashyap.anurag.lmskotlin.Models

class ModelReturnedBook {
    var timestamp:String = ""
    var returnDate:String = ""
    var uid:String = ""

    constructor()
    constructor(timestamp: String, returnDate: String, uid: String) {
        this.timestamp = timestamp
        this.returnDate = returnDate
        this.uid = uid
    }


}