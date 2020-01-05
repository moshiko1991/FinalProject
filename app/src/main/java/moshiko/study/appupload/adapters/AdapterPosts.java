package moshiko.study.appupload.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import moshiko.study.appupload.R;
import moshiko.study.appupload.ThereProfileActivity;
import moshiko.study.appupload.models.ModelPost;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.myHolder>{

    Context context;
    List<ModelPost> postList;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout row_posts.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts,
                viewGroup, false);


        return new myHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder myHolder, int position) {
        //get data
        String userId = postList.get(position).getUserId();
        String userName = postList.get(position).getUserName();
        String userEmoji = postList.get(position).getUserEmoji();
        String userDp = postList.get(position).getUserDp();
        String postId = postList.get(position).getPostId();
        String postTitle = postList.get(position).getPostTitle();
        String postDescr = postList.get(position).getPostDescr();
        String postImage = postList.get(position).getPostImage();
        String postTimeStemp = postList.get(position).getPostTime();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(postTimeStemp));
        String postTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //set data
        myHolder.tvUserName.setText(userName + " " + userEmoji);
        myHolder.tvPostTime.setText(postTime);
        myHolder.tvPostTitle.setText(postTitle);
        myHolder.tvPostDescription.setText(postDescr);

        //set user dp

        try {
            Picasso.get().load(userDp)
                    .into(myHolder.ivUserPhoto);
        }catch (Exception e){

        }

        //set post image
        //if no image
        if (postImage.equals("noImage")){
            myHolder.ivPostImage.setVisibility(View.GONE);
        }else {
            try {
                Picasso.get().load(postImage)
                        .into(myHolder.ivPostImage);
            }catch (Exception e){

            }
        }


        //handle button click
        //More
        myHolder.btnMore.setOnClickListener(v -> {
        //will implement later
            Toast.makeText(context, "More", Toast.LENGTH_SHORT).show();
        });

        //Like
        myHolder.btnLike.setOnClickListener(v -> {
            Toast.makeText(context, "Like", Toast.LENGTH_SHORT).show();
        });

        //Comment
        myHolder.btnComment.setOnClickListener(v -> {
            //will implement later
            Toast.makeText(context, "Comment", Toast.LENGTH_SHORT).show();
        });

        //Share
        myHolder.btnShare.setOnClickListener(v -> {
            //will implement later
            Toast.makeText(context, "Share", Toast.LENGTH_SHORT).show();
        });

        //profileLayout
        myHolder.profileLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, ThereProfileActivity.class);
            intent.putExtra("userId", userId);
            context.startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //inner class view holder
    class myHolder extends RecyclerView.ViewHolder{
        //view from row_posts.xml

        ImageView ivUserPhoto;
        ImageView ivPostImage;

        TextView tvUserName, tvPostTime, tvPostTitle, tvPostDescription, tvPostLike;

        ImageButton btnMore;
        Button btnLike, btnComment, btnShare;
        LinearLayout profileLayout;

        public myHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            ivUserPhoto = itemView.findViewById(R.id.ivUserPhoto);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvPostTime = itemView.findViewById(R.id.tvPostTime);
            tvPostTitle = itemView.findViewById(R.id.tvPostTitle);
            tvPostDescription = itemView.findViewById(R.id.tvPostDescription);
            btnMore = itemView.findViewById(R.id.btnMore);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
            profileLayout = itemView.findViewById(R.id.profileLayout);
        }
    }
}
