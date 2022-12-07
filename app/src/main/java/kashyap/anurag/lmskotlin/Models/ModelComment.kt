package kashyap.anurag.lmskotlin.Models

class ModelComment {

    var commentId:String = ""
    var materialId:String = ""
    var timestamp:String = ""
    var comment:String = ""
    var uid:String = ""

    constructor()
    constructor(
        commentId: String,
        materialId: String,
        timestamp: String,
        comment: String,
        uid: String
    ) {
        this.commentId = commentId
        this.materialId = materialId
        this.timestamp = timestamp
        this.comment = comment
        this.uid = uid
    }


}