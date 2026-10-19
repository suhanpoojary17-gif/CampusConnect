package com.campusconnect.app.features.academics;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    LinearLayout root = new LinearLayout(requireContext());
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(dp(24), dp(24), dp(24), dp(24));

    root.setBackgroundColor(
            requireContext().getColor(R.color.main_background)
    );

    Bundle arguments = getArguments();

    String heading = arguments != null
            ? arguments.getString(ARG_HEADING, "Academics")
            : "Academics";

    String message = arguments != null
            ? arguments.getString(ARG_MESSAGE, "")
            : "";

    TextView title = new TextView(requireContext());
    title.setText(heading);
    title.setTextSize(24);
    title.setTypeface(null, Typeface.BOLD);
    title.setTextColor(
            requireContext().getColor(R.color.primary_text)
    );

    root.addView(title);

    TextView body = new TextView(requireContext());
    body.setText(message);
    body.setTextSize(15);
    body.setTextColor(
            requireContext().getColor(R.color.secondary_text)
    );

    LinearLayout.LayoutParams bodyParams =
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

    bodyParams.topMargin = dp(12);
    root.addView(body, bodyParams);

    TextView back = new TextView(requireContext());
    back.setText("‹  Back to Academics");
    back.setTextSize(15);
    back.setTextColor(
            requireContext().getColor(R.color.primary_accent)
    );
    back.setPadding(0, dp(20), 0, dp(12));

    back.setOnClickListener(v ->
            getParentFragmentManager().popBackStack()
    );

    root.addView(back);

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
