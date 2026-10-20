package com.campusconnect.app.features.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.campusconnect.app.MainActivity;
import com.campusconnect.app.R;
import com.campusconnect.app.core.theme.ThemeManager;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

public class StudentProfileFragment extends Fragment {

    private static final String BASE_URL = "http://127.0.0.1:8080";

    private static final String SESSION_PREFS = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String EMAIL_KEY = "user_email";

    private LinearLayout rootLayout;
    private LinearLayout profileContent;
    private ProgressBar progressBar;

    private TextView statusText;
    private TextView themeValueText;

    private String studentName = "";
    private String studentId = "";
    private String sectionName = "";

    private volatile boolean viewActive;

    public StudentProfileFragment() {
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
                & Configuration.UI_MODE_NIGHT_MASK) 
                == Configuration.UI_MODE_NIGHT_YES;

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(context.getColor(R.color.main_background));

        rootLayout = new LinearLayout(context);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(dp(20), dp(24), dp(20), dp(32));
        rootLayout.setBackgroundColor(context.getColor(R.color.main_background));

        scrollView.addView(rootLayout);

        // Header Title (Fixed contrast)
        TextView heading = createHeaderTitle("Profile & Settings", 26, true, isDarkMode);
        rootLayout.addView(heading, matchWrap());

        // Subtitle (Fixed contrast)
        TextView subtitle = createHeaderTitle("Manage your student information, preferences, and tools.", 14, false, isDarkMode);
        subtitle.setTextColor(isDarkMode ? android.graphics.Color.parseColor("#B0B0B0") : context.getColor(R.color.secondary_text));
        LinearLayout.LayoutParams subtitleParams = matchWrap();
        subtitleParams.topMargin = dp(6);
        rootLayout.addView(subtitle, subtitleParams);

        // --- CARD 1: STUDENT PROFILE (#CBE6F1 - Light Blue, Dark Text) ---
        addSectionHeading("STUDENT PROFILE", isDarkMode);

        LinearLayout profileCard = createCardWithColor(R.color.profile_card_1);
        profileContent = new LinearLayout(context);
        profileContent.setOrientation(LinearLayout.VERTICAL);

        profileCard.addView(profileContent, matchWrap());
        rootLayout.addView(profileCard, matchWrap());

        progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);

        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        progressParams.gravity = Gravity.CENTER_HORIZONTAL;
        progressParams.topMargin = dp(20);
        progressParams.bottomMargin = dp(20);
        rootLayout.addView(progressBar, progressParams);

        statusText = createCustomText("Loading your profile...", 14, false, "#1A1A1A");
        statusText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusParams = matchWrap();
        statusParams.bottomMargin = dp(12);
        rootLayout.addView(statusText, statusParams);

        // --- CARD 2: ACADEMIC TOOLS (#4CA450 - Green, White Text) ---
        addSectionHeading("ACADEMIC TOOLS", isDarkMode);

        LinearLayout calendarCard = createCardWithColor(R.color.profile_card_2);
        TextView calendarTitle = createCustomText("Academic Calendar Sync", 17, true, "#FFFFFF");
        calendarCard.addView(calendarTitle, matchWrap());

        TextView calendarDesc = createCustomText("Sync your semester timetable and examination dates directly with your device's calendar app.", 13, false, "#E0E0E0");
        LinearLayout.LayoutParams calDescParams = matchWrap();
        calDescParams.topMargin = dp(6);
        calendarCard.addView(calendarDesc, calDescParams);

        MaterialButton syncButton = createCustomButton("Sync to Calendar", "#FFFFFF", "#1A1A1A");
        LinearLayout.LayoutParams syncParams = matchWrap();
        syncParams.topMargin = dp(16);
        calendarCard.addView(syncButton, syncParams);

        syncButton.setOnClickListener(v -> syncAcademicCalendar());
        rootLayout.addView(calendarCard, matchWrap());

        // --- CARD 3: ACCOUNT SETTINGS (#FCCA46 - Yellow, Dark Text) ---
        addSectionHeading("ACCOUNT SETTINGS", isDarkMode);

        LinearLayout settingsCard = createCardWithColor(R.color.profile_card_3);

        TextView appearanceHeading = createCustomText("Appearance", 17, true, "#1A1A1A");
        settingsCard.addView(appearanceHeading, matchWrap());

        TextView appearanceDescription = createCustomText("Choose how CampusConnect looks on your device.", 13, false, "#555555");
        LinearLayout.LayoutParams descriptionParams = matchWrap();
        descriptionParams.topMargin = dp(5);
        settingsCard.addView(appearanceDescription, descriptionParams);

        themeValueText = createCustomText("", 14, true, "#1A1A1A");
        LinearLayout.LayoutParams themeValueParams = matchWrap();
        themeValueParams.topMargin = dp(12);
        settingsCard.addView(themeValueText, themeValueParams);

        MaterialButton appearanceButton = createCustomButton("Change appearance", "#1A1A1A", "#FFFFFF");
        LinearLayout.LayoutParams appearanceButtonParams = matchWrap();
        appearanceButtonParams.topMargin = dp(14);
        settingsCard.addView(appearanceButton, appearanceButtonParams);

        appearanceButton.setOnClickListener(v -> showThemeDialog());
        rootLayout.addView(settingsCard, matchWrap());

        // --- CARD 4: SESSION & SUPPORT (#F36E65 - Coral, White Text) ---
        addSectionHeading("SESSION & SUPPORT", isDarkMode);

        LinearLayout logoutCard = createCardWithColor(R.color.profile_card_4);

        TextView logoutDescription = createCustomText("Sign out of your CampusConnect student account on this device.", 14, false, "#FFFFFF");
        logoutCard.addView(logoutDescription, matchWrap());

        MaterialButton logoutButton = createCustomButton("Log out", "#FFFFFF", "#1A1A1A");
        LinearLayout.LayoutParams logoutButtonParams = matchWrap();
        logoutButtonParams.topMargin = dp(16);
        logoutCard.addView(logoutButton, logoutButtonParams);
        logoutButton.setOnClickListener(v -> showLogoutConfirmation());

        // Administrator query notice footer card
        View adminDivider = new View(context);
        adminDivider.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));
        adminDivider.setAlpha(0.4f);
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        divParams.topMargin = dp(16);
        divParams.bottomMargin = dp(16);
        logoutCard.addView(adminDivider, divParams);

        TextView adminNotice = createCustomText("Note: In case of any query related to the application, please meet the administrator.", 12, false, "#FFFFFF");
        logoutCard.addView(adminNotice, matchWrap());

        rootLayout.addView(logoutCard, matchWrap());

        updateThemeLabel();

        return scrollView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewActive = true;
        loadStudentProfile();
    }

    private void syncAcademicCalendar() {
        if (!isAdded()) return;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, "CampusConnect: Semester Classes & Exams")
                .putExtra(CalendarContract.Events.DESCRIPTION, "Synced academic calendar events and examination schedules from CampusConnect.")
                .putExtra(CalendarContract.Events.EVENT_LOCATION, "College Campus")
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendar.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calendar.getTimeInMillis() + (60 * 60 * 1000));

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "No calendar app found to sync events.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadStudentProfile() {
        SharedPreferences preferences = requireContext().getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE);
        String token = preferences.getString(TOKEN_KEY, null);
        String email = preferences.getString(EMAIL_KEY, "");

        if (TextUtils.isEmpty(token)) {
            showProfileFailure("Your session has expired. Please sign in again.");
            return;
        }

        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BASE_URL + "/api/students/me");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + token);

                int statusCode = connection.getResponseCode();
                InputStream stream = (statusCode >= 200 && statusCode < 300) ? connection.getInputStream() : connection.getErrorStream();

                StringBuilder response = new StringBuilder();
                if (stream != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    }
                }

                if (statusCode < 200 || statusCode >= 300) {
                    throw new IllegalStateException("Profile request failed with HTTP " + statusCode);
                }

                JSONObject profile = new JSONObject(response.toString());
                String fetchedName = profile.optString("name", "Student");
                String fetchedStudentId = profile.optString("studentId", "");
                String fetchedSectionName = profile.optString("sectionName", "");

                if (TextUtils.isEmpty(fetchedStudentId)) {
                    throw new IllegalStateException("The student ID was not returned.");
                }

                if (!isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    if (!viewActive || !isAdded()) return;
                    studentName = fetchedName;
                    studentId = fetchedStudentId;
                    sectionName = fetchedSectionName;
                    displayProfile(fetchedName, fetchedStudentId, email, fetchedSectionName);
                });

            } catch (Exception exception) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (!viewActive || !isAdded()) return;
                    showProfileFailure("Unable to load your profile. Check your connection and retry.");
                });
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private void displayProfile(String name, String id, String email, String section) {
        if (profileContent == null || !isAdded()) return;

        profileContent.removeAllViews();

        addProfileRow("Name", name);
        addProfileRow("Student ID", id);
        addProfileRow("Email", TextUtils.isEmpty(email) ? "Not available" : email);
        addProfileRow("Section", TextUtils.isEmpty(section) ? "Not assigned" : section);

        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (statusText != null) statusText.setVisibility(View.GONE);
    }

    private void addProfileRow(String label, String value) {
        Context context = requireContext();
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));

        TextView labelText = createCustomText(label, 12, false, "#555555");
        TextView valueText = createCustomText(TextUtils.isEmpty(value) ? "Not available" : value, 15, true, "#1A1A1A");

        LinearLayout.LayoutParams valueParams = matchWrap();
        valueParams.topMargin = dp(4);

        row.addView(labelText, matchWrap());
        row.addView(valueText, valueParams);
        profileContent.addView(row, matchWrap());

        View divider = new View(context);
        divider.setBackgroundColor(android.graphics.Color.parseColor("#1A1A1A"));
        divider.setAlpha(0.15f);
        profileContent.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
    }

    private void showProfileFailure(String message) {
        if (!isAdded()) return;
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (statusText != null) {
            statusText.setVisibility(View.VISIBLE);
            statusText.setText(message);
        }
    }

    private void showThemeDialog() {
        if (!isAdded()) return;

        String[] themeNames = {
                "Light Theme",
                "Dark Theme",
                "Monochrome Mode",
                "System Default"
        };
        String[] themeValues = {"light", "dark", "monochrome", "system"};

        String selectedTheme = ThemeManager.getTheme(requireContext());
        int selectedIndex = 3;
        for (int i = 0; i < themeValues.length; i++) {
            if (themeValues[i].equals(selectedTheme)) {
                selectedIndex = i;
                break;
            }
        }

        RadioGroup radioGroup = new RadioGroup(requireContext());
        radioGroup.setOrientation(RadioGroup.VERTICAL);
        radioGroup.setPadding(dp(20), dp(8), dp(20), dp(8));

        for (int i = 0; i < themeNames.length; i++) {
            RadioButton radioButton = new RadioButton(requireContext());
            radioButton.setText(themeNames[i]);
            radioButton.setTextSize(15);
            radioButton.setTextColor(requireContext().getColor(R.color.primary_text));
            radioButton.setId(View.generateViewId());
            radioButton.setTag(themeValues[i]);

            radioGroup.addView(radioButton, new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (i == selectedIndex) radioButton.setChecked(true);
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Choose appearance")
                .setView(radioGroup)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Apply", null)
                .create();

        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                int checkedId = radioGroup.getCheckedRadioButtonId();
                if (checkedId == -1) {
                    Toast.makeText(requireContext(), "Select an appearance first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                RadioButton selectedButton = radioGroup.findViewById(checkedId);
                String value = String.valueOf(selectedButton.getTag());
                ThemeManager.setTheme(requireContext(), value);
                dialog.dismiss();
                Toast.makeText(requireContext(), "Appearance updated.", Toast.LENGTH_SHORT).show();
                updateThemeLabel();
                if (getActivity() != null) getActivity().recreate();
            });
        });
        dialog.show();
    }

    private void updateThemeLabel() {
        if (themeValueText == null || !isAdded()) return;
        String theme = ThemeManager.getTheme(requireContext());
        String label;
        if ("light".equals(theme)) label = "Light Theme";
        else if ("dark".equals(theme)) label = "Dark Theme";
        else if ("monochrome".equals(theme)) label = "Monochrome Mode";
        else label = "System Default";

        themeValueText.setText("Current appearance: " + label);
    }

    private void showLogoutConfirmation() {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Log out?")
                .setMessage("Are you sure you want to log out of CampusConnect?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Log out", (dialog, which) -> performLogout())
                .show();
    }

    private void performLogout() {
        if (!isAdded()) return;
        SharedPreferences preferences = requireContext().getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE);
        preferences.edit().clear().apply();

        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void addSectionHeading(String title, boolean isDarkMode) {
        Context context = requireContext();
        TextView heading = createCustomText(title, 12, true, isDarkMode ? "#FFFFFF" : "#1A1A1A");
        heading.setLetterSpacing(0.08f);

        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(26);
        params.bottomMargin = dp(10);
        rootLayout.addView(heading, params);
    }

    private LinearLayout createCardWithColor(int colorResId) {
        Context context = requireContext();
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackgroundResource(R.drawable.bg_card);

        if (card.getBackground() != null) {
            card.getBackground().setTint(context.getColor(colorResId));
        }
        return card;
    }

    private TextView createHeaderTitle(String text, float size, boolean bold, boolean isDarkMode) {
        Context context = requireContext();
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(size);
        textView.setTextColor(context.getColor(R.color.screen_header_text));

        if (bold) {
            textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return textView;
    }

    private TextView createCustomText(String text, float size, boolean bold, String colorHex) {
        Context context = requireContext();
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(size);
        textView.setTextColor(android.graphics.Color.parseColor(colorHex));

        if (bold) {
            textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return textView;
    }

    private MaterialButton createCustomButton(String text, String bgColorHex, String textColorHex) {
        Context context = requireContext();
        MaterialButton button = new MaterialButton(context);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(15);
        button.setCornerRadius(dp(12));
        button.setElevation(0f);
        
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(bgColorHex)));
        button.setTextColor(android.graphics.Color.parseColor(textColorHex));
        return button;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(float value) {
        return (int) (value * requireContext().getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        viewActive = false;
        rootLayout = null;
        profileContent = null;
        progressBar = null;
        statusText = null;
        themeValueText = null;
        super.onDestroyView();
    }
}