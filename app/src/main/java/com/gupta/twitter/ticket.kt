package com.gupta.twitter

class ticket {
    var tweettxt:String?=null
    var tweetId:String?=null
    var tweetuserUId:String?=null
    var date:String?=null
    var tweetimgeUrl:String?=null
    constructor(tweetId:String,tweettxt:String,tweetimgeUrl:String,tweetuserUId:String,date:String)
    {
        this.tweetId=tweetId
        this.tweetimgeUrl=tweetimgeUrl
        this.tweettxt=tweettxt
        this.tweetuserUId=tweetuserUId
        this.date=date
    }
}