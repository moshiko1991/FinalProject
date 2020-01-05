package moshiko.study.appupload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    ActionBar actionBar;
    FirebaseAuth firebaseAuth;
    DatabaseReference userDataBaseRef;

    EditText edPostTitle, edPostDescription;
    ImageView ivPostImage;
    Button btnUploadPost;

    //user info
    String name,email, emogi, uid, imageDp;

    Uri image_uri = null;

    //progress bar
    ProgressDialog progressDialog;

    //permissions constance
    public static final int CAMERA_REQUEST_CODE = 100;
    public static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_CAMERA_REQUEST_CODE = 300;
    private static final int IMAGE_GALLERY_REQUEST_CODE = 400;

    //permissions array
    String cameraPermission[];
    String storagePermission[];




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");
        //enable back button in actionbar
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init permissions
        cameraPermission = new String[] {Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE};

        storagePermission = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressDialog = new ProgressDialog(this);


        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        actionBar.setSubtitle(name);

        //get info of current user for post
        userDataBaseRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userDataBaseRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    name = "" + ds.child("name").getValue();
                    emogi = "" + ds.child("emoji").getValue();
                    imageDp = "" + ds.child("image").getValue();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

                //init views
        edPostTitle = findViewById(R.id.edPostTitle);
        edPostDescription = findViewById(R.id.edPostDescription);
        ivPostImage = findViewById(R.id.ivPostImage);
        btnUploadPost = findViewById(R.id.btnUploadPost);

        //get image from camera or gallery
        ivPostImage.setOnClickListener(v -> {
            //image dialog
            showImageDialog();
        });

        //click listener upload button
        btnUploadPost.setOnClickListener(v -> {
            //get data of title and description
            String title = edPostTitle.getText().toString();
            String description = edPostDescription.getText().toString();
            if (TextUtils.isEmpty(title)){
                Toast.makeText(this, "Pleas Enter Title...",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(description)){
                Toast.makeText(this, "Pleas Enter Description...",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (image_uri == null){
                //post without image
                uploadData(title, description, "noImage");
            }else {
                //post with image
                uploadData(title, description, String.valueOf(image_uri));
            }
        });
    }

    private void uploadData(String title, String description, String uri) {
        progressDialog.setMessage("Publishing post...");
        progressDialog.show();

        //show upload time
        String timeStamp = String.valueOf(System.currentTimeMillis());

        String filePath = "Posts/" + "post_" + timeStamp;

        if (!uri.equals("noImage")){
            //post with image
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePath);
            ref.putFile(Uri.parse(uri))
                    .addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();


                        if (uriTask.isSuccessful()) {
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("userId", uid);
                            hashMap.put("userName", name);
                            hashMap.put("userEmoji", emogi);
                            hashMap.put("userDp", imageDp);
                            hashMap.put("postId", timeStamp);
                            hashMap.put("postTitle", title);
                            hashMap.put("postDescr", description);
                            hashMap.put("postImage", downloadUri.toString());
                            hashMap.put("postTime", timeStamp);


                            //path to store post data
                            DatabaseReference ref1 = FirebaseDatabase.getInstance()
                                    .getReference("Posts");
                            ref1.child(timeStamp).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.cancel();
                                            Toast.makeText(AddPostActivity.this
                                                    , "Post Published."
                                                    , Toast.LENGTH_SHORT).show();
                                            edPostTitle.setText("");
                                            edPostDescription.setText("");
                                            ivPostImage.setImageURI(null);
                                            image_uri = null;

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddPostActivity.this
                                            , "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this
                                , ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
            });

        }else {
            //post without image
            HashMap<Object, String> hashMap = new HashMap<>();
            hashMap.put("userId", uid);
            hashMap.put("userName", name);
            hashMap.put("userEmoji", emogi);
            hashMap.put("userDp", imageDp);
            hashMap.put("postId", timeStamp);
            hashMap.put("postTitle", title);
            hashMap.put("postDescr", description);
            hashMap.put("postImage", "noImage");
            hashMap.put("postTime", timeStamp);


            //path to store post data
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("Posts");
            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.cancel();
                            Toast.makeText(AddPostActivity.this
                                    , "Post Published."
                                    , Toast.LENGTH_SHORT).show();
                            edPostTitle.setText("");
                            edPostDescription.setText("");
                            ivPostImage.setImageURI(null);
                            image_uri = null;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this
                            , ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showImageDialog() {
        //camera and gallery options
        String[] options = {"Camera", "Gallery"};

        //set options dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chose Image from:");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    //camera
                    if (!checkCameraPermissions()){
                        requestCameraPermission();
                    }else {
                        pickFromCamera();
                    }
                }
                if (which == 1){
                    //gallery
                    if (!checkStoragePermissions()){
                        requestStoragePermission();
                    }else {
                         pickFromGallery();
                    }
                }
            }
        });

        builder.create().show();


    }

    private void pickFromGallery() {
        //intent pic from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_GALLERY_REQUEST_CODE);
    }

    private void pickFromCamera() {
        //intent pic from camera
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        image_uri = getContentResolver().insert(MediaStore.Images.Media
                .EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_CAMERA_REQUEST_CODE);
    }

    //storage
    private boolean checkStoragePermissions(){
        //if storage permission:
        //return true if enable
        //return false if disable
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    //camera
    private boolean checkCameraPermissions(){
        //if storage permission:
        //return true if enable
        //return false if disable
        boolean resultCamera = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

        boolean resultStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return resultCamera && resultStorage;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    public void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            email = user.getEmail();
            uid = user.getUid();
        }else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //back to previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                checkUserStatus();
                break;

            case R.id.action_settings:
                checkUserStatus();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //handle permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }
                }else {
                    Toast.makeText(this, "Camera and Galley permissions necessary...",
                            Toast.LENGTH_SHORT).show();
                }
            }break;

            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    Boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        pickFromGallery();
                    }else{
                        Toast.makeText(this, "Galley permission necessary...",
                                Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //After choose picture method

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_GALLERY_REQUEST_CODE) {
                //get uri of image that choose from gallery
                image_uri = data.getData();

                ivPostImage.setImageURI(image_uri);

            }
            if (requestCode == IMAGE_CAMERA_REQUEST_CODE) {
                //get uri of image that choose from camera
                ivPostImage.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
