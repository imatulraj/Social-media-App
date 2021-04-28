package com.gupta.twitter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_login_event.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.ticketpost.*
import kotlinx.android.synthetic.main.ticketpost.view.*
import kotlinx.android.synthetic.main.tweet_ticket.view.*
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    var listOftickets=ArrayList<ticket>()
    var listadapter:listshowAdapter?=null
    var myemail:String?=null
    var myuid:String?=null
    private var database= FirebaseDatabase.getInstance()
    private var myRef=database.reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var b:Bundle=intent.extras!!
        myemail=b.getString("email")
        myuid=b.getString("uid")
        listOftickets.add(ticket("21","uhdbc","URL","add","12345"))
        listadapter=listshowAdapter(this,listOftickets)
        listshow.adapter=listadapter
        loadimagetolist()
    }
    inner class listshowAdapter:BaseAdapter{
        var context:Context?=null
        var listoftweets=ArrayList<ticket>()
        constructor(context:Context,listoftweets:ArrayList<ticket>){
            this.listoftweets=listoftweets
            this.context=context
        }
        override fun getCount(): Int {
            return listoftweets.size
        }

        override fun getItem(position: Int): Any {
            return listoftweets[position]
        }

        override fun getItemId(position: Int): Long {
            return  position.toLong()
        }

        @RequiresApi(Build.VERSION_CODES.N)
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var tweet=listoftweets[position]
            if(tweet.tweetuserUId.equals("add"))
            {
                var myview=layoutInflater.inflate(R.layout.ticketpost,null)

                var df= SimpleDateFormat("dd/mm/yyyy hh:mm")
                var dateobj= Date()
                myview.postAttach.setOnClickListener(View.OnClickListener {
                    loadimage()
                })
                myview.postSend.setOnClickListener(View.OnClickListener {
                    myRef.child("post").push().setValue(postinfo(myemail!!,myuid!!,DownloadUrl!!,myview.postText.text.toString(),df.format(dateobj)))
                    Toast.makeText(applicationContext,"uploaded",Toast.LENGTH_SHORT).show()
                })
                return myview
            }
            else if(tweet.tweetuserUId.equals("loading"))
                {
                    var myview=layoutInflater.inflate(R.layout.loading,null)
                    return myview
                }
            else {
                var myview=layoutInflater.inflate(R.layout.tweet_ticket,null)
                myview.txt_tweet.setText(tweet.tweettxt)
                myview.userName.setText(tweet.tweetuserUId)
                Picasso.get()
                    .load(tweet.tweetimgeUrl)
                    .into(myview.tweet_picture)
                myview.userDate.setText(tweet.date)
                myRef.child("user").child(tweet.tweetuserUId!!).addValueEventListener(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        var td=snapshot!!.value as HashMap<String,Any>
                        for (key in td.keys)
                        {
                            var value =td[key] as String
                            if(key.equals("ProfileImage")){
                                Picasso.get().load(value).into(myview.userImage)
                            }
                        }
                    }catch (E:Exception){
                        Toast.makeText(this@MainActivity,"error in profile fetch",Toast.LENGTH_LONG).show()
                    }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
                myview.tweet_Unlike.setOnClickListener(View.OnClickListener {
                 myview.tweet_Unlike.visibility=View.GONE
                    myview.tweet_like.visibility=View.VISIBLE
                })
                return myview
            }
        }
    }

    val PICK_IMAGE_CODE=123
    fun loadimage() {
        var intent= Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(intent, PICK_IMAGE_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==PICK_IMAGE_CODE&& data!=null&&resultCode== RESULT_OK)
        {
            var selectedImage=data.data
            var filepathIndex= arrayOf(MediaStore.Images.Media.DATA)
            var cursor=contentResolver.query(selectedImage!!, filepathIndex, null, null, null)
            cursor!!.moveToFirst()
            var columnIndex=cursor!!.getColumnIndex(filepathIndex[0])
            var pathimge=cursor!!.getString(columnIndex)
            cursor!!.close()
           uploadDirectory(BitmapFactory.decodeFile(pathimge))
        }
    }
    fun loadimagetolist()
    {
        myRef.child("post").addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    var td=snapshot!!.value as HashMap<String,Any>
                    listOftickets.clear()
                    listOftickets.add(ticket("21","uhdbc","URL","add","12234"))
                    for (key in td!!.keys)
                    {
                        var post =td[key] as HashMap<String, Any>
                        listOftickets.add(ticket(key,
                                post["text"] as String,
                            post["image"] as String,
                                SplitString(post["email"] as String),
                        post["time"] as String))
                    }
                    listadapter!!.notifyDataSetChanged()
                }catch (E:Exception){}
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })

    }
    var DownloadUrl:String?=null
    @RequiresApi(Build.VERSION_CODES.N)
    private fun uploadDirectory(bitmap: Bitmap) {
        val  storage:FirebaseStorage= FirebaseStorage.getInstance()
        listOftickets.add(0,ticket("21","uhdbc","URL","loading","12345"))
        listadapter!!.notifyDataSetChanged()
        var storageRef= storage.getReferenceFromUrl("gs://twitter-5b844.appspot.com")
        var df= SimpleDateFormat("dd:mm:yyyy:hh:mm:ss")
        var dateobj= Date()
        var imagepath=SplitString(myemail!!)+"/post/"+df.format(dateobj)+".jpg"
        var imageref=storageRef.child("images/"+imagepath)
        // compressing the image
        var boas= ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,boas)
        //uploading now
        val data=boas.toByteArray()
        val uploadimge=imageref.putBytes(data)
        uploadimge.addOnFailureListener {
            Toast.makeText(applicationContext,"fail to uploaded data", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot->
            //var x=storageRef.child("images/"+imagepath).downloadUrl.result
           storageRef.child("images/"+imagepath).downloadUrl.addOnCompleteListener(
               OnCompleteListener {
                   if(it.isSuccessful)
                   {
                       DownloadUrl=it.result.toString()
                  // Toast.makeText(this,"$DownloadUrl",Toast.LENGTH_LONG).show()
                       listOftickets.removeAt(0)
                       listadapter!!.notifyDataSetChanged()
                   }
               })

        }


    }
    fun SplitString(email:String):String{
        val split= email.split("@")
        return split[0]
    }
}