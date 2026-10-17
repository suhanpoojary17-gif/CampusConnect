package com.campusconnect.app.navigation;

import android.app.Activity;
import android.content.Intent;

import com.campusconnect.app.StudentDashboardActivity;

public final class AppNavigator {

    private AppNavigator() {
        // Prevent instantiation.
    }

    public static void openStudentDashboard(Activity activity) {
        Intent intent = new Intent(
                activity,
                StudentDashboardActivity.class
        );

        activity.startActivity(intent);
        activity.finish();
    }
}