package com.campusconnect.app.features.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.campusconnect.app.R;
import com.campusconnect.app.features.notifications.NotificationsFragment;
import com.campusconnect.app.features.profile.StudentProfileFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.campusconnect.app.features.academics.AttendanceFragment;
import com.campusconnect.app.features.academics.TimetableFragment;
import com.campusconnect.app.features.academics.AssignmentsFragment;
import com.campusconnect.app.features.academics.AcademicPerformanceFragment;

public class HomeDashboardFragment extends Fragment {

    public interface NavigationListener {
        void onHomeCardSelected(int destinationId);
    }

    private static final String BASE_URL = "http://127.0.0.1:8080";

    private static final String PREFS_NAME =
            "campusconnect_session";

    private static final String TOKEN_KEY = "jwt_token";

    private ExecutorService executor;
    private NavigationListener navigationListener;

    private TextView welcomeText;
    private TextView sectionText;
    private TextView attendancePercentage;
    private TextView attendanceStatus;
    private TextView attendanceDetails;
    private TextView timetableContent;
    private TextView assignmentsContent;
    private TextView noticesContent;
    private TextView performanceContent;
    private TextView notificationsContent;

    public HomeDashboardFragment() {
        // Required empty constructor.
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof NavigationListener) {
            navigationListener = (NavigationListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_home_dashboard,
                container,
                false
        );
    }

@Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        welcomeText = view.findViewById(R.id.welcomeText);
        sectionText = view.findViewById(R.id.sectionText);

        attendancePercentage =
                view.findViewById(R.id.attendancePercentage);

        attendanceStatus =
                view.findViewById(R.id.attendanceStatus);

        attendanceDetails =
                view.findViewById(R.id.attendanceDetails);

        timetableContent =
                view.findViewById(R.id.timetableContent);

        assignmentsContent =
                view.findViewById(R.id.assignmentsContent);

        noticesContent =
                view.findViewById(R.id.noticesContent);

        performanceContent =
                view.findViewById(R.id.performanceContent);

        notificationsContent =
                view.findViewById(R.id.notificationsContent);

        // Top Navigation Header Buttons (Profile Left, Notifications Right)
        ImageButton btnProfile = view.findViewById(R.id.btnProfile);
        ImageButton btnNotifications = view.findViewById(R.id.btnNotifications);

        btnProfile.setOnClickListener(v -> openProfileScreen());
        btnNotifications.setOnClickListener(v -> openNotificationsScreen());

        View attendanceCard = view.findViewById(R.id.attendanceCard);
        View timetableCard = view.findViewById(R.id.timetableCard);
        View assignmentsCard = view.findViewById(R.id.assignmentsCard);
        View noticesCard = view.findViewById(R.id.noticesCard);
        View performanceCard = view.findViewById(R.id.performanceCard);
        View notificationsCard = view.findViewById(R.id.notificationsCard);
        View chatbotCard = view.findViewById(R.id.chatbotCard);

