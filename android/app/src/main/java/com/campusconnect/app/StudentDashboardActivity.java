package com.campusconnect.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.campusconnect.app.features.home.HomeDashboardFragment;
import com.campusconnect.app.features.navigation.DestinationFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.campusconnect.app.features.academics.AcademicsFragment;

public class StudentDashboardActivity extends AppCompatActivity
        implements HomeDashboardFragment.NavigationListener {

    private static final String PREFS_NAME = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String ROLE_KEY = "user_role";
    private static final String SELECTED_DESTINATION = "selected_destination";

    private BottomNavigationView bottomNavigation;

    @Override
    public void onHomeCardSelected(int destinationId) {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(destinationId);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!hasStudentSession()) {
            returnToLogin();
            return;
        }

        setContentView(R.layout.activity_student_dashboard);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setOnItemSelectedListener(item -> {
            showDestination(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        } else {
            int selectedItemId = savedInstanceState.getInt(
                    SELECTED_DESTINATION,
                    R.id.nav_home
            );

            bottomNavigation.setSelectedItemId(selectedItemId);
        }
    }

    private boolean hasStudentSession() {
        SharedPreferences preferences = getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
        );

        String token = preferences.getString(TOKEN_KEY, null);
        String role = preferences.getString(ROLE_KEY, null);

        return token != null
                && !token.trim().isEmpty()
                && "STUDENT".equalsIgnoreCase(role);
    }

    private void showDestination(int itemId) {
        Fragment fragment;

        if (itemId == R.id.nav_home) {
            fragment = new HomeDashboardFragment();
        } else if (itemId == R.id.nav_academics) {
            fragment = new AcademicsFragment();
        } else if (itemId == R.id.nav_campus) {
            fragment = DestinationFragment.newInstance(
                    "Campus",
                    "Digital bus pass, facilities and feedback."
            );
        } else if (itemId == R.id.nav_notifications) {
            fragment = DestinationFragment.newInstance(
                    "Notifications",
                    "Stay updated with campus notices and notifications."
            );
        } else if (itemId == R.id.nav_profile) {
            fragment = DestinationFragment.newInstance(
                    "Profile",
                    "Your student profile and account settings."
            );
        } else {
            return;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        fragment
                )
                .commit();
    }

    private void returnToLogin() {
        Intent intent = new Intent(
                this,
                MainActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(
            @NonNull Bundle outState
    ) {
        if (bottomNavigation != null) {
            outState.putInt(
                    SELECTED_DESTINATION,
                    bottomNavigation.getSelectedItemId()
            );
        }

        super.onSaveInstanceState(outState);
    }
}