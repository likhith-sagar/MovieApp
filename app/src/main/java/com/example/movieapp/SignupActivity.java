package com.example.movieapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {
    EditText email, password;
    Button btn;
    TextView tview;
    LoadingDialog loadingDialog;
    FirebaseAuth mFirebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        loadingDialog = new LoadingDialog(SignupActivity.this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btn = findViewById(R.id.button);
        tview = findViewById(R.id.textView2);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eText = email.getText().toString();
                String pText = password.getText().toString();
                if(eText.isEmpty()){
                    email.setError("Please enter Email id");
                    email.requestFocus();
                    return;
                }
                if(pText.isEmpty()){
                    password.setError("Please enter valid password");
                    password.requestFocus();
                    return;
                }
                loadingDialog.startLoading();
                mFirebaseAuth.createUserWithEmailAndPassword(eText, pText).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(SignupActivity.this, "SignUp failed, try again!", Toast.LENGTH_SHORT).show();
                            loadingDialog.stopLoading(false);
                        }
                        else {
                            Intent i = new Intent(SignupActivity.this, SetProfileActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            loadingDialog.stopLoading(true);
                            startActivity(i);
                        }
                    }
                });
            }
        });

        tview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignupActivity.this, MainActivity.class);
                i.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });
    }
}