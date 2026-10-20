package com.campusconnect.app.features.academics;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TimetableFragment extends Fragment {

    private static final String BASE_URL = "http://127.0.0.1:8080";
    private static final String PREFS_NAME = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";

    private static final List<String> DAYS = Arrays.asList(
            "MONDAY",
            "TUESDAY",
            "WEDNESDAY",
            "THURSDAY",
            "FRIDAY",
            "SATURDAY",
            "SUNDAY"
    );

    private ExecutorService executor;
    private LinearLayout root;
    private LinearLayout daySelector;
    private LinearLayout classesContainer;
    private TextView statusText;
    private ProgressBar progressBar;
    private List<JSONObject> timetable = new ArrayList<>();
    private String selectedDay = "MONDAY";

    public TimetableFragment() {
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
        boolean isDarkMode = (context.getResources().getConfiguration().uiMode 
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        if (savedInstanceState != null) {
            selectedDay = savedInstanceState.getString("selected_day", getCurrentDay());
        } else {
            selectedDay = getCurrentDay();
        }

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(context.getColor(R.color.main_background));

        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(28));
        scrollView.addView(root);

        TextView eyebrow = new TextView(context);
        eyebrow.setText("CAMPUSCONNECT");
        eyebrow.setTextColor(context.getColor(R.color.primary_accent));
        eyebrow.setTextSize(12);
        eyebrow.setLetterSpacing(0.12f);
        eyebrow.setTypeface(null, Typeface.BOLD);
        root.addView(eyebrow);

        TextView title = new TextView(context);
        title.setText("My Timetable");
        title.setTextColor(isDarkMode ? android.graphics.Color.parseColor("#FFFFFF") : context.getColor(R.color.primary_text));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = dp(8);
        root.addView(title, titleParams);

        TextView subtitle = new TextView(context);
        subtitle.setText("Your weekly class schedule.");
        subtitle.setTextColor(isDarkMode ? android.graphics.Color.parseColor("#CCCCCC") : context.getColor(R.color.secondary_text));
        subtitle.setTextSize(14);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.topMargin = dp(4);
        subtitleParams.bottomMargin = dp(16);
        root.addView(subtitle, subtitleParams);

        TextView dayTitle = new TextView(context);
        dayTitle.setText("Select a day");
        dayTitle.setTextColor(isDarkMode ? android.graphics.Color.parseColor("#FFFFFF") : context.getColor(R.color.primary_text));
        dayTitle.setTextSize(16);
        dayTitle.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams dayTitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dayTitleParams.topMargin = dp(8);
        root.addView(dayTitle, dayTitleParams);

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(context);
        horizontalScrollView.setHorizontalScrollBarEnabled(false);
        daySelector = new LinearLayout(context);
        daySelector.setOrientation(LinearLayout.HORIZONTAL);
        horizontalScrollView.addView(daySelector);

        LinearLayout.LayoutParams selectorParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        selectorParams.topMargin = dp(12);
        root.addView(horizontalScrollView, selectorParams);

        progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        progressParams.gravity = Gravity.CENTER_HORIZONTAL;
        progressParams.topMargin = dp(40);
        root.addView(progressBar, progressParams);

        statusText = new TextView(context);
        statusText.setText("Loading your timetable...");
        statusText.setTextColor(isDarkMode ? android.graphics.Color.parseColor("#CCCCCC") : context.getColor(R.color.secondary_text));
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        statusParams.topMargin = dp(12);
        root.addView(statusText, statusParams);

        classesContainer = new LinearLayout(context);
        classesContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams classesParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        classesParams.topMargin = dp(16);
        root.addView(classesContainer, classesParams);

        renderDaySelector();
        return scrollView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        executor = Executors.newSingleThreadExecutor();
        loadTimetable();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("selected_day", selectedDay);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        super.onDestroy();
    }

    private void loadTimetable() {
        SharedPreferences preferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String token = preferences.getString(TOKEN_KEY, null);
        if (TextUtils.isEmpty(token)) {
            showError("Your session has expired. Please sign in again.");
            return;
        }
        executor.execute(() -> {
            try {
                JSONObject profile = getObject("/api/students/me", token);
                String sectionId = profile.optString("sectionId", "");
                if (TextUtils.isEmpty(sectionId) || "null".equalsIgnoreCase(sectionId)) {
                    throw new IllegalStateException("Your academic section could not be found.");
                }
                JSONArray response = getArray("/api/timetable/weekly/" + sectionId, token);
                List<JSONObject> entries = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    JSONObject item = response.optJSONObject(i);
                    if (item != null) entries.add(item);
                }
                entries.sort(
                        Comparator
                                .comparingInt((JSONObject item) -> dayOrder(item.optString("day", "")))
                                .thenComparing(item -> item.optString("startTime", "23:59:59"))
                );
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    timetable = entries;
                    progressBar.setVisibility(View.GONE);
                    statusText.setVisibility(View.GONE);
                    renderDaySelector();
                    renderSelectedDay();
                });
            } catch (Exception e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    showError("Unable to load the timetable. Please check your connection and try again.");
                });
            }
        });
    }

    private void renderDaySelector() {
        if (daySelector == null) return;
        daySelector.removeAllViews();
        Context context = requireContext();

        for (String day : DAYS) {
            Button button = new Button(context);
            button.setText(day.substring(0, 1) + day.substring(1, 3).toLowerCase(Locale.ROOT));
            button.setAllCaps(false);
            button.setTextSize(13);
            button.setTypeface(null, Typeface.BOLD);
            button.setMinWidth(0);
            button.setMinimumWidth(0);
            button.setPadding(dp(16), 0, dp(16), 0);

            GradientDrawable buttonDrawable = new GradientDrawable();
            buttonDrawable.setCornerRadius(dp(12));

            boolean selected = day.equals(selectedDay);
            if (selected) {
                buttonDrawable.setColor(context.getColor(R.color.primary_accent));
                button.setBackground(buttonDrawable);
                button.setTextColor(context.getColor(android.R.color.white));
            } else {
                buttonDrawable.setColor(android.graphics.Color.parseColor("#E6DFD3"));
                button.setBackground(buttonDrawable);
                button.setTextColor(android.graphics.Color.parseColor("#1A1A1A"));
            }

            button.setOnClickListener(v -> {
                selectedDay = day;
                renderDaySelector();
                renderSelectedDay();
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    dp(40)
            );
            params.rightMargin = dp(8);
            daySelector.addView(button, params);
        }
    }

    private void renderSelectedDay() {
        classesContainer.removeAllViews();
        Context context = requireContext();
        boolean isDarkMode = (context.getResources().getConfiguration().uiMode 
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        List<JSONObject> selectedEntries = new ArrayList<>();
        for (JSONObject item : timetable) {
            if (selectedDay.equalsIgnoreCase(item.optString("day", ""))) {
                selectedEntries.add(item);
            }
        }
        selectedEntries.sort(
                Comparator.comparing(item -> item.optString("startTime", "23:59:59"))
        );

        TextView heading = new TextView(context);
        heading.setText(selectedDay.substring(0, 1) + selectedDay.substring(1).toLowerCase(Locale.ROOT));
        heading.setTextSize(20);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setTextColor(isDarkMode ? android.graphics.Color.parseColor("#FFFFFF") : context.getColor(R.color.primary_text));
        
        LinearLayout.LayoutParams headingParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        headingParams.bottomMargin = dp(12);
        classesContainer.addView(heading, headingParams);

        if (selectedEntries.isEmpty()) {
            addEmptyState("No classes are scheduled for this day.");
            return;
        }
        for (JSONObject item : selectedEntries) {
            addClassCard(item);
        }
    }

    private void addClassCard(JSONObject item) {
        Context context = requireContext();
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));

        GradientDrawable background = new GradientDrawable();
        // Always enforce Apple Wallet Cream surface (#E6DFD3)
        background.setColor(android.graphics.Color.parseColor("#E6DFD3"));
        background.setCornerRadius(dp(16));
        card.setBackground(background);
        card.setElevation(dp(2));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(12);
        classesContainer.addView(card, cardParams);

        String startTime = formatTime(item.optString("startTime", ""));
        String endTime = formatTime(item.optString("endTime", ""));

        // Extract Subject Name (with Subject ID fallback)
        String subject = extractValue(item, "subjectName", "subject_name", "subject", "subjectId");
        if (TextUtils.isEmpty(subject)) subject = "Subject";

        // Extract Teacher Name (with Teacher ID fallback)
        String teacher = extractValue(item, "teacherName", "teacher_name", "facultyName", "teacherId");
        if (TextUtils.isEmpty(teacher)) teacher = "Not assigned";

        String room = extractValue(item, "room", "roomNo", "room_no", "location");
        if (TextUtils.isEmpty(room)) room = "Not specified";

        // Header: Time Slot
        TextView time = new TextView(context);
        time.setText(startTime + " - " + endTime);
        time.setTextSize(16);
        time.setTypeface(null, Typeface.BOLD);
        time.setTextColor(android.graphics.Color.parseColor("#1A1A1A"));
        card.addView(time);

        // Subject Name
        addDetail(card, "Subject", subject);
        // Room Location
        addDetail(card, "Room", room);
        // Teacher / Faculty Name
        addDetail(card, "Teacher", teacher);
    }

    private String extractValue(JSONObject object, String... keys) {
        for (String key : keys) {
            String val = object.optString(key, "");
            if (!TextUtils.isEmpty(val) && !"null".equalsIgnoreCase(val)) {
                return val;
            }
        }
        return "";
    }

    private void addDetail(LinearLayout card, String label, String value) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView labelText = new TextView(requireContext());
        labelText.setText(label + ": ");
        labelText.setTextSize(14);
        labelText.setTypeface(null, Typeface.BOLD);
        labelText.setTextColor(android.graphics.Color.parseColor("#333333")); // Crisp dark label

        TextView valueText = new TextView(requireContext());
        valueText.setText(value);
        valueText.setTextSize(14);
        valueText.setTextColor(android.graphics.Color.parseColor("#1A1A1A")); // High-contrast dark value

        row.addView(labelText);
        row.addView(valueText);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(6);
        card.addView(row, params);
    }

    private void addEmptyState(String message) {
        Context context = requireContext();
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(20), dp(24), dp(20), dp(24));

        GradientDrawable background = new GradientDrawable();
        background.setColor(android.graphics.Color.parseColor("#E6DFD3"));
        background.setCornerRadius(dp(16));
        card.setBackground(background);
        card.setElevation(dp(2));

        TextView text = new TextView(context);
        text.setText(message);
        text.setTextSize(14);
        text.setGravity(Gravity.CENTER);
        text.setTextColor(android.graphics.Color.parseColor("#333333"));
        card.addView(text);

        classesContainer.addView(
                card,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
    }

    private void showError(String message) {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (statusText != null) {
            statusText.setVisibility(View.VISIBLE);
            statusText.setText(message);
        }
    }

    private JSONObject getObject(String path, String token) throws Exception {
        return new JSONObject(getString(path, token));
    }

    private JSONArray getArray(String path, String token) throws Exception {
        return new JSONArray(getString(path, token));
    }

    private String getString(String path, String token) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            StringBuilder result = new StringBuilder();
            if (stream != null) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                }
            }
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("Request failed with HTTP " + status);
            }
            return result.toString();
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private int dayOrder(String day) {
        switch (day.toUpperCase(Locale.ROOT)) {
            case "MONDAY": return 1;
            case "TUESDAY": return 2;
            case "WEDNESDAY": return 3;
            case "THURSDAY": return 4;
            case "FRIDAY": return 5;
            case "SATURDAY": return 6;
            case "SUNDAY": return 7;
            default: return 8;
        }
    }

    private String getCurrentDay() {
        switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY: return "MONDAY";
            case Calendar.TUESDAY: return "TUESDAY";
            case Calendar.WEDNESDAY: return "WEDNESDAY";
            case Calendar.THURSDAY: return "THURSDAY";
            case Calendar.FRIDAY: return "FRIDAY";
            case Calendar.SATURDAY: return "SATURDAY";
            case Calendar.SUNDAY:
            default: return "SUNDAY";
        }
    }

    private String formatTime(String value) {
        if (TextUtils.isEmpty(value) || "null".equalsIgnoreCase(value)) return "--:--";
        try {
            String[] parts = value.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            String period = hour >= 12 ? "PM" : "AM";
            int displayHour = hour % 12;
            if (displayHour == 0) displayHour = 12;
            return String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, period);
        } catch (Exception e) {
            return value;
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}