package kashyap.anurag.lmskotlin.Models

class ModelTimeTable {

    var subName:String = ""
    var teacherName:String = ""
    var startTime:String = ""
    var endTime:String = ""
    var day:String = ""
    var ongoingTopic:String = ""
    var percentSylComp:String = ""

    constructor()
    constructor(
        subName: String,
        teacherName: String,
        startTime: String,
        endTime: String,
        day: String,
        ongoingTopic: String,
        percentSylComp: String
    ) {
        this.subName = subName
        this.teacherName = teacherName
        this.startTime = startTime
        this.endTime = endTime
        this.day = day
        this.ongoingTopic = ongoingTopic
        this.percentSylComp = percentSylComp
    }


}