package com.campusconnect.app.features.academics;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.campusconnect.app.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AcademicsFragment extends Fragment {

    private static final String BASE_URL = "http://127.0.0.1:8080";

    private static final String PREFS_NAME = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";

    private ExecutorService executor;

    private LinearLayout contentContainer;
    private TextView loadingText;

    public AcademicsFragment() {
        // Required empty constructor.
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull android.view.LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        Context context = requireContext();

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(
                context.getColor(R.color.main_background)
        );

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(28));

        scrollView.addView(root);

        TextView eyebrow = new TextView(context);
        eyebrow.setText("CAMPUSCONNECT");
        eyebrow.setTextColor(
                context.getColor(R.color.primary_accent)
        );
        eyebrow.setTextSize(12);
        eyebrow.setLetterSpacing(0.12f);
        eyebrow.setTypeface(null, Typeface.BOLD);

        root.addView(eyebrow);

        TextView title = new TextView(context);
        title.setText("Academics");
        title.setTextColor(
                context.getColor(R.color.primary_text)
        );
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        titleParams.topMargin = dp(8);
        root.addView(title, titleParams);

        TextView subtitle = new TextView(context);
        subtitle.setText(
                "Your attendance, classes and academic progress."
        );
        subtitle.setTextColor(
                context.getColor(R.color.secondary_text)
        );
        subtitle.setTextSize(14);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        subtitleParams.topMargin = dp(4);
        root.addView(subtitle, subtitleParams);

        loadingText = new TextView(context);
        loadingText.setText("Loading academic information...");
        loadingText.setTextColor(
                context.getColor(R.color.secondary_text)
        );
        loadingText.setTextSize(14);
        loadingText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams loadingParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(90)
                );

        loadingParams.topMargin = dp(16);
        root.addView(loadingText, loadingParams);

        contentContainer = new LinearLayout(context);
        contentContainer.setOrientation(LinearLayout.VERTICAL);

        root.addView(
                contentContainer,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        return scrollView;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        executor = Executors.newSingleThreadExecutor();
        loadAcademics();
    }

    private void loadAcademics() {
        SharedPreferences preferences =
                requireContext().getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        String token = preferences.getString(TOKEN_KEY, null);

        if (TextUtils.isEmpty(token)) {
            showError("Your session has expired. Please sign in again.");
            return;
        }

        executor.execute(() -> {
            try {
                JSONObject profile = getObject(
                        "/api/students/me",
                        token
                );

                String studentId = profile.optString(
                        "studentId",
                        ""
                );

                String sectionId = profile.optString(
                        "sectionId",
                        ""
                );

                if (TextUtils.isEmpty(studentId)
                        || TextUtils.isEmpty(sectionId)) {
                    throw new IllegalStateException(
                            "Student profile is incomplete."
                    );
                }

                List<JSONObject> attendance = new ArrayList<>();
                List<JSONObject> timetable = new ArrayList<>();
                List<JSONObject> assignments = new ArrayList<>();
                List<JSONObject> exams = new ArrayList<>();
                List<JSONObject> results = new ArrayList<>();

                JSONObject performance = null;

                String attendanceError = null;
                String timetableError = null;
                String assignmentsError = null;
                String examsError = null;
                String resultsError = null;
                String performanceError = null;

                try {
                    attendance = toList(
                            getArray(
                                    "/api/attendance/student/"
                                            + studentId
                                            + "/all-subjects",
                                    token
                            )
                    );
                } catch (Exception e) {
                    attendanceError = "Attendance information is unavailable.";
                }

                try {
                    timetable = toList(
                            getArray(
                                    "/api/timetable/weekly/"
                                            + sectionId,
                                    token
                            )
                    );
                } catch (Exception e) {
                    timetableError = "Weekly timetable is unavailable.";
                }

                try {
                    assignments = toList(
                            getArray("/api/assignments", token)
                    );
                } catch (Exception e) {
                    assignmentsError = "Assignments are unavailable.";
                }

                try {
                    exams = toList(
                            getArray("/api/exams/my", token)
                    );
                } catch (Exception e) {
                    examsError = "Exam information is unavailable.";
                }

                try {
                    results = toList(
                            getArray("/api/results/my", token)
                    );
                } catch (Exception e) {
                    resultsError = "Results are unavailable.";
                }

                try {
                    performance = getObject(
                            "/api/performance/my",
                            token
                    );
                } catch (Exception e) {
                    performanceError = "Performance information is unavailable.";
                }

                List<JSONObject> finalAttendance = attendance;
                List<JSONObject> finalTimetable = timetable;
                List<JSONObject> finalAssignments = assignments;
                List<JSONObject> finalExams = exams;
                List<JSONObject> finalResults = results;

                JSONObject finalPerformance = performance;

                String finalAttendanceError = attendanceError;
                String finalTimetableError = timetableError;
                String finalAssignmentsError = assignmentsError;
                String finalExamsError = examsError;
                String finalResultsError = resultsError;
                String finalPerformanceError = performanceError;

                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) {
                        return;
                    }

                    renderAttendance(
                            finalAttendance,
                            finalAttendanceError
                    );

                    renderTimetable(
                            finalTimetable,
                            finalTimetableError
                    );

                    renderAssignments(
                            finalAssignments,
                            finalAssignmentsError
                    );

                    renderExams(
                            finalExams,
                            finalExamsError
                    );

                    renderResults(
                            finalResults,
                            finalResultsError
                    );

                    renderPerformance(
                            finalPerformance,
                            finalPerformanceError
                    );

                    renderCourseMaterialsEntry();

                    loadingText.setVisibility(View.GONE);
                });

            } catch (Exception e) {
                showError(
                        "Unable to load your academic profile. "
                                + "Please return to Home and try again."
                );
            }
        });
    }

    private void renderAttendance(
                List<JSONObject> records,
                String error
        ) {
        LinearLayout card = createCard("Attendance");

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.topMargin = dp(16);

        if (error != null) {
                addBodyText(card, error);
                card.addView(createViewAttendanceButton(), buttonParams);
                return; 
        }

        if (records.isEmpty()) {
                addBodyText(
                        card,
                        "No subject attendance records are available yet."
                );
                card.addView(createViewAttendanceButton(), buttonParams);
                return; 
        }

        for (JSONObject item : records) {
                String subject = item.optString("subjectName", "Subject");
                String code = item.optString("subjectCode", "");
                double percentage = item.optDouble("attendancePercentage", 0);
                String risk = item.optString("riskLevel", "UNKNOWN");

                long present = item.optLong("presentClasses", 0);
                long absent = item.optLong("absentClasses", 0);
                long total = item.optLong("totalClasses", 0);
                long needed = item.optLong("classesNeededFor75", 0);

                StringBuilder text = new StringBuilder();
                text.append(subject);

                if (!TextUtils.isEmpty(code) && !"null".equalsIgnoreCase(code)) {
                text.append(" (").append(code).append(")");
                }

                text.append("\n")
                        .append(String.format(
                                Locale.getDefault(),
                                "%.1f%% attendance",
                                percentage
                        ));

                text.append("\n")
                        .append("Present: ").append(present)
                        .append("  |  Absent: ").append(absent)
                        .append("  |  Total: ").append(total);

                text.append("\nRisk: ").append(risk);

                if (needed > 0) {
                text.append("\nClasses needed to reach 75%: ").append(needed);
                }

                addBodyText(card, text.toString());
        }

        card.addView(createViewAttendanceButton(), buttonParams);
        
        }

    private Button createViewAttendanceButton() {
        Button button = new Button(requireContext());
        button.setText("View full attendance");
        button.setOnClickListener(v -> openAttendanceScreen());
        return button;
    }

    private void openAttendanceScreen() {
        if (!isAdded()) {
            return;
        }
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new AttendanceFragment())
                .addToBackStack(null)
                .commit();
    }

   
        private void renderTimetable(
                List<JSONObject> records,
                String error
        ) {
        LinearLayout card = createCard("Timetable");

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        buttonParams.topMargin = dp(16);

        if (error != null) {
                addBodyText(card, error);
                card.addView(createViewTimetableButton(), buttonParams);
                return;
        }

        if (records.isEmpty()) {
                addBodyText(
                        card,
                        "No timetable entries are available."
                );
                card.addView(createViewTimetableButton(), buttonParams);
                return;
        }

        records.sort(
                Comparator
                        .comparingInt((JSONObject item) ->
                                dayOrder(item.optString("day", ""))
                        )
                        .thenComparing(
                                (JSONObject item) ->
                                        item.optString(
                                                "startTime",
                                                "23:59:59"
                                        )
                        )
        );

        String currentDay = "";

        for (JSONObject item : records) {
                String day = item.optString("day", "Day");

                if (!day.equalsIgnoreCase(currentDay)) {
                currentDay = day;

                TextView dayHeading = new TextView(requireContext());
                dayHeading.setText(day);
                dayHeading.setTextColor(
                        requireContext().getColor(
                                R.color.primary_accent
                        )
                );
                dayHeading.setTextSize(15);
                dayHeading.setTypeface(null, Typeface.BOLD);

                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );

                params.topMargin = dp(10);
                card.addView(dayHeading, params);
                }

                String start = item.optString("startTime", "");
                String end = item.optString("endTime", "");
                String room = item.optString("room", "");
                String subjectId = item.optString("subjectId", "");

                StringBuilder entry = new StringBuilder();

                entry.append(start)
                        .append(" - ")
                        .append(end);

                if (!TextUtils.isEmpty(room)
                        && !"null".equalsIgnoreCase(room)) {
                entry.append("\nRoom: ").append(room);
                }

                if (!TextUtils.isEmpty(subjectId)
                        && !"null".equalsIgnoreCase(subjectId)) {
                entry.append("\nSubject ID: ").append(subjectId);
                }

                addBodyText(card, entry.toString());
        }

        card.addView(createViewTimetableButton(), buttonParams);
        }

        private Button createViewTimetableButton() {
        Button button = new Button(requireContext());

        button.setText("View full timetable");
        button.setAllCaps(false);

        button.setOnClickListener(v -> openTimetableScreen());

        return button;
        }

        private void openTimetableScreen() {
        if (!isAdded()) {
                return;
        }

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new TimetableFragment()
                )
                .addToBackStack(null)
                .commit();
        }

    private void renderAssignments(
                List<JSONObject> records,
                String error
        ) {
        LinearLayout card = createCard("Assignments");

        if (error != null) {
                addBodyText(card, error);
                card.addView(createViewAssignmentsButton());
                return;
        }

        if (records.isEmpty()) {
                addBodyText(card, "No assignments are available.");
                card.addView(createViewAssignmentsButton());
                return;
        }

        records.sort(
                Comparator.comparing(
                        item -> item.optString(
                                "dueDateTime",
                                "9999"
                        )
                )
        );

        int previewCount = Math.min(records.size(), 3);

        for (int i = 0; i < previewCount; i++) {
                JSONObject item = records.get(i);

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

                String marks = item.optString(
                        "maximumMarks",
                        ""
                );

                StringBuilder text = new StringBuilder(title);

                if (!TextUtils.isEmpty(subject)
                        && !"null".equalsIgnoreCase(subject)) {
                text.append("\nSubject: ").append(subject);
                }

                text.append("\nDue: ").append(due.replace('T', ' '));

                if (!TextUtils.isEmpty(marks)
                        && !"null".equalsIgnoreCase(marks)) {
                text.append("\nMaximum marks: ").append(marks);
                }

                addBodyText(card, text.toString());
        }

        if (records.size() > previewCount) {
                addBodyText(
                        card,
                        (records.size() - previewCount)
                                + " more assignment(s)"
                );
        }

        card.addView(createViewAssignmentsButton());
        }
        
    private Button createViewAssignmentsButton() {
        Button button = new Button(requireContext());

        button.setText("View All Assignments");
        button.setAllCaps(false);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(16);

        button.setOnClickListener(v -> openAssignmentsScreen());

        return button;
        }

        private void openAssignmentsScreen() {
        if (!isAdded()) {
                return;
        }

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new AssignmentsFragment()
                )
                .addToBackStack(null)
                .commit();
        }

    private void renderExams(
            List<JSONObject> records,
            String error
    ) {
        LinearLayout card = createCard("Examinations");

        if (error != null) {
            addBodyText(card, error);
            return;
        }

        if (records.isEmpty()) {
            addBodyText(card, "No examinations are available.");
            return;
        }

        records.sort(
                Comparator.comparing(
                        item -> item.optString("examDate", "9999")
                )
        );

        for (JSONObject item : records) {
            String title = item.optString("title", "Examination");
            String subject = item.optString("subjectName", "");
            String date = item.optString("examDate", "");
            String start = item.optString("startTime", "");
            String end = item.optString("endTime", "");
            String maximum = item.optString("maximumMarks", "");

            StringBuilder text = new StringBuilder(title);

            if (!TextUtils.isEmpty(subject)
                    && !"null".equalsIgnoreCase(subject)) {
                text.append("\nSubject: ").append(subject);
            }

            if (!TextUtils.isEmpty(date)) {
                text.append("\nDate: ").append(date);
            }

            if (!TextUtils.isEmpty(start)
                    || !TextUtils.isEmpty(end)) {
                text.append("\nTime: ")
                        .append(start)
                        .append(" - ")
                        .append(end);
            }

            if (!TextUtils.isEmpty(maximum)) {
                text.append("\nMaximum marks: ").append(maximum);
            }

            addBodyText(card, text.toString());
        }
    }

    private void renderResults(
            List<JSONObject> records,
            String error
    ) {
        LinearLayout card = createCard("Examination results");

        if (error != null) {
            addBodyText(card, error);
            return;
        }

        if (records.isEmpty()) {
            addBodyText(
                    card,
                    "No published examination results are available."
            );
            return;
        }

        for (JSONObject item : records) {
            String title = item.optString(
                    "examTitle",
                    "Examination"
            );

            String subject = item.optString(
                    "subjectName",
                    ""
            );

            double obtained = item.optDouble(
                    "marksObtained",
                    0
            );

            double maximum = item.optDouble(
                    "maximumMarks",
                    0
            );

            double percentage = item.optDouble(
                    "percentage",
                    -1
            );

            if (percentage < 0 && maximum > 0) {
                percentage = obtained * 100.0 / maximum;
            }

            StringBuilder text = new StringBuilder(title);

            if (!TextUtils.isEmpty(subject)
                    && !"null".equalsIgnoreCase(subject)) {
                text.append("\nSubject: ").append(subject);
            }

            text.append("\nMarks: ")
                    .append(formatNumber(obtained))
                    .append(" / ")
                    .append(formatNumber(maximum));

            if (percentage >= 0) {
                text.append("\nPercentage: ")
                        .append(String.format(
                                Locale.getDefault(),
                                "%.1f%%",
                                percentage
                        ));
            }

            addBodyText(card, text.toString());
        }
    }

    private void renderPerformance(
            JSONObject response,
            String error
    ) {
        LinearLayout card = createCard("Academic performance");

        if (error != null || response == null) {
            addBodyText(
                    card,
                    error != null
                            ? error
                            : "Performance information is unavailable."
            );
            return;
        }

        double obtained = response.optDouble(
                "totalMarksObtained",
                0
        );

        double maximum = response.optDouble(
                "totalMaximumMarks",
                0
        );

        double percentage = response.optDouble(
                "overallPercentage",
                maximum > 0 ? obtained * 100.0 / maximum : 0
        );

        String level = response.optString(
                "performanceLevel",
                "Not available"
        );

        if (maximum <= 0) {
            addBodyText(
                    card,
                    "No academic performance records are available yet."
            );
            return;
        }

        addBodyText(
                card,
                String.format(
                        Locale.getDefault(),
                        "Overall percentage: %.1f%%\n"
                                + "Marks obtained: %s / %s\n"
                                + "Performance level: %s",
                        percentage,
                        formatNumber(obtained),
                        formatNumber(maximum),
                        level
                )
        );

        JSONArray subjects = response.optJSONArray("subjects");

        if (subjects != null && subjects.length() > 0) {
            addBodyText(card, "\nSubject performance");

            for (int i = 0; i < subjects.length(); i++) {
                JSONObject item = subjects.optJSONObject(i);

                if (item == null) {
                    continue;
                }

                String subjectName = item.optString(
                        "subjectName",
                        "Unknown subject"
                );

                String subjectCode = item.optString(
                        "subjectCode",
                        ""
                );

                double subjectObtained = item.optDouble(
                        "marksObtained",
                        0
                );

                double subjectMaximum = item.optDouble(
                        "maximumMarks",
                        0
                );

                double subjectPercentage = item.optDouble(
                        "percentage",
                        subjectMaximum > 0
                                ? subjectObtained * 100.0 / subjectMaximum
                                : 0
                );

                String subjectLevel = item.optString(
                        "performanceLevel",
                        "Not available"
                );

                String subjectDetails = String.format(
                        Locale.getDefault(),
                        "%s%s\nMarks: %s / %s\nPercentage: %.1f%%\nPerformance level: %s",
                        subjectName,
                        subjectCode.isEmpty() ? "" : " (" + subjectCode + ")",
                        formatNumber(subjectObtained),
                        formatNumber(subjectMaximum),
                        subjectPercentage,
                        subjectLevel
                );

                addBodyText(card, subjectDetails);
            }
        }
    }

    private void renderCourseMaterialsEntry() {
        LinearLayout card = createCard("Course Materials");

        addBodyText(
                card,
                "Access study resources, subject notes, "
                        + "and other learning materials shared by your teachers."
        );

        Button button = new Button(requireContext());
        button.setText("View Course Materials");
        button.setAllCaps(false);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(16);

        button.setOnClickListener(v -> openCourseMaterialsScreen());

        card.addView(button, params);
        }

        private void openCourseMaterialsScreen() {
        if (!isAdded()) {
                return;
        }

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new CourseMaterialsFragment()
                )
                .addToBackStack(null)
                .commit();
        }

    private LinearLayout createCard(String heading) {
        Context context = requireContext();

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.topMargin = dp(18);

        contentContainer.addView(card, cardParams);

        TextView title = new TextView(context);
        title.setText(heading);
        title.setTextColor(
                context.getColor(R.color.primary_text)
        );
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);

        card.addView(title);

        return card;
    }

    private void addBodyText(
            LinearLayout card,
            String value
    ) {
        TextView text = new TextView(requireContext());

        text.setText(value);
        text.setTextColor(
                requireContext().getColor(R.color.secondary_text)
        );
        text.setTextSize(14);
        text.setLineSpacing(dp(3), 1.0f);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(10);

        card.addView(text, params);
    }

    private List<JSONObject> toList(JSONArray array) {
        List<JSONObject> records = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);

            if (item != null) {
                records.add(item);
            }
        }

        return records;
    }

    private int dayOrder(String day) {
        switch (day.toUpperCase(Locale.ROOT)) {
            case "MONDAY":
                return 1;
            case "TUESDAY":
                return 2;
            case "WEDNESDAY":
                return 3;
            case "THURSDAY":
                return 4;
            case "FRIDAY":
                return 5;
            case "SATURDAY":
                return 6;
            case "SUNDAY":
                return 7;
            default:
                return 8;
        }
    }

    private String formatNumber(double value) {
        if (value == Math.floor(value)) {
            return String.format(
                    Locale.getDefault(),
                    "%.0f",
                    value
            );
        }

        return String.format(
                Locale.getDefault(),
                "%.1f",
                value
        );
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

            connection = (HttpURLConnection) url.openConnection();

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

    private void showError(String message) {
        if (!isAdded()) {
            return;
        }

        requireActivity().runOnUiThread(() -> {
            if (!isAdded()) {
                return;
            }

            loadingText.setVisibility(View.GONE);
            LinearLayout card = createCard("Notice");
            addBodyText(card, message);
        });
    }

    private int dp(int value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }
}