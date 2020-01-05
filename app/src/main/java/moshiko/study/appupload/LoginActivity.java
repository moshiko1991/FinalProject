package moshiko.study.appupload;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;


public class LoginActivity extends AppCompatActivity {


    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient googleSignInClient;

    Button btnLogin;
    ImageView btnGoogle;
    TextView tvRegister, tvRecover;
    EditText etEmail,etPassword;
    StorageReference storageReference;
    StorageReference coverReference;
    StorageReference imageReference;
    StorageReference emojiReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin = findViewById(R.id.btnLogin);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvRegister = findViewById(R.id.tvRegister);
        tvRecover = findViewById(R.id.tvRecover);
        btnGoogle = findViewById(R.id.btnGoogle);


        tvRegister.setOnClickListener(v -> openRegisterActivity());

        tvRecover.setOnClickListener(v -> showRecoverPasswordDialog());

        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        //1. Lamda for button whit method inside;
        btnLogin.setOnClickListener(V->Login());
    }

    private void showRecoverPasswordDialog() {
        //Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        //Set LinerLayout
        LinearLayout linearLayout = new LinearLayout(this);

        EditText etEmail = new EditText(this);
        etEmail.setHint("Email");
        etEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        linearLayout.addView(etEmail);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        //Buttons recover
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Input email for recover
                String email = etEmail.getText().toString().trim();
                beginRecovery(email);
            }
        });

        //Buttons cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Dismiss Dialog
                dialog.dismiss();

            }
        });

        //Shoe Dialog
        builder.create().show();

    }

    private void beginRecovery(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Toast.makeText(this, "Recover Email sent",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Failed...",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            //Error masage
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private OnSuccessListener<AuthResult> mSuccessListener =
            authResult -> {
                toggleProgress(false);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                //if user signing first time show user info from google account
                if(authResult.getAdditionalUserInfo().isNewUser()){

                    String email = user.getEmail();
                    String userId = user.getUid();

                    HashMap<Object, String> hashMap = new HashMap<>();

                    hashMap.put("email", email);
                    hashMap.put("userId", userId);
                    hashMap.put("name", "");
                    hashMap.put("emoji", "");
                    hashMap.put("image", "");
                    hashMap.put("cover", "");

                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

                    DatabaseReference reference = firebaseDatabase.getReference("Users");
                    reference.child(userId).setValue(hashMap);
                }



                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            };

    private OnFailureListener mFailureListener = e -> {
        toggleProgress(false);
        showError(e.getMessage());
    };

    private void Login() {
        if (!isEmailValid() | !isPasswordValid()){
            return;
        }
        toggleProgress(true);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(getEmail(), getPassword())
                .addOnSuccessListener(mSuccessListener)
                .addOnFailureListener(mFailureListener);

    }

    //2. getEmail and getPassword Strings
    private String getEmail(){
        return etEmail.getText().toString();
    }

    private String getPassword(){
        return etPassword.getText().toString();
    }

    //3. Email if valid
    private boolean isEmailValid(){
        //getEmail().contains("@");
        boolean valid = Patterns.EMAIL_ADDRESS.matcher(getEmail()).matches();
        if (!valid){
            etEmail.setError("Invalid Email");
        }else {
            etEmail.setError(null);
        }
        return valid;
    }


    //4. Password if valid
    public boolean isPasswordValid(){
        boolean valid = getPassword().length() >= 6;

        if (!valid){
            etPassword.setError("Password must contain t least 6  Characters");
        }else {
            etPassword.setError(null );
        }
        return valid;
    }


    //5. Show progress method

    ProgressDialog progressDialog;
    private void toggleProgress(Boolean show){
        //Lazy loading
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Login in");
            progressDialog.setMessage("Please Wait...");
        }

        if (show){
            progressDialog.show();
        }else {
            progressDialog.dismiss();
        }
    }

    //6. Show Error is something wrong.
    private void showError(String massage){
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(massage)
                .setPositiveButton("OK", (dialog, which) -> {

        })
                .show();
    }

    private void openRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);


            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);


            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Error: " + e.getMessage(),Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }



    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {


        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String email = user.getEmail();
                            String userId = user.getUid();
                            String userName = user.getDisplayName();

                            HashMap<Object, String> hashMap = new HashMap<>();

                            hashMap.put("email", email);
                            hashMap.put("userId", userId);
                            hashMap.put("name", userName);
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("typingTo", "onOne");
                            hashMap.put("emoji", "");
                            hashMap.put("image", "");
                            hashMap.put("cover", "");

                            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

                            DatabaseReference reference = firebaseDatabase.getReference("Users");
                            reference.child(userId).setValue(hashMap);

                            Toast.makeText(LoginActivity.this,
                                    user.getEmail(),
                                    Toast.LENGTH_SHORT).show();


                            startActivity(new Intent(LoginActivity.this
                                    , DashboardActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this,
                                    "Login Failed",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }


                    }
                });
    }









}
