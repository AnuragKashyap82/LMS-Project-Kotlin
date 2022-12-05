package kashyap.anurag.lmskotlin.Models

class ModelMaterial {

    var materialId:String = ""
    var subjectName:String = ""
    var topicName:String = ""
    var branch:String = ""
    var semester:String = ""
    var timestamp:String = ""
    var url:String = ""

    constructor()
    constructor(
        materialId: String,
        subjectName: String,
        topicName: String,
        branch: String,
        semester: String,
        timestamp: String,
        url: String
    ) {
        this.materialId = materialId
        this.subjectName = subjectName
        this.topicName = topicName
        this.branch = branch
        this.semester = semester
        this.timestamp = timestamp
        this.url = url
    }


}