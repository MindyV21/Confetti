package com.codepath.confetti;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.codepath.confetti.databinding.ActivityMainBinding;
import com.codepath.confetti.databinding.ActivitySignUpBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();
        Log.i(TAG, "" + mAuth.getCurrentUser());
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        Log.i(TAG, "User logging out");
        Toast.makeText(MainActivity.this, "Logging out", Toast.LENGTH_SHORT).show();
    }
}