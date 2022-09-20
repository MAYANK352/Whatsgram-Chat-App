package com.mayank.whatsgram

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_new_user.*
import java.net.URI
import java.util.*

class NewUser : AppCompatActivity() {

    lateinit var alreadyacc:TextView
    lateinit var registerbutton:Button
    lateinit var emailnew:EditText
    lateinit var password:EditText
    lateinit var cnfpass:EditText
    lateinit var usernam:EditText
    lateinit var uploadimg:Button
    lateinit var circularImage:CircleImageView


    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_user)

        alreadyacc=findViewById(R.id.alreadyAccount)
        emailnew=findViewById(R.id.txtemail1)
        password=findViewById(R.id.txtpassword1)
        cnfpass=findViewById(R.id.txtrepassword)
        usernam=findViewById(R.id.txtusername)
        uploadimg=findViewById(R.id.uploadPic)
        circularImage=findViewById(R.id.circleimage)

        uploadimg.setOnClickListener{
            val intent=Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent,0)
        }

        auth=Firebase.auth

        registerbutton=findViewById(R.id.Registerbutton)
        registerbutton.setOnClickListener{
            performRegister()
        }

        checkBox2.setOnCheckedChangeListener{
                buttonView, isChecked ->
            if (isChecked){
                cnfpass.inputType=1
                password.inputType=1
            }else {
                cnfpass.inputType= InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                password.inputType= InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }


        alreadyacc.setOnClickListener {

            val intent = Intent(this@NewUser, MainActivity::class.java)
            startActivity(intent)
        }

    }
    var selectedPhotoUri:Uri ?= null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==0 && resultCode==Activity.RESULT_OK && data!=null){
            selectedPhotoUri =data.data
            val bitmap=MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            circularImage.setImageBitmap(bitmap)
            circularImage.alpha=1f
            uploadimg.visibility = View.INVISIBLE


//            val bitmapDrawable=BitmapDrawable(bitmap)
//
//            uploadimg.setBackgroundDrawable(bitmapDrawable)
        }

    }

    private fun performRegister(){
        val email = emailnew.text.toString()
        val pass= password.text.toString()
        val user=usernam.text.toString()
        val cnfpassword=cnfpass.text.toString()

        if(user.isEmpty() || email.isEmpty() || pass.isEmpty() || cnfpassword.isEmpty()){
            Toast.makeText(this@NewUser, "oops.. Fields are empty !!", Toast.LENGTH_SHORT)
                .show()
            return
        }
        if(pass != cnfpassword){
            Toast.makeText(this@NewUser, "Password Not Matched!", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,pass)
            .addOnCompleteListener{
                if(!it.isSuccessful) return@addOnCompleteListener
                Toast.makeText(this@NewUser, "Account Created", Toast.LENGTH_SHORT).show()


                        val intent=Intent(this,LatestMessagesActivity::class.java)
                        intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)


                uploadImageToStorage()


            }.addOnFailureListener{
                Toast.makeText(this@NewUser, "Failed to Register", Toast.LENGTH_SHORT).show()

            }
    }

    private fun uploadImageToStorage(){
        if(selectedPhotoUri == null) return

        val filename= UUID.randomUUID().toString()
        val ref=FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Toast.makeText(this@NewUser, "Image Uploaded ", Toast.LENGTH_SHORT).show()
            ref.downloadUrl.addOnSuccessListener {
               saveuserdataToDtatabase(it.toString())
            }
        }.addOnFailureListener{
            Toast.makeText(this@NewUser, "Not Uploaded", Toast.LENGTH_SHORT).show()
            Log.d("NewUser","#${it.message}")
        }



    }

    private fun saveuserdataToDtatabase(profileImageUrl: String) {
        val uid=FirebaseAuth.getInstance().uid?:""
        val ref=FirebaseDatabase.getInstance().getReference("/user/$uid")

        val user=User(uid, usernam.text.toString(),profileImageUrl)
        ref.setValue(user)

    }

}
@Parcelize
class User(val uid:String , val username:String , val profileImageUrl:String):Parcelable{
    constructor():this("","","")
}


