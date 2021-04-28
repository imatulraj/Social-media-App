package com.gupta.twitter

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_login_event.*
import java.io.ByteArrayOutputStream
import java.util.*


class loginEvent : AppCompatActivity() {
     var storage: FirebaseStorage?=null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private lateinit var auth: FirebaseAuth
    private var database= FirebaseDatabase.getInstance()
    private var myRef=database.reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_event)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        auth = FirebaseAuth.getInstance()

        loginImage.setOnClickListener(View.OnClickListener {
            checkpermission()
        })

    }

    override fun onStart() {
        super.onStart()
        var user=auth.currentUser
        if(user!=null)
        {
            LoadTwetter()
        }
    }
    val loaderImage=123
     fun checkpermission() {
        if(Build.VERSION.SDK_INT>=23)
        {if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )!= PackageManager.PERMISSION_GRANTED)
            requestPermissions(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                loaderImage
            )
        }
        loadimage()
    }
    @RequiresApi(Build.VERSION_CODES.N)
    private fun createAccount(email: String, password: String) {
        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    firebaseImageLoad()

                } else {
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }


    var DownloadUrl:String?=null
    @RequiresApi(Build.VERSION_CODES.N)
    private fun firebaseImageLoad() {
        var user=auth.currentUser!!
        var email=user.email
        storage=FirebaseStorage.getInstance()
        var storageRef= storage!!.getReferenceFromUrl("gs://twitter-5b844.appspot.com")
        var df=SimpleDateFormat("dd:mm:yyyy:hh:mm:ss")
        var dateobj= Date()
        var imagepath=SplitString(email!!)+"/profile/"+df.format(dateobj)+".jpg"
        var imageref=storageRef.child("images/"+imagepath)
        loginImage.isDrawingCacheEnabled=true
        loginImage.buildDrawingCache()
        var drawable=loginImage.drawable as BitmapDrawable
        var bitmap=drawable.bitmap
        // compressing the image
        var boas=ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,boas)
        //uploading now
        val data=boas.toByteArray()
        val uploadimge=imageref.putBytes(data)
        uploadimge.addOnFailureListener {
            progress.visibility=View.GONE
            Toast.makeText(applicationContext,"dail to uplaod data",Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot->
            //var x=storageRef.child("images/"+imagepath).downloadUrl.result

            storageRef.child("images/"+imagepath).downloadUrl.addOnCompleteListener(
                OnCompleteListener {
                    progress.visibility=View.GONE
                    if(it.isSuccessful)
                    {
                        DownloadUrl=it.result.toString()
                        Log.d("imagepostProfile","$DownloadUrl")
                        myRef.child("user").child(SplitString(email)).child("email").setValue(user.email)
                        myRef.child("user").child(SplitString(email)).child("Uid").setValue(user.uid)
                        myRef.child("user").child(SplitString(email)).child("ProfileImage").setValue(DownloadUrl!!)
                        LoadTwetter()
                    }
                })


        }
            .addOnProgressListener {
                progress.visibility=View.VISIBLE
            }
                


    }

    private fun LoadTwetter() {
        var user=auth.currentUser
        if (user!=null)
        {
            var intent=Intent(this,MainActivity::class.java)
            intent.putExtra("email",user.email)
            intent.putExtra("uid",user.uid)
            startActivity(intent)
        }
    }

    fun SplitString(email:String):String{
        val split= email.split("@")
        return split[0]
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            loaderImage -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadimage()
                } else {
                    Toast.makeText(applicationContext, "PERMISSION DENIED", Toast.LENGTH_SHORT)
                        .show()
                }

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    val PICK_IMAGE_CODE=123
     fun loadimage() {
        var intent=Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
         startActivityForResult(intent, PICK_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==PICK_IMAGE_CODE&& data!=null&&resultCode== RESULT_OK)
        {
            var selectedImage=data.data
            var filepathIndex= arrayOf(MediaStore.Images.Media.DATA)
           // val cursor=ContentResolver.query(selectedImage!!,filepathIndex!!,null,null,null)
            var cursor=contentResolver.query(selectedImage!!, filepathIndex, null, null, null)
            cursor!!.moveToFirst()
            var columnIndex=cursor!!.getColumnIndex(filepathIndex[0])
            var pathimge=cursor!!.getString(columnIndex)
            cursor!!.close()
            loginImage.setImageBitmap(BitmapFactory.decodeFile(pathimge))
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun loginevent(view: View) {
        createAccount(enterEmail.text.toString(), enterPass.text.toString())
    }
}