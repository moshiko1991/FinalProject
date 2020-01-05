package moshiko.study.appupload;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    Button btnRegisterRA;
    EditText etEmailRA, etPasswordRA, etConfirmRA;
    TextView tvLoginBack;
    EditText etFirstName, etLastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnRegisterRA = findViewById(R.id.btnRegisterRA);
        etEmailRA = findViewById(R.id.etEmailRA);
        etPasswordRA = findViewById(R.id.etPasswordRA);
        etConfirmRA = findViewById(R.id.etConfimRA);
        tvLoginBack = findViewById(R.id.tvLoginBack);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);

        tvLoginBack.setOnClickListener(v ->openLoginActivity());

        btnRegisterRA.setOnClickListener(V->Register());


    }

    private void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    private OnSuccessListener<AuthResult> mSuccessListener =
            authResult -> {
                toggleProgress(false);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String email = user.getEmail();
                String userId = user.getUid();

                HashMap<Object, String> hashMap = new HashMap<>();

                hashMap.put("email", email);
                hashMap.put("userId", userId);
                hashMap.put("name", etFirstName.getText().toString() + " " + etLastName.getText().toString());
                hashMap.put("emoji", "");
                hashMap.put("image", "");
                hashMap.put("cover", "");

                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

                DatabaseReference reference = firebaseDatabase.getReference("Users");
                reference.child(userId).setValue(hashMap);

                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            };

    private OnFailureListener mFailureListener =e -> {
        toggleProgress(false);
        showError(e.getMessage());
    };

    private void Register() {
        if (!isEmailValid() | !isPasswordValid() | !isConfirmValid()) {

            return;
        }

        toggleProgress(true);
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(getEmail(), getPassword())
        .addOnSuccessListener(mSuccessListener)
                .addOnFailureListener(mFailureListener);


    }

    private String getEmail(){
        return etEmailRA.getText().toString();
    }

    private String getPassword(){
        return etPasswordRA.getText().toString();
    }

    private String getConfirm(){
        return etConfirmRA.getText().toString();
    }


    private boolean isEmailValid(){
        //getEmail().contains("@"); option with strings
        boolean valid = Patterns.EMAIL_ADDRESS.matcher(getEmail()).matches();

        if (!valid){
            etEmailRA.setError("Invalid email address");
        }else {
            etEmailRA.setError(null);
        }
        return valid;
    }


    private boolean isPasswordValid(){
         Boolean valid = getPassword().length() >= 6;
         if (!valid){
             etPasswordRA.setError("Password must contain at least 6 character.");
         }else {
             etPasswordRA.setError(null);
         }
         return valid;
    }

    private boolean isConfirmValid(){
        String password = getPassword();
        String confirm = getConfirm();
        Boolean valid = password.matches(confirm);
        if (!valid){
            etConfirmRA.setError("Passwords are not match");
        }else{
            etConfirmRA.setError(null);
        }
        return valid;
    }


    ProgressDialog progressDialog;
    private void toggleProgress(boolean show){
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

    private void showError(String massage){
        new AlertDialog.Builder(this).setTitle("Error").setMessage(massage)
                .setPositiveButton("OK", (dialog, which) -> {

                }).
                show();
    }
}
