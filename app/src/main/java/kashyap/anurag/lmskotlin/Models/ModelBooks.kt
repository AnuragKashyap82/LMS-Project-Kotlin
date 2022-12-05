package kashyap.anurag.lmskotlin.Models

class ModelBooks {

    var subjectName:String = ""
    var bookName:String = ""
    var authorName:String = ""
    var bookId:String = ""
    var timestamp:String = ""

    constructor()
    constructor(
        subjectName: String,
        bookName: String,
        authorName: String,
        bookId: String,
        timestamp: String
    ) {
        this.subjectName = subjectName
        this.bookName = bookName
        this.authorName = authorName
        this.bookId = bookId
        this.timestamp = timestamp
    }


}