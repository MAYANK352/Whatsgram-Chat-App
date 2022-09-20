package com.mayank.whatsgram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chatlog.*
import kotlinx.android.synthetic.main.sender_row.view.*
import kotlinx.android.synthetic.main.reciever_row.view.*


class ChatlogActivity : AppCompatActivity() {

    val adapter=GroupAdapter<ViewHolder>()
    var toUser:User?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatlog)

        recyclerView_Chatlog.adapter=adapter



//        val username=intent.getStringExtra(NewMessageActivity.USER_KEY)
        toUser=intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

            supportActionBar?.title=toUser?.username


      // setupDummyData()
        listenForMessages()
         sendbutton.setOnClickListener {

           performSendMessage()

       }
    }

    private fun listenForMessages() {
        val fromId=FirebaseAuth.getInstance().uid
        val toId=toUser?.uid
        val ref=FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage=snapshot.getValue(ChatMessage::class.java)
                if(chatMessage!=null){
                   if(chatMessage.fromId==FirebaseAuth.getInstance().uid){
                       val currentUser=LatestMessagesActivity.currentUser?:return
                       adapter.add(ChatToItem(chatMessage.text,currentUser))
                   }
                   else{

                       adapter.add(ChatFromItem(chatMessage.text,toUser))
                   }
                }
                recyclerView_Chatlog.scrollToPosition(adapter.itemCount-1)

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    class ChatMessage(val id:String,val text:String,val fromId:String,val toId:String,val timestamp: Long){
        constructor():this("","","","",-1)
    }
    private fun performSendMessage() {
       val text=enterMessage.text.toString()
        val fromId=FirebaseAuth.getInstance().uid
        val user=intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId=user?.uid

        //val reference=FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference=FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toreference=FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()
        if(fromId==null){
            return
        }
        val chatMessage = ChatMessage(reference.key!!,text,fromId,toId!!,System.currentTimeMillis()/1000)
        reference.setValue(chatMessage).addOnSuccessListener {
            enterMessage.text.clear()
            recyclerView_Chatlog.scrollToPosition(adapter.itemCount-1)

        }.addOnFailureListener{
            Toast.makeText(this, "Unable to Send", Toast.LENGTH_SHORT).show()
        }
        toreference.setValue(chatMessage).addOnSuccessListener {

        }.addOnFailureListener{
            Toast.makeText(this, "Unable to Send", Toast.LENGTH_SHORT).show()
        }


        val latestMessageRef=FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)
        val latestMessageToRef=FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }

//    private fun setupDummyData() {
//        val adapter=GroupAdapter<ViewHolder>()
//        adapter.add(ChatFromItem("hey "))
//        adapter.add(ChatToItem("huhuioeherllo "))
//        adapter.add(ChatFromItem("dughwbnc"))
//        adapter.add(ChatToItem("textmessage"))
//
//        recyclerView_Chatlog.adapter=adapter
//    }
}
class ChatToItem(val text:String, val user: User?):Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textViewto.text=text
        val uri=user?.profileImageUrl
        val targetImageView=viewHolder.itemView.imageViewto
        Picasso.get().load(uri).into(targetImageView)
    }


    override fun getLayout(): Int {
    return R.layout.sender_row
    }

}
class ChatFromItem(val text:String, val user: User?):Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textViewFrom.text=text

        val uri=user?.profileImageUrl
        val targetImageView=viewHolder.itemView.imageViewfrom
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.reciever_row
    }

}

