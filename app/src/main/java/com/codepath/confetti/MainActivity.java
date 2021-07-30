package com.codepath.confetti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.codepath.confetti.databinding.ActivityMainBinding;
import com.codepath.confetti.fragments.NotesFragment;
import com.codepath.confetti.fragments.SettingsBottomSheetFragment;
import com.codepath.confetti.fragments.UploadFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

/**
 * Activity for viewing and creating notes
 */
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    public static final String TAG = "MainActivity";

    private Toolbar toolbar;

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

        // set up toolbar
        toolbar = binding.toolbar;
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        // set up bottom navigation
        bottomNavigationView = binding.bottomNavigation;
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.action_upload:
                        switchFragments(getString(R.string.upload_fragment), getString(R.string.notes_fragment));
                        break;
                    case R.id.action_home:
                    default:
                        switchFragments(getString(R.string.notes_fragment), getString(R.string.upload_fragment));
                        break;
                }
                return true;
            }
        });

        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Log.i(TAG, "settingsss");

            SettingsBottomSheetFragment tagFragment = new SettingsBottomSheetFragment();
            tagFragment.show(getSupportFragmentManager(), tagFragment.getTag());

            // to consume menu item
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Hides not seen fragments, shows current fragment
     * @param currentTag fragment tag for current fragment
     * @param hiddenTag fragment tag for hidden fragment
     */
    private void switchFragments(String currentTag, String hiddenTag) {
        if(fragmentManager.findFragmentByTag(currentTag) != null) {
            //if the fragment exists, show it.
            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag(currentTag)).commit();
        } else {
            //if the fragment does not exist, add it to fragment manager.
            Fragment fragment;
            if (currentTag.equals(getString(R.string.upload_fragment)))
                fragment = new UploadFragment();
            else
                fragment = new NotesFragment();

            fragmentManager.beginTransaction().add(R.id.flContainer, fragment, currentTag).commit();
        }

        // hides other fragment
        if(fragmentManager.findFragmentByTag(hiddenTag) != null){
            //if the other fragment is visible, hide it.
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(hiddenTag)).commit();
        }
    }
}