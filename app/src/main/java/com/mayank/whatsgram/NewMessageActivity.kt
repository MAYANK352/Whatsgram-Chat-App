package com.mayank.whatsgram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*
import java.security.acl.Group
import java.util.*


class NewMessageActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title="Select User"
       // recyclerView_NewMessage.addItemDecoration(DividerItemDecoration(this,
            //DividerItemDecoration.VERTICAL))

      //  val adapter=GroupAdapter<ViewHolder>()


        fetchuserDetails()

    }
    companion object{
        val USER_KEY="USER_KEY"
    }
    private fun fetchuserDetails() {
     val ref=   FirebaseDatabase.getInstance().getReference("/user")
      ref.addListenerForSingleValueEvent(object: ValueEventListener {

          override fun onDataChange(snapshot: DataSnapshot) {
              val adapter= GroupAdapter<ViewHolder>()
              snapshot.children.forEach{
                  val user=it.getValue(User::class.java)
                  if(user!=null)
                  adapter.add(UserItem(user))

                  adapter.setOnItemClickListener { item, view ->
                      val userItem=item as UserItem
                      val intent= Intent(view.context,ChatlogActivity::class.java)
                     // intent.putExtra(USER_KEY,userItem.user.username)
                      intent.putExtra(USER_KEY,userItem.user)
                      startActivity(intent)
                      finish()
                  }

              }
              recyclerView_NewMessage.adapter=adapter
          }

          override fun onCancelled(error: DatabaseError) {

          }

      })

    }
}

class UserItem(val user:User): Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int) {

   viewHolder.itemView.username_textview_new_message.text=user.username

        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageView2)





    }
    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }



}