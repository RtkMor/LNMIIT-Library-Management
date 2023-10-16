package com.example.abcd;

import android.os.Bundle;

import com.example.abcd.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.example.abcd.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // Check if you should load the HomeFragment
        boolean shouldLoadHomeFragment = getIntent().getBooleanExtra("homeUpdated", false);
        if (shouldLoadHomeFragment) {
            loadHomeFragment();
        }

        // Check if you should load the ProfileFragment
        boolean shouldLoadProfileFragment = getIntent().getBooleanExtra("profileUpdated", false);
        if (shouldLoadProfileFragment) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            loadProfileFragment();
        }

    }

    private void loadHomeFragment() {
        navController.navigate(R.id.navigation_home);
    }

    private void loadProfileFragment() {
        navController.navigate(R.id.navigation_notifications);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}