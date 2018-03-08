package com.example.justin.freebies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private Button buttonLogIn;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignup;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() != null) {
            finish();
            startActivity(new Intent(this, MainPage.class));
        }

        progressDialog = new ProgressDialog(this);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonLogIn = (Button) findViewById(R.id.buttonLogIn);
        textViewSignup = (TextView) findViewById(R.id.textViewSignin);

        buttonLogIn.setOnClickListener(this);
        textViewSignup.setOnClickListener(this);
    }

    private void userLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        if(TextUtils.isEmpty(email))    {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(password))    {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Registering...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()) {
                            finish();
                            startActivity(new Intent(getApplicationContext(), MainPage.class));
                        }
                        else {

                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {

        if(view == buttonLogIn) {
            userLogin();
        }

        if(view == textViewSignup) {
            finish();
            Intent i = new Intent(this, StartUpLogin.class);
            startActivity(i);
        }
    }
}
