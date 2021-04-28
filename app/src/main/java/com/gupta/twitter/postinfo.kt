package com.gupta.twitter

import com.google.firebase.storage.StorageReference

class postinfo {
    var email:String?=null
    var uid:String?=null
    var image:String?=null
    var text:String?=null
    var time:String?=null
    constructor(email:String,uid:String,image:String,text:String,time:String)
    {
        this.email=email
        this.uid=uid
        this.image=image
        this.text=text
        this.time=time
    }
}