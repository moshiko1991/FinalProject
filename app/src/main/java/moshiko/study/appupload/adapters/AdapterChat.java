package moshiko.study.appupload.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import moshiko.study.appupload.R;
import moshiko.study.appupload.models.ModelChat;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder>{

    private static  final int MSG_TYPE_LEFT = 0;
    private static  final int MSG_TYPE_RIGHT = 1;

    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    FirebaseUser firebaseUser;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layouts: left -> receiver, right -> sender (XML files)
        if (i == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, viewGroup,
                    false);
            return new MyHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, viewGroup,
                    false);
            return new MyHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        //get data
        String message = chatList.get(i).getMessage();
        String timesTamp = chatList.get(i).getTimestamp();


        //convert time stamp to dd//mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        // calendar.setTimeInMillis(Long.parseLong(timesTamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        myHolder.tvMessage.setText(message);
        myHolder.tvTime.setText(dateTime);

        try{
            Picasso.get().load(imageUrl).into(myHolder.ivProfile);
        }catch (Exception e){

        }

        //click to show delete dialog
        myHolder.massageLayout.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete");
            builder.setMessage("Delete this message?");
            //delete btn
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteMessage(i);
                }
            });
            //cancel delete btn
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        });



        //set seen/sent status of massage
        if (i == chatList.size()-1){
            if (chatList.get(i).isSeen()){
                //Toast.makeText(context, "check moshiko true", Toast.LENGTH_SHORT).show();
                myHolder.tvIsSeen.setText("Seen");
            }else {
                //Toast.makeText(context, "check moshiko false", Toast.LENGTH_SHORT).show();
                myHolder.tvIsSeen.setText("Sent");
            }
        }else {
            myHolder.tvIsSeen.setVisibility(View.GONE);
        }
    }

    private void deleteMessage(int position) {
        String localUserId = firebaseUser.getUid();


        String messageTimeStamp = chatList.get(position).getTimestamp();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Chats");
        Query query = databaseReference.orderByChild("timestamp").equalTo(messageTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
//                    ModelChat chat = ds.getValue(ModelChat.class);
//
//
//                    if (ds.child("isSeen").getValue().equals(null)){
//                        boolean isSeen = (boolean) ds.child("isSeen").getValue(true);
//                        chat.setSeen(isSeen);
//                        return;
//                    }

                    if (ds.child("sender").getValue().equals(localUserId)){
                        ds.getRef().removeValue();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message was deleted.");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context, "Message delete...", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(context, "You cant delete this message..", Toast.LENGTH_SHORT).show();
                    }




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get current user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }

    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {

        //views
        ImageView ivProfile;
        TextView tvMessage, tvTime, tvIsSeen;
        LinearLayout massageLayout;

        public  MyHolder(@NonNull View itemView){
            super(itemView);

            //init vies
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvIsSeen = itemView.findViewById(R.id.tvIsSeen);
            massageLayout = itemView.findViewById(R.id.messageLayout);
        }
    }
}