        // Updated Direct Notifications/Notices Navigation Handler
        View.OnClickListener directNotificationsClickListener = clickedView -> {
            if (isAdded()) {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new NotificationsFragment())
                        .addToBackStack(null)
                        .commit();
            }
        };

        // Dedicated individual card navigation handlers
        attendanceCard.setOnClickListener(v -> {
            if (isAdded()) {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new com.campusconnect.app.features.academics.AttendanceFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        timetableCard.setOnClickListener(v -> {
            if (isAdded()) {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new com.campusconnect.app.features.academics.TimetableFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        assignmentsCard.setOnClickListener(v -> {
            if (isAdded()) {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new com.campusconnect.app.features.academics.AssignmentsFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        performanceCard.setOnClickListener(v -> {
            if (isAdded()) {
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new com.campusconnect.app.features.academics.AcademicPerformanceFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        
        // Cards route directly to NotificationsFragment
        noticesCard.setOnClickListener(directNotificationsClickListener);
        notificationsCard.setOnClickListener(directNotificationsClickListener);
        
        chatbotCard.setOnClickListener(v -> openChatbot());

        executor = Executors.newFixedThreadPool(4);

        loadDashboard();

        // Apply monochrome styling if selected
        if (com.campusconnect.app.core.theme.ThemeManager.isMonochrome(requireContext())) {
            applyMonochromeToView(view, true);
        }
    }

    private void openProfileScreen() {
        if (!isAdded()) {
            return;
        }

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new StudentProfileFragment())
                .addToBackStack(null)
                .commit();
    }

    private void openNotificationsScreen() {
        if (!isAdded()) {
            return;
        }

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new NotificationsFragment())
                .addToBackStack(null)
                .commit();
    }

    private void openChatbot() {
        if (!isAdded()) {
            return;
        }

        getParentFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new com.campusconnect.app.features.chatbot.ChatbotFragment()
                )
                .addToBackStack(null)
                .commit();
    }

    private void loadDashboard() {
        SharedPreferences preferences =
                requireContext().getSharedPreferences(
                        PREFS_NAME,
                        android.content.Context.MODE_PRIVATE
                );

        String token = preferences.getString(TOKEN_KEY, null);

        if (TextUtils.isEmpty(token)) {
            showProfileError("Please sign in again.");
            return;
        }

        executor.execute(() -> {
            try {
                JSONObject profile = getObject(
                        "/api/students/me",
                        token
                );

                String name = profile.optString(
                        "name",
                        "Student"
                );

                String studentId = profile.optString(
                        "studentId",
                        ""
                );

                String sectionId = profile.optString(
                        "sectionId",
                        ""
                );

                String sectionName = profile.optString(
                        "sectionName",
                        ""
                );

                if (TextUtils.isEmpty(studentId)
                        || TextUtils.isEmpty(sectionId)) {

                    throw new IllegalStateException(
                            "Student profile is incomplete."
                    );
                }

                updateText(
                        welcomeText,
                        "Welcome back, " + name + "!"
                );

                updateText(
                        sectionText,
                        "Section " + sectionName
                );

                loadAttendance(token, studentId);
                loadTimetable(token, sectionId);

            } catch (Exception e) {
                showProfileError(
                        "Unable to load your profile. Pull down or reopen the dashboard to retry."
                );
            }
        });

        executor.execute(() -> loadAssignments(token));
        executor.execute(() -> loadNotices(token));
        executor.execute(() -> loadPerformance(token));
        executor.execute(() -> loadUnreadCount(token));
    }

    private void loadAttendance(
            String token,
            String studentId
    ) {
        try {
            JSONObject response = getObject(
                    "/api/attendance/student/"
                            + studentId
                            + "/summary",
                    token
            );

            long totalClasses =
                    response.optLong("totalClasses", 0);

            long presentClasses =
                    response.optLong("presentClasses", 0);

            long absentClasses =
                    response.optLong("absentClasses", 0);

            double percentage =
                    response.optDouble(
                            "attendancePercentage",
                            0
                    );

            if (totalClasses == 0) {
                updateText(
                        attendancePercentage,
                        "--"
                );

                updateText(
                        attendanceStatus,
                        "No attendance records yet"
                );

                // Safely update visibility on the UI thread
                if (attendanceDetails != null) {
                    requireActivity().runOnUiThread(() -> attendanceDetails.setVisibility(android.view.View.GONE));
                }

                return;
            }

            String formattedPercentage = String.format(
                    Locale.getDefault(),
                    "%.1f%%",
                    percentage
            );

            updateText(
                    attendancePercentage,
                    formattedPercentage
            );

            String status;

            if (percentage >= 80) {
                status = "SAFE";
            } else if (percentage >= 75) {
                status = "MODERATE RISK";
            } else {
                status = "HIGH RISK";
            }

            updateText(
                    attendanceStatus,
                    status
            );

            // Safely update visibility on the UI thread
            if (attendanceDetails != null) {
                requireActivity().runOnUiThread(() -> attendanceDetails.setVisibility(android.view.View.GONE));
            }

        } catch (Exception e) {
            android.util.Log.e("HomeDashboard", "Failed to load attendance", e);
            updateText(
                    attendancePercentage,
                    "--"
            );

            updateText(
                    attendanceStatus,
                    "Unavailable"
            );

            if (attendanceDetails != null) {
                requireActivity().runOnUiThread(() -> attendanceDetails.setVisibility(android.view.View.GONE));
            }
        }
    }

    private void loadTimetable(
            String token,
            String sectionId
    ) {
        try {
            JSONArray response = getArray(
                    "/api/timetable/today/" + sectionId,
                    token
            );

            if (response.length() == 0) {
                updateText(
                        timetableContent,
                        "No classes scheduled for today."
                );
                return;
            }

            List<JSONObject> entries = new ArrayList<>();

            for (int i = 0; i < response.length(); i++) {
                JSONObject item = response.optJSONObject(i);

                if (item != null) {
                    entries.add(item);
                }
            }

            Collections.sort(
                    entries,
                    Comparator.comparing(
                            item -> item.optString(
                                    "startTime",
                                    "23:59"
                            )
                    )
            );

            // Grab the next upcoming class (first item in sorted list)
            JSONObject nextClass = entries.get(0);
            
            String start = nextClass.optString("startTime", "");
            String end = nextClass.optString("endTime", "");
            
            // Adjust "subject" or "className" based on your JSON response key
            String className = nextClass.optString("subjectName", 
                               nextClass.optString("className", "Scheduled Class"));
            String room = nextClass.optString("room", "");

            StringBuilder summary = new StringBuilder();
            summary.append(className).append("\n");
            
            if (!TextUtils.isEmpty(start) && !TextUtils.isEmpty(end)) {
                summary.append(start).append(" - ").append(end);
            } else {
                summary.append("Today");
            }

            if (!TextUtils.isEmpty(room) && !"null".equalsIgnoreCase(room)) {
                summary.append(" • Room ").append(room);
            }

            updateText(
                    timetableContent,
                    summary.toString()
            );

        } catch (Exception e) {
            Log.e("HomeDashboard", "Failed to load timetable", e);
            updateText(
                    timetableContent,
                    "Today's timetable is currently unavailable."
            );
        }
    }

    private void loadAssignments(String token) {
        try {
            JSONArray response = getArray(
                    "/api/assignments",
                    token
            );

            List<JSONObject> upcoming = new ArrayList<>();

            String now = java.time.LocalDateTime.now().toString();

            for (int i = 0; i < response.length(); i++) {
                JSONObject item = response.optJSONObject(i);

                if (item == null) {
                    continue;
                }

                String dueDateTime = item.optString(
                        "dueDateTime",
                        ""
                );

                if (!TextUtils.isEmpty(dueDateTime)
                        && dueDateTime.compareTo(now) >= 0) {

                    upcoming.add(item);
                }
            }

            Collections.sort(
                    upcoming,
                    Comparator.comparing(
                            item -> item.optString(
                                    "dueDateTime",
                                    "9999"
                            )
                    )
            );

            if (upcoming.isEmpty()) {
                updateText(
                        assignmentsContent,
                        "No upcoming assignments."
                );

                return;
            }

            // Grab only the single nearest upcoming assignment for a minimalist card look
            JSONObject nextAssignment = upcoming.get(0);

            String title = nextAssignment.optString(
                    "title",
                    "Assignment"
            );

            String subject = nextAssignment.optString(
                    "subjectName",
                    ""
            );

            String due = nextAssignment.optString(
                    "dueDateTime",
                    ""
            );

            StringBuilder summary = new StringBuilder();
            summary.append(title);

            if (!TextUtils.isEmpty(subject)
                    && !"null".equalsIgnoreCase(subject)) {

                summary.append("\n").append(subject);
            }

            if (!TextUtils.isEmpty(due)) {
                // Clean up the timestamp to show just date/time nicely
                String cleanDue = due.replace('T', ' ');
                if (cleanDue.length() > 16) {
                    cleanDue = cleanDue.substring(0, 16); // Trim seconds if present
                }
                summary.append("\nDue: ").append(cleanDue);
            }

            updateText(
                    assignmentsContent,
                    summary.toString()
            );

        } catch (Exception e) {
            Log.e("HomeDashboard", "Failed to load assignments", e);
            updateText(
                    assignmentsContent,
                    "Assignments are currently unavailable."
            );
        }
    }

    private void loadNotices(String token) {
        try {
            JSONArray response = getArray(
                    "/api/notices",
                    token
            );

            if (response.length() == 0) {
                updateText(
                        noticesContent,
                        "No notices available."
                );

                return;
            }

            List<JSONObject> notices = new ArrayList<>();

            for (int i = 0; i < response.length(); i++) {
                JSONObject item = response.optJSONObject(i);

                if (item != null) {
                    notices.add(item);
                }
            }

            Collections.sort(
                    notices,
                    (first, second) ->
                            second.optString("date", "")
                                    .compareTo(
                                            first.optString("date", "")
                                    )
            );

            // Grab only the single latest notice for a clean, minimalist feed card
            JSONObject latestNotice = notices.get(0);

            String title = latestNotice.optString("title", "Campus notice");
            String date = latestNotice.optString("date", "");

            StringBuilder summary = new StringBuilder();
            summary.append(title);

            if (!TextUtils.isEmpty(date)) {
                summary.append("\n").append(date);
            }

            updateText(
                    noticesContent,
                    summary.toString()
            );

        } catch (Exception e) {
                Log.e("HomeDashboard", "Failed to load notices", e);
            updateText(
                    noticesContent,
                    "Notices are currently unavailable."
            );
        }
    }

    private void loadPerformance(String token) {
        try {
            JSONObject response = getObject(
                    "/api/performance/my",
                    token
            );

            double obtained = response.optDouble(
                    "totalMarksObtained",
                    0
            );

            double maximum = response.optDouble(
                    "totalMaximumMarks",
                    0
            );

            String level = response.optString(
                    "performanceLevel",
                    "Good"
            );

            JSONArray subjects = response.optJSONArray(
                    "subjects"
            );

            if (maximum <= 0
                    || (subjects != null && subjects.length() == 0)) {

                updateText(
                        performanceContent,
                        "No academic performance records available yet."
                );

                return;
            }

            double percentage = response.optDouble(
                    "overallPercentage",
                    obtained * 100.0 / maximum
            );

            // Clean & Minimalist: Display only high-level summary metrics
            String overallSummary = String.format(
                    Locale.getDefault(),
                    "%.1f%% • %s\nMarks: %.0f / %.0f",
                    percentage,
                    level,
                    obtained,
                    maximum
            );

            updateText(
                    performanceContent,
                    overallSummary
            );

        } catch (Exception e) {
            Log.e("HomeDashboard", "Failed to load performance", e);
            updateText(
                    performanceContent,
                    "Academic performance is currently unavailable."
            );
        }
    }

    private void loadUnreadCount(String token) {
        try {
            String response = getString(
                    "/api/notifications/unread-count",
                    token
            );

            long unread = Long.parseLong(response.trim());

            String message;

            if (unread == 0) {
                message = "You're all caught up.";
            } else if (unread == 1) {
                message = "You have 1 unread notification.";
            } else {
                message = "You have "
                        + unread
                        + " unread notifications.";
            }

            updateText(
                    notificationsContent,
                    message
            );

        } catch (Exception e) {
            Log.e("HomeDashboard", "Failed to load notification count", e);
            updateText(
                    notificationsContent,
                    "Notification count is currently unavailable."
            );
        }
    }

    private JSONObject getObject(
            String path,
            String token
    ) throws Exception {

        return new JSONObject(getString(path, token));
    }

    private JSONArray getArray(
            String path,
            String token
    ) throws Exception {

        return new JSONArray(getString(path, token));
    }

    private String getString(
            String path,
            String token
    ) throws Exception {

        HttpURLConnection connection = null;

        try {
            URL url = new URL(BASE_URL + path);

            connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            connection.setRequestProperty(
                    "Accept",
                    "application/json"
            );

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + token
            );

            int status = connection.getResponseCode();

            InputStream stream;

            if (status >= 200 && status < 300) {
                stream = connection.getInputStream();
            } else {
                stream = connection.getErrorStream();
            }

            StringBuilder result = new StringBuilder();

            if (stream != null) {
                try (BufferedReader reader =
                             new BufferedReader(
                                     new InputStreamReader(
                                             stream,
                                             StandardCharsets.UTF_8
                                     )
                             )) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                }
            }

            if (status < 200 || status >= 300) {
                throw new IllegalStateException(
                        "Request failed with HTTP " + status
                );
            }

            return result.toString();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void updateText(
            TextView target,
            String value
    ) {
        if (!isAdded()) {
            return;
        }

        requireActivity().runOnUiThread(() -> {
            if (isAdded()) {
                target.setText(value);
            }
        });
    }

    private void showProfileError(String message) {
        updateText(welcomeText, "Welcome to CampusConnect");
        updateText(sectionText, message);
    }

    @Override
    public void onDestroyView() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        navigationListener = null;

        super.onDestroyView();
    }

    private void applyMonochromeToView(android.view.View view, boolean isMonochrome) {
        if (!isMonochrome) return;

        if (view instanceof com.google.android.material.card.MaterialCardView) {
            com.google.android.material.card.MaterialCardView card = 
                (com.google.android.material.card.MaterialCardView) view;
            card.setCardBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));
            card.setStrokeColor(android.graphics.Color.parseColor("#000000"));
            card.setStrokeWidth(2);
        } else if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyMonochromeToView(group.getChildAt(i), true);
            }
        }
    }
}