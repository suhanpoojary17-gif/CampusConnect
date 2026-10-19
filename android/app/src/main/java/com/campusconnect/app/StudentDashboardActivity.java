package com.campusconnect.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.campusconnect.app.core.theme.ThemeManager;
import com.campusconnect.app.features.academics.AcademicsFragment;
import com.campusconnect.app.features.campus.CampusFragment;
import com.campusconnect.app.features.home.HomeDashboardFragment;

public class StudentDashboardActivity extends AppCompatActivity
        implements HomeDashboardFragment.NavigationListener {

    private static final String PREFS_NAME = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String ROLE_KEY = "user_role";
    private static final String SELECTED_DESTINATION = "selected_destination";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // Custom Navigation Layout Views
    private View navHome, navAcademics, navCampus;
    private ImageView iconHome, iconAcademics, iconCampus;
    private TextView textHome, textAcademics, textCampus;

    private int activeColor;
    private int inactiveColor;
    private int currentTabIdx = 0;

    @Override
    public void onHomeCardSelected(int destinationId) {
        // Map old BottomNavigationView destination IDs to custom tab indexes
        if (destinationId == R.id.nav_home) {
            selectTab(0, true);
        } else if (destinationId == R.id.nav_academics) {
            selectTab(1, true);
        } else if (destinationId == R.id.nav_campus) {
            selectTab(2, true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        if (!hasStudentSession()) {
            returnToLogin();
            return;
        }

        setContentView(R.layout.activity_student_dashboard);

        // Initialize theme colors
        activeColor = Color.parseColor("#FF5722");   // Vibrant Orange
        inactiveColor = Color.parseColor("#718096"); // Muted Gray

        // Bind custom navigation views from layout
        navHome = findViewById(R.id.navHome);
        navAcademics = findViewById(R.id.navAcademics);
        navCampus = findViewById(R.id.navCampus);

        iconHome = findViewById(R.id.iconHome);
        iconAcademics = findViewById(R.id.iconAcademics);
        iconCampus = findViewById(R.id.iconCampus);

        textHome = findViewById(R.id.textHome);
        textAcademics = findViewById(R.id.textAcademics);
        textCampus = findViewById(R.id.textCampus);

        // Set click listeners for the pill tabs
        navHome.setOnClickListener(v -> selectTab(0, true));
        navAcademics.setOnClickListener(v -> selectTab(1, true));
        navCampus.setOnClickListener(v -> selectTab(2, true));

        // Restore or initialize default tab state
        if (savedInstanceState == null) {
            selectTab(0, true);
        } else {
            int savedIndex = savedInstanceState.getInt(SELECTED_DESTINATION, 0);
            selectTab(savedIndex, false);
        }
    }

    private void selectTab(int tabIndex, boolean loadFragment) {
        currentTabIdx = tabIndex;

        // Reset all tabs to inactive state
        setTabState(iconHome, textHome, false);
        setTabState(iconAcademics, textAcademics, false);
        setTabState(iconCampus, textCampus, false);

        Fragment selectedFragment;

        switch (tabIndex) {
            case 0:
            default:
                selectedFragment = new HomeDashboardFragment();
                setTabState(iconHome, textHome, true);
                break;
            case 1:
                selectedFragment = new AcademicsFragment();
                setTabState(iconAcademics, textAcademics, true);
                break;
            case 2:
                selectedFragment = new CampusFragment();
                setTabState(iconCampus, textCampus, true);
                break;
        }

        if (loadFragment) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, selectedFragment)
                    .commit();
        }
    }

    private void setTabState(ImageView icon, TextView text, boolean isActive) {
        int color = isActive ? activeColor : inactiveColor;
        icon.setImageTintList(ColorStateList.valueOf(color));
        text.setTextColor(color);
        
        // Slight scale emphasis on the active icon
        icon.setScaleX(isActive ? 1.15f : 1.0f);
        icon.setScaleY(isActive ? 1.15f : 1.0f);
    }

    private boolean hasStudentSession() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String token = preferences.getString(TOKEN_KEY, null);
        String role = preferences.getString(ROLE_KEY, null);

        return token != null
                && !token.trim().isEmpty()
                && "STUDENT".equalsIgnoreCase(role);
    }

    // Required stubs to satisfy calls from NotificationsFragment
    public void refreshUnreadBadge() {
        // No-op
    }

    public void updateUnreadBadge(long unreadCount) {
        // No-op
    }

    public void updateUnreadBadge(int unreadCount) {
        // No-op
    }

    private void returnToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(SELECTED_DESTINATION, currentTabIdx);
        super.onSaveInstanceState(outState);
    }
}