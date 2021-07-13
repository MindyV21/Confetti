package com.codepath.confetti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.codepath.confetti.databinding.ActivityMainBinding;
import com.codepath.confetti.databinding.ActivitySignUpBinding;
import com.codepath.confetti.fragments.NotesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    public static final String TAG = "MainActivity";

    final FragmentManager fragmentManager = getSupportFragmentManager();
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();
        Log.i(TAG, "" + mAuth.getCurrentUser().getDisplayName());

        bottomNavigationView = binding.bottomNavigation;

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch (item.getItemId()) {
                    case R.id.action_home:
                        fragment = new NotesFragment();
                        break;
                    case R.id.action_upload:
//                        fragment = new ComposeFragment();
                        break;
                    case R.id.action_profile:
                    default:
//                        fragment = new InstaProfileFragment();
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });

        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        Log.i(TAG, "User logging out");
        Toast.makeText(MainActivity.this, "Logging out", Toast.LENGTH_SHORT).show();
    }
}