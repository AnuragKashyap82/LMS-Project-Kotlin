package kashyap.anurag.lmskotlin.Models

class ModelNotice {

    var uid:String = ""
    var NoticeId:String = ""
    var title:String = ""
    var number:String = ""
    var url:String = ""
    var timestamp:String = ""

    constructor()

    constructor(
        uid: String,
        NoticeId: String,
        title: String,
        number: String,
        url: String,
        timestamp: String
    ) {
        this.uid = uid
        this.NoticeId = NoticeId
        this.title = title
        this.number = number
        this.url = url
        this.timestamp = timestamp
    }
}