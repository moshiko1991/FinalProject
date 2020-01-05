package moshiko.study.appupload;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import moshiko.study.appupload.adapters.AdapterPosts;
import moshiko.study.appupload.models.ModelPost;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    //FireBase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;





    //Path for cover and profile image
    String storagePath = "USERS_PROFILE_COVER_IMAGE/";


    //views from xml
    CircleImageView ivAvatar;
    ImageView ivCover;
    TextView tvName, tvEmoji;
    FloatingActionButton fabAccount;
    RecyclerView postsRecyclerView;

    //ProgressDialog
    ProgressDialog progressDialog;

    //Permissions constants private for only this activity and final for no changes
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_CAMERA_REQUEST_CODE = 300;
    private static final int IMAGE_GALLERY_REQUEST_CODE = 400;

    //Arrays Permissions
    String cameraPermission[];
    String storagePermission[];
    String profileOrCover;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String userId;

    //uri for picture
    Uri uriImage;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        //init Permission Arrays
        cameraPermission = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvName = view.findViewById(R.id.tvName);
        //tvEmail = view.findViewById(R.id.tvEmail);
        tvEmoji = view.findViewById(R.id.tvEmoji);
        ivCover = view.findViewById(R.id.ivCover);
        fabAccount = view.findViewById(R.id.fabAccount);
        postsRecyclerView = view.findViewById(R.id.rvPosts);

        ivAvatar.setOnClickListener(view1 -> editProfilePicture());
        ivCover.setOnClickListener(view1 -> editCoverPicture());

        //init ProgressDialog
        progressDialog = new ProgressDialog(getActivity());

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            //check until required data
                for (DataSnapshot ds : dataSnapshot.getChildren()){

                    //get data
                    String name = "" + ds.child("name").getValue();
                    //String email = "" + ds.child("email").getValue();
                    String emoji = "" + ds.child("emoji").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    //set data
                    tvName.setText(name);
                    //tvEmail.setText(email);
                    tvEmoji.setText(emoji);

                    try {
                        //if image is load then set
                        Picasso.get().load(image).into(ivAvatar);

                    }catch (Exception e){
                        //if not loaded set default
                        Picasso.get().load(R.drawable.profile).into(ivAvatar);
                    }

                    try {
                        //if image is load then set
                        Picasso.get().load(cover).into(ivCover);
                    }catch (Exception e){
                        //if not loaded set default
                        Picasso.get().load(R.drawable.ic_default_img).into(ivCover);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        fabAccount.setOnClickListener(v -> { showEditProfileDialog();});

        postList = new ArrayList<>();

        checkUserStatus();
        loadMyPosts();

        return view;
    }

    private void loadMyPosts() {
        postsRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postsRecyclerView.setLayoutManager(layoutManager);

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("userId").equalTo(userId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    postList.add(myPosts);

                    adapterPosts = new AdapterPosts(getActivity(), postList);

                    postsRecyclerView.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchMyPosts(String searchQuery) {
        postsRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postsRecyclerView.setLayoutManager(layoutManager);


        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("userId").equalTo(userId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if (myPosts.getPostTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    myPosts.getPostDescr().contains(searchQuery.toLowerCase())){
                        postList.add(myPosts);
                    }

                    adapterPosts = new AdapterPosts(getActivity(), postList);

                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    //storage
    private boolean checkStoragePermissions(){
        //if storage permission:
        //return true if enable
        //return false if disable
        boolean result = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        requestPermissions(storagePermission, STORAGE_REQUEST_CODE);
    }

    //camera
    private boolean checkCameraPermissions(){
        //if storage permission:
        //return true if enable
        //return false if disable
        boolean resultCamera = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

        boolean resultStorage = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return resultCamera && resultStorage;
    }

    private void requestCameraPermission(){
        requestPermissions(cameraPermission, CAMERA_REQUEST_CODE);
    }

    private void editProfilePicture(){
        //Alert Dialog

        progressDialog.setMessage("Updating Profile Picture");
        profileOrCover = "image";
        showImageDialog();

    }

    private void editCoverPicture(){
        //Alert Dialog

        profileOrCover = "cover";
        showImageDialog();
        progressDialog.setMessage("Updating Cover Picture");

    }



    private void showEditProfileDialog() {
        //Edit profile
        //1.Edit Profile Picture
        //1.Edit Cover Picture
        //1.Edit name
        //1.Edit emoji

        String[] options = {"Add Emoji"};

        //Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Set title
        builder.setTitle("Choose Action");
        //showImageDialog();

        //Set items To Dialog
        builder.setItems(options, (dialog, which) -> {
            //handle dialog items click
                //Edit emoji
                progressDialog.setMessage("Updating Emoji");
                showNameAndEmogiDialog("emoji");

        });
        //create and show dialog
        builder.create().show();
    }

    private void showNameAndEmogiDialog(String key) {
        //Costume Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + key);
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        //add edit Text
        EditText editText = new EditText(getActivity());
        editText.setHint("Enter " + key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);


        //button dialog adding for update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //get text from edit Text
                String value = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(value)){
                    progressDialog.show();

                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //update and dismiss progress

                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //filed dismiss
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Error " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    if (key.equals("emoji")){
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = databaseReference.orderByChild("userId").equalTo(userId);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    String child = ds.getKey();
                                    dataSnapshot.getRef().child(child).child("userEmoji").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }


                }else {
                    Toast.makeText(getActivity(), "Pleas Enter " + key,
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

        //button dialog adding for cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
    }

    private void showImageDialog() {
        //Dialog with camera and upload from gallery
        String options[] = {"Use gallery", "Take a picture"};

        //Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Set title
        builder.setTitle("Update photo:");
        //showImageDialog();

        //Set items To Dialog
        builder.setItems(options, (dialog, which) -> {
            //handle dialog items click
            if (which == 0){
                //Camera
                if (!checkCameraPermissions()){
                    requestCameraPermission();
                }else {
                    cameraPicture();
                }
            }else if (which == 1){
                //Gallery
                if (!checkStoragePermissions()){
                    requestStoragePermission();
                }else {
                    galleryPicture();
                }

            }
        });
        //create and show dialog
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //method for user that ask him to allow or not the permissions

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length > 0){
                    boolean cameraApproved = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageApproved = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;
                    if (cameraApproved && writeStorageApproved){
                        cameraPicture();
                    }else {
                        Toast.makeText(getActivity(),
                                "Pleas Enable Permissions",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE:{


                if (grantResults.length > 0){

                    boolean writeStorageApproved = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageApproved){
                        galleryPicture();
                    }else {
                        Toast.makeText(getActivity(), "Pleas Enable Permissions", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //After choose picture method
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_GALLERY_REQUEST_CODE){
                //get uri of image that choose from gallery
                uriImage = data.getData();

                uploadCoverPhoto(uriImage);

            }
            if (requestCode == IMAGE_CAMERA_REQUEST_CODE){
                //get uri of image that choose from camera
                uploadCoverPhoto(uriImage);

            }
        }



        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadCoverPhoto(Uri uri) {
        //show progress
        progressDialog.show();

        //Path and name of image to be stored in firebase
        String fileNameAndPath = storagePath + "" + profileOrCover + "_" + user.getUid();

        StorageReference uploadReference = storageReference.child(fileNameAndPath);
        uploadReference.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri = uriTask.getResult();

                //checking the upload
                if (uriTask.isSuccessful()){
                    //update url in users database
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(profileOrCover, downloadUri.toString());
                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //Success of user URL
                            //dismiss progress bar
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Image Updated...",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Error add URL
                            //dismiss progress bar
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Image Update Fail"
                                    , Toast.LENGTH_SHORT).show();

                        }
                    });

                    if (profileOrCover.equals("image")){
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = databaseReference.orderByChild("userId").equalTo(userId);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    String child = ds.getKey();
                                    dataSnapshot.getRef().child(child).child("userDp").setValue(downloadUri.toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                }else {
                    //Error
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(),"Error", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(),"Error" + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void galleryPicture() {
        Toast.makeText(getActivity(), "Gallery work", Toast.LENGTH_SHORT).show();
        //Intent of choose a picture from device camera
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp picture");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        //put image uri
        uriImage = getActivity().getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        //Intent to take a picture from camera
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, uriImage);
        startActivityForResult(intentCamera, IMAGE_CAMERA_REQUEST_CODE);
    }

    private void cameraPicture() {
        Toast.makeText(getActivity(), "Camera work", Toast.LENGTH_SHORT).show();
        //Intent of choose a picture from gallery
        Intent intentGallery = new Intent(Intent.ACTION_PICK);
        intentGallery.setType("image/*"); // * for in image directory
        startActivityForResult(intentGallery, IMAGE_GALLERY_REQUEST_CODE);
    }


    public void checkUserStatus(){
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            userId = user.getUid();
        }else {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //to show menu in fragment
        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)){
                    searchMyPosts(query);
                }else{
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)){
                    searchMyPosts(newText);
                }else{
                    loadMyPosts();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();
            if (id == R.id.action_logout){
                firebaseAuth.signOut();
                checkUserStatus();
            }
            if (id == R.id.action_add_post){
                startActivity(new Intent(getActivity(), AddPostActivity.class));
            }
            return super.onOptionsItemSelected(item);
        }


}
