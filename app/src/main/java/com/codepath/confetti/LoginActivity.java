package com.codepath.confetti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.confetti.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    public static final String TAG = "LoginActivity";
    public static final int VALID = 1;

    private ImageView ivLogo;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // init firebase auth
        mAuth = FirebaseAuth.getInstance();

        // init layout
        ivLogo = binding.ivLogo;
        etEmail = binding.etEmail;
        etPassword = binding.etPassword;
        btnLogin = binding.btnLogin;
        tvSignUp = binding.tvSignUp;

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: improve input checks
                // check input values
                if (checkEmailInput() != VALID) return;

                if (checkPasswordInput() != VALID) return;


            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSignUpActivity();
            }
        });
    }

    private void goToSignUpActivity() {
        Intent i = new Intent(this, SignUpActivity.class);
        startActivity(i);
    }

    private void goToMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    // returns VALID if valid password, 0 otherwise
    private int checkPasswordInput() {
        String password = etPassword.getText().toString().trim();
        if (password.isEmpty()) {
            Log.i(TAG, "Password is empty");
            etPassword.setError("Password is required!");
            etPassword.requestFocus();
            return 0;
        }
        return VALID;
    }

    private int checkEmailInput() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Log.i(TAG, "Email is empty");
            etEmail.setError("Email is required!");
            etEmail.requestFocus();
            return 0;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.i(TAG, "Email is invalid");
            etEmail.setError("Please provide valid email!");
            etEmail.requestFocus();
            return 0;
        }

        return VALID;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // check if user is signed in (non-null) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // TODO: go into app
        }
    }

    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // user signed in successfully!
                            Log.i(TAG, "onSuccess user sign in");
                            Toast.makeText(LoginActivity.this, "User signed in!", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            // pass user to main activity
                        } else {
                            // user sign in failed!
                            Log.i(TAG, "onFailure user sign in");
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            // do something
                        }
                    }
                });
    }
}