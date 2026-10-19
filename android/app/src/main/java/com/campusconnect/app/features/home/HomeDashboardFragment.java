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

        View.OnClickListener academicsClickListener = clickedView -> {
            if (navigationListener != null) {
                navigationListener.onHomeCardSelected(R.id.nav_academics);
            }
        };

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

        attendanceCard.setOnClickListener(academicsClickListener);
        timetableCard.setOnClickListener(academicsClickListener);
        assignmentsCard.setOnClickListener(academicsClickListener);
        performanceCard.setOnClickListener(academicsClickListener);
        
        // Cards now correctly route directly to NotificationsFragment
        noticesCard.setOnClickListener(directNotificationsClickListener);
        notificationsCard.setOnClickListener(directNotificationsClickListener);
        
        chatbotCard.setOnClickListener(v -> openChatbot());

        executor = Executors.newFixedThreadPool(4);

        loadDashboard();
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

                updateText(
                        attendanceDetails,
                        "Your attendance summary will appear when classes are recorded."
                );

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

            updateText(
                    attendanceDetails,
                    "Present: " + presentClasses
                            + "  •  Absent: " + absentClasses
                            + "  •  Total: " + totalClasses
            );

        } catch (Exception e) {
            updateText(
                    attendancePercentage,
                    "--"
            );

            updateText(
                    attendanceStatus,
                    "Attendance unavailable"
            );

            updateText(
                    attendanceDetails,
                    "Could not load attendance information."
            );
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

            StringBuilder result = new StringBuilder();

            for (JSONObject item : entries) {
                String start = item.optString(
                        "startTime",
                        ""
                );

                String end = item.optString(
                        "endTime",
                        ""
                );

                String room = item.optString(
                        "room",
                        ""
                );

                result.append(start)
                        .append(" - ")
                        .append(end)
                        .append("\n");

                result.append("Scheduled class");

                if (!TextUtils.isEmpty(room)
                        && !"null".equalsIgnoreCase(room)) {

                    result.append(" • Room ")
                            .append(room);
                }

                result.append("\n\n");
            }

            updateText(
                    timetableContent,
                    result.toString().trim()
            );

        } catch (Exception e) {
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

            StringBuilder result = new StringBuilder();

            int limit = Math.min(upcoming.size(), 3);

            for (int i = 0; i < limit; i++) {
                JSONObject item = upcoming.get(i);

                String title = item.optString(
                        "title",
                        "Assignment"
                );

                String subject = item.optString(
                        "subjectName",
                        ""
                );

                String due = item.optString(
                        "dueDateTime",
                        "Deadline unavailable"
                );

                result.append(title);

                if (!TextUtils.isEmpty(subject)
                        && !"null".equalsIgnoreCase(subject)) {

                    result.append("\n")
                            .append(subject);
                }

                result.append("\nDue: ")
                        .append(due.replace('T', ' '))
                        .append("\n\n");
            }

            updateText(
                    assignmentsContent,
                    result.toString().trim()
            );

        } catch (Exception e) {
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

            StringBuilder result = new StringBuilder();

            int limit = Math.min(notices.size(), 3);

            for (int i = 0; i < limit; i++) {
                JSONObject item = notices.get(i);

                result.append(
                        item.optString("title", "Campus notice")
                );

                String date = item.optString("date", "");

                if (!TextUtils.isEmpty(date)) {
                    result.append("\n")
                            .append(date);
                }

                result.append("\n\n");
            }

            updateText(
                    noticesContent,
                    result.toString().trim()
            );

        } catch (Exception e) {
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
                    ""
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

            String overallSummary = String.format(
                    Locale.getDefault(),
                    "Overall percentage: %.1f%%\nMarks obtained: %.0f / %.0f\nPerformance level: %s",
                    percentage,
                    obtained,
                    maximum,
                    level
            );

            StringBuilder result = new StringBuilder(overallSummary);

            if (subjects != null && subjects.length() > 0) {
                result.append("\n\nSubject performance:");
                for (int i = 0; i < subjects.length(); i++) {
                    JSONObject sub = subjects.optJSONObject(i);
                    if (sub != null) {
                        String subName = sub.optString("subjectName", "Subject");
                        String subCode = sub.optString("subjectCode", "");
                        double subObt = sub.optDouble("totalMarksObtained", 0);
                        double subMax = sub.optDouble("totalMaximumMarks", 0);
                        double subPct = sub.optDouble("percentage", 0);
                        String subLevel = sub.optString("performanceLevel", "");

                        String subjectLine = String.format(
                                Locale.getDefault(),
                                "\n%s (%s)\nMarks: %.0f / %.0f (%.1f%%) • %s",
                                subName,
                                subCode,
                                subObt,
                                subMax,
                                subPct,
                                subLevel
                        );
                        result.append(subjectLine);
                    }
                }
            }

            updateText(
                    performanceContent,
                    result.toString().trim()
            );

        } catch (Exception e) {
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
}