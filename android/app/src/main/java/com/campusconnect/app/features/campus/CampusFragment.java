package com.campusconnect.app.features.campus;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.campusconnect.app.R;
import com.campusconnect.app.features.feedback.FeedbackFragment;

public class CampusFragment extends Fragment {

public CampusFragment() {
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
    root.setPadding(dp(20), dp(24), dp(20), dp(28));

    scrollView.addView(
            root,
            new ScrollView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
    );

    // Header eyebrow
    TextView eyebrow = new TextView(context);
    eyebrow.setText("CAMPUSCONNECT");
    eyebrow.setTextColor(
            context.getColor(R.color.secondary_text)
    );
    eyebrow.setTextSize(12);
    eyebrow.setLetterSpacing(0.15f);
    eyebrow.setTypeface(null, Typeface.BOLD);

    root.addView(eyebrow);

    // Page title
    TextView title = new TextView(context);
    title.setText("Campus");
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

    // Page subtitle
    TextView subtitle = new TextView(context);
    subtitle.setText(
            "Explore campus facilities, events and student services."
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

    subtitleParams.topMargin = dp(6);
    root.addView(subtitle, subtitleParams);

    // Section heading
    TextView sectionHeading = new TextView(context);
    sectionHeading.setText("CAMPUS SERVICES");
    sectionHeading.setTextColor(
            context.getColor(R.color.secondary_text)
    );
    sectionHeading.setTextSize(11);
    sectionHeading.setLetterSpacing(0.08f);
    sectionHeading.setTypeface(null, Typeface.BOLD);

    LinearLayout.LayoutParams sectionParams =
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

    sectionParams.topMargin = dp(28);
    root.addView(sectionHeading, sectionParams);

    // Card 01: Campus Facilities
    addNavigationCard(
            root,
            "Campus Facilities",
            "Explore the facilities available on campus.",
            "01",
            this::openFacilities
    );

    // Card 02: Digital Bus Pass
    addNavigationCard(
            root,
            "Digital Bus Pass",
            "Apply for a bus pass and manage your applications.",
            "02",
            this::openBusPass
    );

    // Card 03: Campus Events
    addNavigationCard(
            root,
            "Campus Events",
            "Discover upcoming events and manage your registrations.",
            "03",
            this::openEvents
    );

    // Card 04: Student Feedback
    addNavigationCard(
            root,
            "Student Feedback",
            "Share feedback and suggestions to improve campus services.",
            "04",
            this::openFeedback
    );

    return scrollView;
}

/**
 * Creates a numbered navigation card using the existing
 * CampusConnect card design.
 */
        private void addNavigationCard(
                LinearLayout parent,
                String heading,
                String description,
                String number,
                Runnable action
        ) {
        Context context = requireContext();

        // Check if dark mode / monochrome is active
        boolean isDarkMode = (context.getResources().getConfiguration().uiMode 
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK) 
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        
        // Ensure high-contrast dark text on the light cream cards
        int cardTextColor = isDarkMode ? android.graphics.Color.parseColor("#1A1A1A") : context.getColor(R.color.primary_text);
        int cardSubColor = isDarkMode ? android.graphics.Color.parseColor("#555555") : context.getColor(R.color.secondary_text);

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(18), dp(16), dp(18));
        card.setBackgroundResource(R.drawable.bg_card);

        if (isDarkMode) {
        card.getBackground().setTint(android.graphics.Color.parseColor("#E6DFD3"));
        }else {
        card.getBackground().setTintList(null);
        }
        
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.topMargin = dp(12);
        parent.addView(card, cardParams);

        // Number
        TextView numberView = new TextView(context);
        numberView.setText(number);
        numberView.setTextColor(cardTextColor); // use dynamic contrast
        numberView.setTextSize(15);
        numberView.setTypeface(null, Typeface.BOLD);
        numberView.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams numberParams =
                new LinearLayout.LayoutParams(
                        dp(42),
                        dp(42)
                );

        card.addView(numberView, numberParams);

        // Text container
        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams textParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                );

        textParams.leftMargin = dp(14);
        card.addView(textContainer, textParams);

        // Card title
        TextView cardTitle = new TextView(context);
        cardTitle.setText(heading);
        cardTitle.setTextColor(cardTextColor); // use dynamic contrast
        cardTitle.setTextSize(16);
        cardTitle.setTypeface(null, Typeface.BOLD);

        textContainer.addView(cardTitle);

        // Card description
        TextView cardDescription = new TextView(context);
        cardDescription.setText(description);
        cardDescription.setTextColor(cardSubColor); // use dynamic contrast
        cardDescription.setTextSize(13);
        cardDescription.setLineSpacing(dp(2), 1f);

        LinearLayout.LayoutParams descriptionParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        descriptionParams.topMargin = dp(5);
        textContainer.addView(
                cardDescription,
                descriptionParams
        );

        // Arrow
        TextView arrow = new TextView(context);
        arrow.setText("›");
        arrow.setTextColor(cardTextColor); // use dynamic contrast
        arrow.setTextSize(28);
        arrow.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams arrowParams =
                new LinearLayout.LayoutParams(
                        dp(24),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        card.addView(arrow, arrowParams);

        // Navigation
        card.setOnClickListener(v -> action.run());
        }

/**
 * Opens the existing Campus Facilities screen.
 */
private void openFacilities() {
    openFragment(new CampusFacilitiesFragment());
}

/**
 * Opens the Digital Bus Pass dashboard.
 */
private void openBusPass() {
    openFragment(new BusPassFragment());
}

/**
 * Opens the Campus Events screen.
 */
private void openEvents() {
    openFragment(new CampusEventsFragment());
}

/**
 * Opens the Student Feedback screen.
 */
private void openFeedback() {
    openFragment(new FeedbackFragment());
}

/**
 * Navigates inside the existing dashboard fragment container.
 */
private void openFragment(Fragment fragment) {
    if (!isAdded()) {
        return;
    }

    getParentFragmentManager()
            .beginTransaction()
            .replace(
                    R.id.fragmentContainer,
                    fragment
            )
            .addToBackStack(null)
            .commit();
}

private int dp(int value) {
    return Math.round(
            value * getResources()
                    .getDisplayMetrics()
                    .density
    );
}

}
