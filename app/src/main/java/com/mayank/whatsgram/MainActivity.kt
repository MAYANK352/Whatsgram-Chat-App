package com.mayank.whatsgram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  lateinit var newuser:TextView
  lateinit var email:EditText
  lateinit var Password:EditText
  lateinit var Login:Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        email=findViewById(R.id.txtemail)
        Password=findViewById(R.id.txtpassword)
        Login=findViewById(R.id.loginbutton)

        newuser=findViewById(R.id.Newuser)

        Login.setOnClickListener{
            val mail = email.text.toString()
            val passw= Password.text.toString()

            if(mail.isEmpty() || passw.isEmpty()){
                Toast.makeText(this@MainActivity, "Fields are Empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            FirebaseAuth.getInstance().signInWithEmailAndPassword(mail,passw).addOnSuccessListener{
                val intent=Intent(this@MainActivity,LatestMessagesActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }.addOnFailureListener{
                Toast.makeText(this@MainActivity, "Incorrect Username or Password", Toast.LENGTH_SHORT).show()
                return@addOnFailureListener
            }
        }


        newuser.setOnClickListener {

            val intent = Intent(this@MainActivity, NewUser::class.java)
            startActivity(intent)
        }
        checkBox.setOnCheckedChangeListener{
                buttonView, isChecked ->
            if (isChecked){
                Password.inputType=1
            }else {
                Password.inputType= InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }


    }

}