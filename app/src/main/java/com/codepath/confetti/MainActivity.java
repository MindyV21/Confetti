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
import com.codepath.confetti.fragments.SettingsFragment;
import com.codepath.confetti.fragments.UploadFragment;
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
        Log.i(TAG, "" + mAuth.getCurrentUser());

        bottomNavigationView = binding.bottomNavigation;
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.action_settings:
                        switchFragments("settings", "upload", "notes");
                        break;
                    case R.id.action_upload:
                        switchFragments("upload", "settings", "notes");
                        break;
                    case R.id.action_home:
                    default:
                        switchFragments("notes", "upload", "settings");
                        break;
                }
//                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });

        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    private void switchFragments(String currentTag, String hiddenTagOne, String hiddenTagTwo) {
        if(fragmentManager.findFragmentByTag(currentTag) != null) {
            //if the fragment exists, show it.
            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag(currentTag)).commit();
        } else {
            //if the fragment does not exist, add it to fragment manager.
            Fragment fragment;
            if (currentTag.equals("settings"))
                fragment = new SettingsFragment();
            else if (currentTag.equals("upload"))
                fragment = new UploadFragment();
            else
                fragment = new NotesFragment();

            fragmentManager.beginTransaction().add(R.id.flContainer, fragment, currentTag).commit();
        }
        if(fragmentManager.findFragmentByTag(hiddenTagOne) != null){
            //if the other fragment is visible, hide it.
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(hiddenTagOne)).commit();
        }
        if(fragmentManager.findFragmentByTag(hiddenTagTwo) != null){
            //if the other fragment is visible, hide it.
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(hiddenTagTwo)).commit();
        }
    }
}