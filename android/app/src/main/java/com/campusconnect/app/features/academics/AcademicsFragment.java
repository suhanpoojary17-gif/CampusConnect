package com.campusconnect.app.features.academics;

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

public class AcademicsFragment extends Fragment {

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
        root.setPadding(dp(20), dp(24), dp(20), dp(28));

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
                "Everything you need for your academic journey."
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

        TextView sectionHeading = new TextView(context);
        sectionHeading.setText("YOUR ACADEMIC TOOLS");
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

        addNavigationCard(
                root,
                "Attendance",
                "Track attendance and subject-wise percentages.",
                "01",
                this::openAttendanceScreen
        );

        addNavigationCard(
                root,
                "Timetable",
                "Check your weekly class schedule.",
                "02",
                this::openTimetableScreen
        );

        addNavigationCard(
                root,
                "Assignments",
                "View assignments, deadlines and submissions.",
                "03",
                this::openAssignmentsScreen
        );

        addNavigationCard(
                root,
                "Course Materials",
                "Access notes and resources shared by teachers.",
                "04",
                this::openCourseMaterialsScreen
        );

        addNavigationCard(
                root,
                "Internal Assessment Marks",
                "Review your internal tests and graded marks.",
                "05",
                () -> openFragment(
                        new InternalAssessmentMarksFragment()
                )
        );

        addNavigationCard(
                root,
                "Examination Results",
                "View your published examination results.",
                "06",
                () -> openFragment(
                        new ExaminationResultsFragment()
                )
        );

        addNavigationCard(
                root,
                "Examinations",
                "Check examination schedules and details.",
                "07",
                () -> openFragment(
                        new ExaminationsFragment()
                )
        );

        addNavigationCard(
                root,
                "Academic Performance",
                "View your overall and subject-wise academic performance.",
                "08",
                () -> openFragment(
                        new AcademicPerformanceFragment()
                )
        );

        return scrollView;
    }

    private void addNavigationCard(
                LinearLayout parent,
                String heading,
                String description,
                String number,
                Runnable action
        ) {
                Context context = requireContext();

                LinearLayout card = new LinearLayout(context);
                card.setOrientation(LinearLayout.HORIZONTAL);
                card.setGravity(Gravity.CENTER_VERTICAL);
                card.setPadding(dp(16), dp(18), dp(16), dp(18));
                card.setBackgroundResource(R.drawable.bg_card);
                card.setClickable(true);
                card.setFocusable(true);

                LinearLayout.LayoutParams cardParams =
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );

                cardParams.topMargin = dp(12);
                parent.addView(card, cardParams);

                TextView numberView = new TextView(context);
                numberView.setText(number);
                numberView.setTextColor(
                        context.getColor(R.color.primary_accent)
                );
                numberView.setTextSize(15);
                numberView.setTypeface(null, Typeface.BOLD);
                numberView.setGravity(Gravity.CENTER);

                LinearLayout.LayoutParams numberParams =
                        new LinearLayout.LayoutParams(
                                dp(42),
                                dp(42)
                        );

                card.addView(numberView, numberParams);

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

                TextView title = new TextView(context);
                title.setText(heading);
                title.setTextColor(
                        context.getColor(R.color.primary_text)
                );
                title.setTextSize(16);
                title.setTypeface(null, Typeface.BOLD);

                textContainer.addView(title);

                TextView subtitle = new TextView(context);
                subtitle.setText(description);
                subtitle.setTextColor(
                        context.getColor(R.color.secondary_text)
                );
                subtitle.setTextSize(13);
                subtitle.setLineSpacing(dp(2), 1f);

                LinearLayout.LayoutParams subtitleParams =
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );

                subtitleParams.topMargin = dp(5);
                textContainer.addView(subtitle, subtitleParams);

                TextView arrow = new TextView(context);
                arrow.setText("›");
                arrow.setTextColor(
                        context.getColor(R.color.primary_accent)
                );
                arrow.setTextSize(28);
                arrow.setGravity(Gravity.CENTER);

                LinearLayout.LayoutParams arrowParams =
                        new LinearLayout.LayoutParams(
                                dp(24),
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );

                card.addView(arrow, arrowParams);

                card.setOnClickListener(v -> action.run());
        }

        private void openAttendanceScreen() {
                openFragment(new AttendanceFragment());
        }

        private void openTimetableScreen() {
                openFragment(new TimetableFragment());
        }

        private void openAssignmentsScreen() {
                openFragment(new AssignmentsFragment());
        }

        private void openCourseMaterialsScreen() {
                openFragment(new CourseMaterialsFragment());
        }

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


        private void showComingSoon(
        String heading,
        String message
        ) {
        if (!isAdded()) {
        return;
        }

        getParentFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        PlaceholderFragment.newInstance(
                                heading,
                                message
                        )
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