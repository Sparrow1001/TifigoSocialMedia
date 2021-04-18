package com.example.tifigosocialmedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    MaterialEditText mEmailEt, mPasswordEt;
    TextView notHaveAccntTv;
    Button mLoginBtn;

    private FirebaseAuth mAuth;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        notHaveAccntTv = findViewById(R.id.not_have_accountTv);
        mLoginBtn = findViewById(R.id.loginBtn);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailEt.getText().toString();
                String passw = mPasswordEt.getText().toString().trim();

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //invalid email
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }else {
                    //valid email
                    loginUser(email, passw);
                }

            }
        });

        notHaveAccntTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        pd = new ProgressDialog(this);
        pd.setMessage("Logging In...");

    }

    private void loginUser(String email, String passw) {
        pd.show();
        mAuth.signInWithEmailAndPassword(email, passw).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    pd.dismiss();
                    FirebaseUser user = mAuth.getCurrentUser();

                    startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                    finish();
                }else {
                    pd.dismiss();
                    Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}