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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.confetti.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity for logging in
 */
public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;

    public static final int VALID = 1;
    public static final int INVALID = 0;

    private ImageView ivLogo;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvSignUp;
    private ProgressBar pbLoading;

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
        pbLoading = binding.pbLoading;

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check input field values
                if (checkEmailInput() != VALID) return;
                if (checkPasswordInput() != VALID) return;

                pbLoading.setVisibility(View.VISIBLE);
                loginUser();
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSignUpActivity();
            }
        });
    }

    /**
     * Intent to go to SignUpActivity
     */
    private void goToSignUpActivity() {
        clearFields();
        Intent i = new Intent(this, SignUpActivity.class);
        startActivity(i);
    }

    /**
     * Intent to go to a user's account
     */
    private void goToMainActivity() {
        clearFields();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    /**
     * Check for password input
     * @return VALID if valid password, INVALID otherwise
     */
    private int checkPasswordInput() {
        String password = etPassword.getText().toString().trim();
        if (password.isEmpty()) {
            Log.i(TAG, "Password is empty");
            etPassword.setError("Password is required!");
            etPassword.requestFocus();
            return INVALID;
        }
        return VALID;
    }

    /**
     * Check for email input
     * @return VALID if valid password, INVALID otherwise
     */
    private int checkEmailInput() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Log.i(TAG, "Email is empty");
            etEmail.setError("Email is required!");
            etEmail.requestFocus();
            return INVALID;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.i(TAG, "Email is invalid");
            etEmail.setError("Please provide valid email!");
            etEmail.requestFocus();
            return INVALID;
        }

        return VALID;
    }

    /**
     * Set up app, checks if a user is logged in
     */
    @Override
    protected void onStart() {
        super.onStart();
        // check if user is signed in (non-null) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Log.i(TAG, "user already logged in");
            goToMainActivity();
        }
    }

    /**
     * Logs in user with Firebase Auth
     */
    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // user signed in successfully!
                            Log.i(TAG, "onSuccess user sign in");
                            Toast.makeText(LoginActivity.this, "User signed in!", Toast.LENGTH_SHORT).show();
                            pbLoading.setVisibility(View.GONE);
                            goToMainActivity();
                        } else {
                            // user sign in failed!
                            Log.i(TAG, "onFailure user sign in");
                            Toast.makeText(LoginActivity.this, "Failed to login! Please check your credentials.", Toast.LENGTH_LONG).show();
                            pbLoading.setVisibility(View.GONE);
                        }
                    }
                });
    }

    /**
     * Clears input fields
     */
    private void clearFields() {
        etEmail.setText("");
        etPassword.setText("");
    }
}