package moshiko.study.appupload.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import moshiko.study.appupload.ChatActivity;
import moshiko.study.appupload.ChatListFragment;
import moshiko.study.appupload.ThereProfileActivity;
import moshiko.study.appupload.models.ModelUser;
import moshiko.study.appupload.R;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {


    Context context;
    List<ModelUser> userList;


    //Ctor:


    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //inflate layout rows
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        //get data
        String outUserId = userList.get(i).getUserId();
        String userImage = userList.get(i).getImage();
        String userName = userList.get(i).getName();
        String userEmoji = userList.get(i).getEmoji();
        String userEmail = userList.get(i).getEmail();


        //set data
        myHolder.tvNameUser.setText(userName);
        myHolder.tvEmojiUser.setText(userEmoji);

        try{
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img)
                    .into(myHolder.ivAvatarUser);

        }catch (Exception e){

        }

        //Handle item click
        myHolder.itemView.setOnClickListener(view -> {
            //show Dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0){
                        //profile clicked
                        Intent intent = new Intent(context, ThereProfileActivity.class);
                        intent.putExtra("userId", outUserId);
                        context.startActivity(intent);
                    }
                    if (which == 1){
                        //chat clicked
                        //start chat with click on user from list
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("outUserId", outUserId);
                        context.startActivity(intent);

                    }
                }
            });
            builder.create().show();

        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //View holder class

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView ivAvatarUser;
        TextView tvNameUser, tvEmojiUser;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            ivAvatarUser = itemView.findViewById(R.id.ivAvatarUser);
            tvNameUser = itemView.findViewById(R.id.tvNameUser);
            tvEmojiUser = itemView.findViewById(R.id.tvEmojiUser);


        }
    }
}
