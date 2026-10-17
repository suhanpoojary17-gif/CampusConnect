package com.campusconnect.app.features.navigation;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.campusconnect.app.R;

public class DestinationFragment extends Fragment {

    private static final String ARG_TITLE = "destination_title";
    private static final String ARG_DESCRIPTION =
            "destination_description";

    public static DestinationFragment newInstance(
            String title,
            String description
    ) {
        DestinationFragment fragment = new DestinationFragment();

        Bundle arguments = new Bundle();
        arguments.putString(ARG_TITLE, title);
        arguments.putString(ARG_DESCRIPTION, description);

        fragment.setArguments(arguments);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull android.view.LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        Bundle arguments = getArguments();

        String title = arguments != null
                ? arguments.getString(ARG_TITLE, "CampusConnect")
                : "CampusConnect";

        String description = arguments != null
                ? arguments.getString(
                        ARG_DESCRIPTION,
                        "Your campus, connected."
                )
                : "Your campus, connected.";

        LinearLayout layout = new LinearLayout(requireContext());

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(
                getResources().getDimensionPixelSize(
                        R.dimen.screen_padding
                ),
                getResources().getDimensionPixelSize(
                        R.dimen.screen_padding
                ),
                getResources().getDimensionPixelSize(
                        R.dimen.screen_padding
                ),
                getResources().getDimensionPixelSize(
                        R.dimen.screen_padding
                )
        );

        layout.setBackgroundColor(
                requireContext().getColor(R.color.main_background)
        );

        TextView titleView = new TextView(requireContext());

        titleView.setText(title);
        titleView.setTextSize(24);
        titleView.setTextColor(
                requireContext().getColor(R.color.primary_text)
        );
        titleView.setGravity(Gravity.CENTER);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView descriptionView = new TextView(requireContext());

        descriptionView.setText(description);
        descriptionView.setTextSize(15);
        descriptionView.setTextColor(
                requireContext().getColor(R.color.secondary_text)
        );
        descriptionView.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams descriptionParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        descriptionParams.topMargin =
                getResources().getDimensionPixelSize(
                        R.dimen.item_spacing
                );

        layout.addView(titleView);
        layout.addView(descriptionView, descriptionParams);

        return layout;
    }
}