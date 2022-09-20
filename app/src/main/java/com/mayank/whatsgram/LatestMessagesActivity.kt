package com.mayank.whatsgram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.latest_message_row.view.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.log

class LatestMessagesActivity : AppCompatActivity() {

    companion object{
        var currentUser:User?=null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_latest_messages)
        recyclerView_Latest_messages.adapter=adapter
       // recyclerView_Latest_messages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        // set itemclicklistner to your adapter
        adapter.setOnItemClickListener{ item,view->
            val intent=Intent(this@LatestMessagesActivity,ChatlogActivity::class.java)

            val row=item as LatestMesssageRow
            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartnerUser)
            startActivity(intent)

        }

        listenForLatestMessages()
       // setUpDummyRows()
        fetchCurrentUser()

        verifyUserIsLoggedIn()




    }

    val latestMessagesMap=HashMap<String,ChatlogActivity.ChatMessage>()

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessagesMap.values.forEach{
            adapter.add(LatestMesssageRow(it))
        }
    }


    private fun listenForLatestMessages() {
        val fromId=FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object :ChildEventListener
        {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
              val chatMessage=snapshot.getValue(ChatlogActivity.ChatMessage::class.java)?:return

                latestMessagesMap[snapshot.key!!]=chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage=snapshot.getValue(ChatlogActivity.ChatMessage::class.java)?:return
                latestMessagesMap[snapshot.key!!]=chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    val adapter= GroupAdapter<ViewHolder>()
//
    class LatestMesssageRow(val chatMessage:ChatlogActivity.ChatMessage):Item<ViewHolder>() {

        var chatPartnerUser:User?=null
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.textView2.text=chatMessage.text
            val chatPartnerId:String
            if(chatMessage.fromId==FirebaseAuth.getInstance().uid){
                chatPartnerId=chatMessage.toId
            }else{
                chatPartnerId=chatMessage.fromId
            }
            val ref = FirebaseDatabase.getInstance().getReference("/user/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatPartnerUser=snapshot.getValue(User::class.java)
                    viewHolder.itemView.textView_username_latest_message.text=chatPartnerUser?.username

                    val targetImageview=viewHolder.itemView.imageView3
                    Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageview)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }

        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }
    }


//    private fun setUpDummyRows() {
//        adapter.add(LatestMesssageRow())
//        adapter.add(LatestMesssageRow())
//        adapter.add(LatestMesssageRow())
//        adapter.add(LatestMesssageRow())
//        adapter.add(LatestMesssageRow())
//
//
//    }

    private fun fetchCurrentUser() {
        val uid=FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance().getReference("/user/$uid")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
            currentUser=snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun verifyUserIsLoggedIn() {
        val uid=  FirebaseAuth.getInstance().uid

        if(uid==null){
            val intent= Intent(this@LatestMessagesActivity,MainActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            R.id.New_message->{
                val intent=Intent(this@LatestMessagesActivity,NewMessageActivity::class.java)
                startActivity(intent)
            }

            R.id.signoutmenu->{
                FirebaseAuth.getInstance().signOut()
                val intent= Intent(this@LatestMessagesActivity,MainActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.nav_menu,menu)

        return super.onCreateOptionsMenu(menu)
    }
}