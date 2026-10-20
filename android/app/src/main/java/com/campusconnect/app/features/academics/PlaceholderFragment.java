package com.campusconnect.app.features.academics;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.campusconnect.app.R;

public class PlaceholderFragment extends Fragment {

    private static final String ARG_HEADING = "heading";
    private static final String ARG_MESSAGE = "message";

    public PlaceholderFragment() {
        // Required for Android Fragment recreation.
    }

    public static PlaceholderFragment newInstance(
            String heading,
            String message
    ) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_HEADING, heading);
        arguments.putString(ARG_MESSAGE, message);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        Context context = requireContext();

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(28));
        root.setBackgroundColor(context.getColor(R.color.main_background));

        Bundle arguments = getArguments();
        String heading = arguments != null
                ? arguments.getString(ARG_HEADING, "Academics")
                : "Academics";
        String message = arguments != null
                ? arguments.getString(ARG_MESSAGE, "This section is currently under development.")
                : "This section is currently under development.";

        // Canvas Eyebrow Header
        TextView eyebrow = new TextView(context);
        eyebrow.setText("CAMPUSCONNECT");
        eyebrow.setTextSize(12);
        eyebrow.setLetterSpacing(0.12f);
        eyebrow.setTypeface(null, Typeface.BOLD);
        eyebrow.setTextColor(context.getColor(R.color.primary_accent));
        root.addView(eyebrow);

        // Screen Heading: Adapts to theme (Black in Light, White in Dark)
        TextView screenTitle = new TextView(context);
        screenTitle.setText(heading);
        screenTitle.setTextSize(28);
        screenTitle.setTypeface(null, Typeface.BOLD);
        screenTitle.setTextColor(context.getColor(R.color.primary_accent));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = dp(8);
        titleParams.bottomMargin = dp(20); // Spacing before the card begins
        root.addView(screenTitle, titleParams);

        // Apple Wallet Cream Surface Card
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(20), dp(18), dp(20));

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setShape(GradientDrawable.RECTANGLE);
        cardBg.setColor(context.getColor(R.color.card_surface_cream));
        cardBg.setCornerRadius(dp(16));
        card.setBackground(cardBg);
        card.setElevation(dp(2));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(16);
        root.addView(card, cardParams);

        // Card Title: High-contrast dark text #1A1A1A
        TextView cardHeading = new TextView(context);
        cardHeading.setText("Module Notice");
        cardHeading.setTextSize(17);
        cardHeading.setTypeface(null, Typeface.BOLD);
        cardHeading.setTextColor(context.getColor(R.color.card_text_primary));
        card.addView(cardHeading);

        // Card Body: Rich dark gray #333333
        TextView body = new TextView(context);
        body.setText(message);
        body.setTextSize(14);
        body.setLineSpacing(dp(2), 1.1f);
        body.setTextColor(context.getColor(R.color.card_text_secondary));
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        bodyParams.topMargin = dp(8);
        card.addView(body, bodyParams);

        // Modernized Back Action Button (#E3DCD2 with bold #1A1A1A text)
        Button backButton = new Button(context);
        backButton.setText("‹  Back to Academics");
        backButton.setAllCaps(false);
        backButton.setTextSize(14);
        backButton.setTypeface(null, Typeface.BOLD);
        backButton.setTextColor(context.getColor(R.color.button_secondary_text));

        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setShape(GradientDrawable.RECTANGLE);
        btnBg.setColor(context.getColor(R.color.button_secondary_bg));
        btnBg.setCornerRadius(dp(12));
        backButton.setBackground(btnBg);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(44)
        );
        buttonParams.topMargin = dp(14);
        backButton.setPadding(dp(18), 0, dp(18), 0);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        card.addView(backButton, buttonParams);

        return root;
    }

    private int dp(int value) {
        return Math.round(
                value * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}