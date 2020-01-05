package moshiko.study.appupload;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import moshiko.study.appupload.notifications.Token;

public class DashboardActivity extends AppCompatActivity {

    public FirebaseAuth firebaseAuth;
    ActionBar actionBar;

    //Token
    String mUID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        firebaseAuth = FirebaseAuth.getInstance();

        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //Default Fragment Home
        actionBar.setTitle("Home");
        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction homeFt = getSupportFragmentManager().beginTransaction();
        homeFt.replace(R.id.content, homeFragment, "");
        homeFt.commit();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        checkUserStatus();


        //update token
        updateToken(FirebaseInstanceId.getInstance().getToken());


    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
        System.out.println("onResume");
    }

    public void updateToken(String token){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Token");
        Token mToken = new Token(token);
        databaseReference.child(mUID).setValue(mToken);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                //home fragment transaction
                case R.id.nav_home:
                    actionBar.setTitle("Home");
                    HomeFragment homeFragment = new HomeFragment();
                    FragmentTransaction homeFt = getSupportFragmentManager().beginTransaction();
                    homeFt.replace(R.id.content, homeFragment, "");
                    homeFt.commit();
                    return true;

                 //profile fragment transaction
                case R.id.nav_profile:
                    actionBar.setTitle("Profile");
                    ProfileFragment profileFragment = new ProfileFragment();
                    FragmentTransaction profileFt = getSupportFragmentManager().beginTransaction();
                    profileFt.replace(R.id.content, profileFragment, "");
                    profileFt.commit();
                    return true;

                 //user fragment transaction
                case R.id.nav_users:
                    actionBar.setTitle("Users");
                    UsersFragment usersFragment = new UsersFragment();
                    FragmentTransaction usersFt = getSupportFragmentManager().beginTransaction();
                    usersFt.replace(R.id.content, usersFragment, "");
                    usersFt.commit();
                    return true;

                case R.id.nav_chat:
                    actionBar.setTitle("Chats");
                    ChatListFragment chatListFragment= new ChatListFragment();
                    FragmentTransaction chatFt = getSupportFragmentManager().beginTransaction();
                    chatFt.replace(R.id.content, chatListFragment, "");
                    chatFt.commit();
                    return true;
            }

            return false;
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("onStart");

        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
           // FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            checkUserStatus();
        });

        //Get the current user?

    }

    public void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null){

            //Token
            mUID = user.getUid();
            SharedPreferences sharedPreferences = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();
            //System.out.println("User get in");
        }else{
            //Go to login activity
            startActivity(new Intent(this, LoginActivity.class ));
            finish();
        }

    }


    @Override
    //Here you save the data
    protected void onPause() {
        super.onPause();
        System.out.println("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save your state here
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //name = getSharedPreferences(this, private... )
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }

        if (id == R.id.action_settings){
            settingDialog();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    public void settingDialog(){
        String[] options = {"LogOut"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settings");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    firebaseAuth.signOut();
                }
            }
        });
        builder.create().show();


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DashboardActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }




}
