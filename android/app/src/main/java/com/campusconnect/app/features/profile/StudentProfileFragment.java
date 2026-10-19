package com.campusconnect.app.features.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class StudentProfileFragment extends Fragment {

    private static final String BASE_URL = "http://127.0.0.1:8080";

    private static final String SESSION_PREFS = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String EMAIL_KEY = "user_email";

    private LinearLayout rootLayout;
    private LinearLayout profileContent;
    private ProgressBar progressBar;

    private TextView nameText;
    private TextView studentIdText;
    private TextView emailText;
    private TextView sectionText;
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

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(
                context.getColor(R.color.main_background)
        );

        rootLayout = new LinearLayout(context);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(dp(20), dp(24), dp(20), dp(32));
        rootLayout.setBackgroundColor(
                context.getColor(R.color.main_background)
        );

        scrollView.addView(rootLayout);

        TextView heading = createText(
                "Profile & Settings",
                27,
                true
        );

        rootLayout.addView(
                heading,
                matchWrap()
        );

        TextView subtitle = createText(
                "Manage your student information and preferences.",
                14,
                false
        );

        subtitle.setTextColor(
                context.getColor(R.color.secondary_text)
        );

        LinearLayout.LayoutParams subtitleParams = matchWrap();
        subtitleParams.topMargin = dp(6);

        rootLayout.addView(subtitle, subtitleParams);

        addSectionHeading("STUDENT PROFILE");

        LinearLayout profileCard = createCard();
        profileContent = new LinearLayout(context);
        profileContent.setOrientation(LinearLayout.VERTICAL);

        profileCard.addView(
                profileContent,
                matchWrap()
        );

        rootLayout.addView(profileCard, matchWrap());

        progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        progressParams.gravity = Gravity.CENTER_HORIZONTAL;
        progressParams.topMargin = dp(20);
        progressParams.bottomMargin = dp(20);

        rootLayout.addView(progressBar, progressParams);

        statusText = createText(
                "Loading your profile...",
                14,
                false
        );

        statusText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams statusParams = matchWrap();
        statusParams.bottomMargin = dp(12);

        rootLayout.addView(statusText, statusParams);

        addSectionHeading("ACCOUNT SETTINGS");

        LinearLayout settingsCard = createCard();

        TextView appearanceHeading = createText(
                "Appearance",
                17,
                true
        );

        settingsCard.addView(
                appearanceHeading,
                matchWrap()
        );

        TextView appearanceDescription = createText(
                "Choose how CampusConnect looks on your device.",
                13,
                false
        );

        appearanceDescription.setTextColor(
                context.getColor(R.color.secondary_text)
        );

        LinearLayout.LayoutParams descriptionParams = matchWrap();
        descriptionParams.topMargin = dp(5);

        settingsCard.addView(
                appearanceDescription,
                descriptionParams
        );

        themeValueText = createText(
                "",
                14,
                true
        );

        LinearLayout.LayoutParams themeValueParams = matchWrap();
        themeValueParams.topMargin = dp(14);

        settingsCard.addView(
                themeValueText,
                themeValueParams
        );

        Button appearanceButton = createButton(
                "Change appearance"
        );

        LinearLayout.LayoutParams appearanceButtonParams = matchWrap();
        appearanceButtonParams.topMargin = dp(12);

        settingsCard.addView(
                appearanceButton,
                appearanceButtonParams
        );

        appearanceButton.setOnClickListener(
                v -> showThemeDialog()
        );

        rootLayout.addView(settingsCard, matchWrap());

        addSectionHeading("SESSION");

        LinearLayout logoutCard = createCard();

        TextView logoutDescription = createText(
                "Sign out of your CampusConnect student account on this device.",
                14,
                false
        );

        logoutDescription.setTextColor(
                context.getColor(R.color.secondary_text)
        );

        logoutCard.addView(
                logoutDescription,
                matchWrap()
        );

        Button logoutButton = createButton("Log out");

        LinearLayout.LayoutParams logoutButtonParams = matchWrap();
        logoutButtonParams.topMargin = dp(16);

        logoutCard.addView(logoutButton, logoutButtonParams);

        logoutButton.setOnClickListener(v -> showLogoutConfirmation());

        rootLayout.addView(logoutCard, matchWrap());

        updateThemeLabel();

        return scrollView;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        viewActive = true;
        loadStudentProfile();
    }

    private void loadStudentProfile() {
        SharedPreferences preferences =
                requireContext().getSharedPreferences(
                        SESSION_PREFS,
                        Context.MODE_PRIVATE
                );

        String token = preferences.getString(TOKEN_KEY, null);
        String email = preferences.getString(EMAIL_KEY, "");

        if (TextUtils.isEmpty(token)) {
            showProfileFailure("Your session has expired. Please sign in again.");
            return;
        }

        emailText = null;

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(BASE_URL + "/api/students/me");

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

                int statusCode = connection.getResponseCode();

                InputStream stream;

                if (statusCode >= 200 && statusCode < 300) {
                    stream = connection.getInputStream();
                } else {
                    stream = connection.getErrorStream();
                }

                StringBuilder response = new StringBuilder();

                if (stream != null) {
                    try (
                            BufferedReader reader =
                                    new BufferedReader(
                                            new InputStreamReader(
                                                    stream,
                                                    StandardCharsets.UTF_8
                                            )
                                    )
                    ) {
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    }
                }

                if (statusCode < 200 || statusCode >= 300) {
                    throw new IllegalStateException(
                            "Profile request failed with HTTP "
                                    + statusCode
                    );
                }

                JSONObject profile = new JSONObject(
                        response.toString()
                );

                String fetchedName = profile.optString(
                        "name",
                        "Student"
                );

                String fetchedStudentId = profile.optString(
                        "studentId",
                        ""
                );

                String fetchedSectionName = profile.optString(
                        "sectionName",
                        ""
                );

                if (TextUtils.isEmpty(fetchedStudentId)) {
                    throw new IllegalStateException(
                            "The student ID was not returned."
                    );
                }

                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    if (!viewActive || !isAdded()) {
                        return;
                    }

                    studentName = fetchedName;
                    studentId = fetchedStudentId;
                    sectionName = fetchedSectionName;

                    displayProfile(
                            fetchedName,
                            fetchedStudentId,
                            email,
                            fetchedSectionName
                    );
                });

            } catch (Exception exception) {
                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    if (!viewActive || !isAdded()) {
                        return;
                    }

                    showProfileFailure(
                            "Unable to load your profile. Check your connection and reopen this screen to retry."
                    );
                });

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private void displayProfile(
            String name,
            String id,
            String email,
            String section
    ) {
        if (profileContent == null || !isAdded()) {
            return;
        }

        profileContent.removeAllViews();

        addProfileRow("Name", name);
        addProfileRow("Student ID", id);

        addProfileRow(
                "Email",
                TextUtils.isEmpty(email) ? "Not available" : email
        );

        addProfileRow(
                "Section",
                TextUtils.isEmpty(section) ? "Not assigned" : section
        );

        progressBar.setVisibility(View.GONE);
        statusText.setVisibility(View.GONE);
    }

    private void addProfileRow(String label, String value) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));

        TextView labelText = createText(label, 12, false);
        labelText.setTextColor(
                requireContext().getColor(R.color.secondary_text)
        );

        TextView valueText = createText(
                TextUtils.isEmpty(value) ? "Not available" : value,
                15,
                true
        );

        LinearLayout.LayoutParams valueParams = matchWrap();
        valueParams.topMargin = dp(4);

        row.addView(labelText, matchWrap());
        row.addView(valueText, valueParams);

        profileContent.addView(row, matchWrap());

        View divider = new View(requireContext());
        divider.setBackgroundColor(
                requireContext().getColor(R.color.card_divider)
        );

        profileContent.addView(
                divider,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(1)
                )
        );
    }

    private void showProfileFailure(String message) {
        if (!isAdded()) {
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        if (statusText != null) {
            statusText.setVisibility(View.VISIBLE);
            statusText.setText(message);
        }

        if (profileContent != null) {
            profileContent.removeAllViews();

            TextView retryMessage = createText(
                    message,
                    14,
                    false
            );

            retryMessage.setTextColor(
                    requireContext().getColor(R.color.secondary_text)
            );

            profileContent.addView(retryMessage, matchWrap());

            Button retryButton = createButton("Retry");

            LinearLayout.LayoutParams retryParams = matchWrap();
            retryParams.topMargin = dp(12);

            profileContent.addView(retryButton, retryParams);
            retryButton.setOnClickListener(v -> {
                progressBar.setVisibility(View.VISIBLE);
                statusText.setVisibility(View.GONE);
                loadStudentProfile();
            });
        }
    }

    private void showThemeDialog() {
        if (!isAdded()) {
            return;
        }

        String[] themeNames = {
                "Light Theme",
                "Dark Theme",
                "System Default"
        };

        String[] themeValues = {
                "light",
                "dark",
                "system"
        };

        String selectedTheme = ThemeManager.getTheme(requireContext());

        int selectedIndex = 2;

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
            radioButton.setTextColor(
                    requireContext().getColor(R.color.primary_text)
            );

            radioButton.setId(View.generateViewId());
            radioButton.setTag(themeValues[i]);

            radioGroup.addView(
                    radioButton,
                    new RadioGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    )
            );

            if (i == selectedIndex) {
                radioButton.setChecked(true);
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Choose appearance")
                .setView(radioGroup)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Apply", null)
                .create();

        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        int checkedId = radioGroup.getCheckedRadioButtonId();

                        if (checkedId == -1) {
                            Toast.makeText(
                                    requireContext(),
                                    "Select an appearance first.",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        RadioButton selectedButton =
                                radioGroup.findViewById(checkedId);

                        String value = String.valueOf(
                                selectedButton.getTag()
                        );

                        ThemeManager.setTheme(
                                requireContext(),
                                value
                        );

                        dialog.dismiss();

                        Toast.makeText(
                                requireContext(),
                                "Appearance updated.",
                                Toast.LENGTH_SHORT
                        ).show();

                        updateThemeLabel();
                    });
        });

        dialog.show();
    }

    private void updateThemeLabel() {
        if (themeValueText == null || !isAdded()) {
            return;
        }

        String theme = ThemeManager.getTheme(requireContext());
        String label;

        if ("light".equals(theme)) {
            label = "Light Theme";
        } else if ("dark".equals(theme)) {
            label = "Dark Theme";
        } else {
            label = "System Default";
        }

        themeValueText.setText("Current appearance: " + label);
    }

    private void showLogoutConfirmation() {
        if (!isAdded()) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Log out?")
                .setMessage(
                        "Are you sure you want to log out of CampusConnect?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Log out", (dialog, which) ->
                        performLogout()
                )
                .show();
    }

    private void performLogout() {
        if (!isAdded()) {
            return;
        }

        SharedPreferences preferences =
                requireContext().getSharedPreferences(
                        SESSION_PREFS,
                        Context.MODE_PRIVATE
                );

        // Clear authentication data but preserve appearance preferences.
        preferences.edit().clear().apply();

        Intent intent = new Intent(
                requireContext(),
                MainActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
    }

    private void addSectionHeading(String title) {
        TextView heading = createText(title, 12, true);

        heading.setLetterSpacing(0.08f);
        heading.setTextColor(
                requireContext().getColor(R.color.secondary_text)
        );

        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(26);
        params.bottomMargin = dp(10);

        rootLayout.addView(heading, params);
    }

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackgroundResource(R.drawable.bg_card);

        return card;
    }

    private TextView createText(
            String text,
            float size,
            boolean bold
    ) {
        TextView textView = new TextView(requireContext());

        textView.setText(text);
        textView.setTextSize(size);
        textView.setTextColor(
                requireContext().getColor(R.color.primary_text)
        );

        if (bold) {
            textView.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        return textView;
    }

    private Button createButton(String text) {
        Button button = new Button(requireContext());

        button.setText(text);
        button.setAllCaps(false);

        return button;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private int dp(float value) {
        return (int) (
                value * requireContext()
                        .getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    @Override
    public void onDestroyView() {
        viewActive = false;

        rootLayout = null;
        profileContent = null;
        progressBar = null;
        nameText = null;
        studentIdText = null;
        emailText = null;
        sectionText = null;
        statusText = null;
        themeValueText = null;

        super.onDestroyView();
    }
}