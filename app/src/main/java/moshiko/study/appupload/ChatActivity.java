package moshiko.study.appupload;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import moshiko.study.appupload.adapters.AdapterChat;
import moshiko.study.appupload.models.ModelChat;
import moshiko.study.appupload.models.ModelUser;
import moshiko.study.appupload.notifications.APIService;
import moshiko.study.appupload.notifications.Client;
import moshiko.study.appupload.notifications.Data;
import moshiko.study.appupload.notifications.FirebaseMessaging;
import moshiko.study.appupload.notifications.Response;
import moshiko.study.appupload.notifications.Sender;
import moshiko.study.appupload.notifications.Token;
import retrofit2.Call;
import retrofit2.Callback;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    //view from xml
    Toolbar tbChat;
    RecyclerView recyclerView;
    ImageView ivProfile;
    TextView tvNameChat, tvStatus;
    EditText etMassage;
    ImageButton btnSend;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;

    ValueEventListener velSeen;
    DatabaseReference drUserSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;


    String outUserId;
    String localUserId;
    String outUserImage;

    //Token
    APIService apiService;
    boolean notify = false;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //init views
        Toolbar tbChat = findViewById(R.id.tbChat);
        setSupportActionBar(tbChat);
        tbChat.setTitle("");
        recyclerView = findViewById(R.id.rvChat);
        ivProfile = findViewById(R.id.ivProfile);
        tvNameChat = findViewById(R.id.tvNameChat);
        tvStatus = findViewById(R.id.tvStatus);
        etMassage = findViewById(R.id.etMassage);
        btnSend = findViewById(R.id.btnSend);

        //LinerLayout for RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //recycler view properties:
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //create api service
        apiService = Client.getRetrofit("http://fcm.googleapis.com/").create(APIService.class);

        //on click user from list we get user id
        //after get from user id the profile picture and full name

        Intent intent = getIntent();
        outUserId = intent.getStringExtra("outUserId");

        //auth instance
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference("Users");

        //Search user to get info

        Query userQuery = usersDbRef.orderByChild("userId").equalTo(outUserId);
        userQuery.addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check until required info received
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    //get data
                    String localUserId = "" + ds.child("userId").getValue();
                    String name ="" + ds.child("name").getValue();
                    outUserImage ="" + ds.child("image").getValue();
                    String typingStatus = "" + ds.child("typingTo").getValue();

                    //check typing status
                    if (typingStatus.equals(localUserId)){
                        tvStatus.setTextColor(getResources().getColor(R.color.grey));
                        tvStatus.setText("typing...");
                    }else {
                        //get value of online status
                        String onlineStatus = "" + ds.child("onlineStatus").getValue();
                        if (onlineStatus.equals("online")){
                            tvStatus.setText(onlineStatus);
                            tvStatus.setTextColor(getResources().getColor(R.color.green));
                        }else {
                            //convert timestamp
                            //convert time stamp to dd//mm/yyyy hh:mm am/pm
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",
                                    calendar).toString();
                            tvStatus.setTextColor(getResources().getColor(R.color.grey));
                            tvStatus.setText("Last seen " + dateTime);
                        }
                    }



//                    {
//                        /* Visit https://firebase.google.com/docs/database/security to learn more about security rules. */
//                        "rules": {
//                        ".read": "auth != null",
//                                ".write": "auth != null"
//                    }
//                    }

                    //set data
                    tvNameChat.setText(name);



                    try {
                        //set in ImageView in toolBar
                        Picasso.get().load(outUserImage).placeholder(R.drawable.ic_profile_white).into(ivProfile);
                    }catch (Exception e){
                        //Set default picture
                        Picasso.get().load(R.drawable.ic_profile_white).into(ivProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Click to send massage
        btnSend.setOnClickListener(view -> {
            notify = true;

            //get massage text
            String message = etMassage.getText().toString().trim();
            //check if empty text
            if (TextUtils.isEmpty(message)){
                //empty
                Toast.makeText(this, "Massage Empty...", Toast.LENGTH_SHORT).show();
            }else {
                //not empty
                sendMassage(message);
            }
            //clear the Edit Text after sending massage
            etMassage.setText("");

        });

        //check edit text change
        etMassage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().trim().length() == 0) {
                    checkOTypingStatus("noOne");
                }else {
                    checkOTypingStatus(localUserId);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        readMessages();

        seenMessage();

    }

    private void seenMessage() {
        drUserSeen = FirebaseDatabase.getInstance().getReference("Chats");
        velSeen = drUserSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);

                    if (chat == null || chat.getMessage().equals("This message was deleted.")){
                        continue;
                    }

                    if (chat.getReceiver().equals(localUserId) &&
                    chat.getSender().equals(outUserId)){
                        HashMap<String, Object> seenHashMap = new HashMap<>();
                        seenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(seenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Chats");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);

                    if (chat.getMessage().equals("This message was deleted.")){
                        continue;
                    }
                    boolean isSeen = (boolean)ds.child("isSeen").getValue();
                    chat.setSeen(isSeen);

                    if (chat.getReceiver().equals(localUserId) &&
                            chat.getSender().equals(outUserId) ||
                            chat.getReceiver().equals(outUserId) &&
                            chat.getSender().equals(localUserId)){
                        chatList.add(chat);
                    }
                }

                //adapter
                adapterChat = new AdapterChat(ChatActivity.this, chatList, outUserImage);
                adapterChat.notifyDataSetChanged();
                //set adapter to recycler view
                recyclerView.setAdapter(adapterChat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMassage(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", localUserId);
        hashMap.put("receiver", outUserId);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);
        databaseReference.child("Chats").push().setValue(hashMap);



        //Token
        String msg = message;
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users")
                .child(localUserId);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUser user = dataSnapshot.getValue(ModelUser.class);
                if (notify) {
                    sentNotification(outUserId, user.getName(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sentNotification(String outUserId, String name, String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(outUserId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(localUserId, name +":" + message, "New message",
                            outUserId,
                            R.drawable.ic_default_img);

                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender).enqueue(new Callback<Response>() {
                        @Override
                        public void onResponse(Call<Response> call, retrofit2.Response<Response>
                                response) {
                            Toast.makeText(ChatActivity.this, ""+response.message(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<Response> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            localUserId = user.getUid();
        }else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status){
        localUserId = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(localUserId);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);

        //update value online status
        databaseReference.updateChildren(hashMap);

    }

    private void checkOTypingStatus(String typing){
        localUserId = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(localUserId);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);

        //update value online status
        databaseReference.updateChildren(hashMap);

    }



    @Override
    protected void onStart() {
        checkOnlineStatus("online");
        checkUserStatus();
        super.onStart();
    }

    @Override
    protected void onPause() {

        //get last time online
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        checkOTypingStatus("noOne");
        drUserSeen.removeEventListener(velSeen);
        super.onPause();
    }


    @Override
    protected void onResume() {
        //set online
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //hide search view (not needed here).
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);



        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

//        int id = item.getItemId();
//        if (id == R.id.action_logout){
//            firebaseAuth.signOut();
//            String timestamp = String.valueOf(System.currentTimeMillis());
//            checkOnlineStatus(timestamp);
//            checkUserStatus();
//        }
        return super.onOptionsItemSelected(item);
    }
}
