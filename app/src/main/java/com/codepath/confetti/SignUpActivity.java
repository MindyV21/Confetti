package com.codepath.confetti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.codepath.confetti.databinding.ActivityLoginBinding;
import com.codepath.confetti.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    public static final String TAG = "SignUpActivity";
    public static final int VALID = 1;

    private ImageView ivProfile;
    private EditText etFullName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySignUpBinding binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // init firebase auth
        mAuth = FirebaseAuth.getInstance();

        // init layout
        ivProfile = binding.ivProfile;
        etFullName = binding.etFullName;
        etEmail = binding.etEmail;
        etPassword = binding.etPassword;
        etConfirmPassword = binding.etConfirmPassword;
        btnCreateAccount = binding.btnCreateAccount;

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: improve input checks
                // check input values
                if (checkNameInput() != VALID) return;
                if (checkEmailInput() != VALID) return;
                if (checkPasswordsInput() != VALID) return;
            }
        });
    }

    private void goToMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    private int checkPasswordsInput() {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (password.isEmpty()) {
            Log.i(TAG, "Password is empty");
            etPassword.setError("Password is required!");
            etPassword.requestFocus();
            return 0;
        }

        if (confirmPassword.isEmpty()) {
            Log.i(TAG, "Confirm Password is empty");
            etConfirmPassword.setError("Confirm password is required!");
            etConfirmPassword.requestFocus();
            return 0;
        }

        if (password.length() < 8) {
            Log.i(TAG, "Password length < 8");
            etPassword.setError("Password length must be greater than 8!");
            etPassword.requestFocus();
            return 0;
        }

        if (password.equals(confirmPassword)) {
            Log.i(TAG, "Password != Confirm password");
            etConfirmPassword.setError("Passwords do not match!");
            etConfirmPassword.requestFocus();
            return 0;
        }

        return VALID;
    }

    private int checkNameInput() {
        String fullName = etFullName.getText().toString().trim();

        if (fullName.isEmpty()) {
            Log.i(TAG, "Full Name is empty");
            etFullName.setError("Full name is required!");
            etFullName.requestFocus();
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
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // TODO: go into app
        }
    }

    public void createUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // user signed in successfully!
                            Log.i(TAG, "onSuccess user create account");
                            Toast.makeText(SignUpActivity.this, "Account creation success!", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            // pass user to main activity
                        } else {
                            // user sign in failed!
                            Log.i(TAG, "onFailure user create account");
                            Toast.makeText(SignUpActivity.this, "Account creation failed.", Toast.LENGTH_SHORT).show();
                            // do something
                        }
                    }
                });
    }
}