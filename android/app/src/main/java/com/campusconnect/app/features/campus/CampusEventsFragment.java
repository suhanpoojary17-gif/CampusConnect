package com.campusconnect.app.features.campus;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CampusEventsFragment extends Fragment {

    private static final String BASE_URL =
            "http://127.0.0.1:8080";

    private static final String PREFS_NAME =
            "campusconnect_session";

    private static final String TOKEN_KEY =
            "jwt_token";

    private static final int COLOR_BACKGROUND =
            Color.rgb(247, 248, 252);

    private static final int COLOR_CARD =
            Color.WHITE;

    private static final int COLOR_PRIMARY =
            Color.rgb(72, 87, 220);

    private static final int COLOR_TEXT =
            Color.rgb(32, 36, 48);

    private static final int COLOR_SECONDARY =
            Color.rgb(112, 119, 137);

    private static final int COLOR_BORDER =
            Color.rgb(226, 229, 238);

    private static final int COLOR_GREEN =
            Color.rgb(28, 137, 87);

    private static final int COLOR_RED =
            Color.rgb(190, 58, 65);

    private static final int COLOR_ORANGE =
            Color.rgb(191, 116, 24);

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private ExecutorService executor;

    private LinearLayout root;
    private LinearLayout eventContainer;
    private ProgressBar progressBar;
    private TextView statusText;
    private Button eventsButton;
    private Button registrationsButton;

    private JSONArray events = new JSONArray();
    private JSONArray registrations = new JSONArray();

    private boolean showingMyRegistrations = false;
    private boolean loading = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull android.view.LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        executor = Executors.newSingleThreadExecutor();

        Context context = requireContext();

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(COLOR_BACKGROUND);

        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(28));

        scrollView.addView(
                root,
                new ScrollView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        buildHeader();
        buildNavigation();
        buildStatusArea();

        eventContainer = new LinearLayout(context);
        eventContainer.setOrientation(LinearLayout.VERTICAL);

        root.addView(
                eventContainer,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        refreshData();

        return scrollView;
    }

    private void buildHeader() {

        TextView eyebrow = createText(
                "CAMPUSCONNECT",
                12,
                COLOR_PRIMARY,
                true
        );

        eyebrow.setLetterSpacing(0.12f);

        root.addView(
                eyebrow,
                marginParams(0, 0, 0, 8)
        );

        TextView title = createText(
                "Campus Events",
                28,
                COLOR_TEXT,
                true
        );

        root.addView(
                title,
                marginParams(0, 0, 0, 8)
        );

        TextView subtitle = createText(
                "Discover campus activities, register for events, "
                        + "and keep track of your registrations.",
                14,
                COLOR_SECONDARY,
                false
        );

        subtitle.setLineSpacing(dp(3), 1.0f);

        root.addView(
                subtitle,
                marginParams(0, 0, 0, 22)
        );
    }

    private void buildNavigation() {

        LinearLayout navigation = new LinearLayout(
                requireContext()
        );

        navigation.setOrientation(LinearLayout.HORIZONTAL);
        navigation.setGravity(Gravity.CENTER_VERTICAL);

        eventsButton = new Button(requireContext());
        eventsButton.setText("All Events");
        eventsButton.setAllCaps(false);
        eventsButton.setOnClickListener(v -> {
            if (!loading) {
                showingMyRegistrations = false;
                updateNavigation();
                renderContent();
            }
        });

        registrationsButton = new Button(requireContext());
        registrationsButton.setText("My Registrations");
        registrationsButton.setAllCaps(false);
        registrationsButton.setOnClickListener(v -> {
            if (!loading) {
                showingMyRegistrations = true;
                updateNavigation();
                renderContent();
            }
        });

        navigation.addView(
                eventsButton,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        navigation.addView(
                registrationsButton,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        root.addView(
                navigation,
                marginParams(0, 0, 0, 14)
        );

        updateNavigation();
    }

    private void updateNavigation() {

        if (eventsButton == null
                || registrationsButton == null) {
            return;
        }

        eventsButton.setTextColor(
                showingMyRegistrations
                        ? COLOR_SECONDARY
                        : COLOR_PRIMARY
        );

        registrationsButton.setTextColor(
                showingMyRegistrations
                        ? COLOR_PRIMARY
                        : COLOR_SECONDARY
        );

        eventsButton.setTypeface(
                null,
                showingMyRegistrations
                        ? Typeface.NORMAL
                        : Typeface.BOLD
        );

        registrationsButton.setTypeface(
                null,
                showingMyRegistrations
                        ? Typeface.BOLD
                        : Typeface.NORMAL
        );
    }

    private void buildStatusArea() {

        LinearLayout statusContainer = new LinearLayout(
                requireContext()
        );

        statusContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        statusContainer.setGravity(Gravity.CENTER_HORIZONTAL);

        progressBar = new ProgressBar(requireContext());

        statusContainer.addView(
                progressBar,
                new LinearLayout.LayoutParams(
                        dp(36),
                        dp(36)
                )
        );

        statusText = createText(
                "Loading campus events...",
                14,
                COLOR_SECONDARY,
                false
        );

        statusText.setGravity(Gravity.CENTER);

        statusContainer.addView(
                statusText,
                marginParams(0, 10, 0, 8)
        );

        Button retryButton = new Button(requireContext());
        retryButton.setText("Retry");
        retryButton.setAllCaps(false);
        retryButton.setVisibility(View.GONE);
        retryButton.setOnClickListener(v -> refreshData());

        statusContainer.addView(
                retryButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        statusContainer.setTag(retryButton);

        root.addView(
                statusContainer,
                marginParams(0, 4, 0, 12)
        );
    }

    private Button getRetryButton() {

        if (root == null) {
            return null;
        }

        for (int i = 0; i < root.getChildCount(); i++) {

            View child = root.getChildAt(i);

            if (child instanceof LinearLayout) {

                Object tag = child.getTag();

                if (tag instanceof Button) {
                    return (Button) tag;
                }
            }
        }

        return null;
    }

    private void refreshData() {

        if (!isAdded() || loading) {
            return;
        }

        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        loading = true;

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        if (statusText != null) {
            statusText.setVisibility(View.VISIBLE);
            statusText.setText("Loading campus events...");
        }

        Button retryButton = getRetryButton();

        if (retryButton != null) {
            retryButton.setVisibility(View.GONE);
        }

        if (eventContainer != null) {
            eventContainer.removeAllViews();
        }

        executor.execute(() -> {

            JSONArray fetchedEvents = null;
            JSONArray fetchedRegistrations = null;
            String error = null;

            try {

                Context context =
                        requireContext().getApplicationContext();

                SharedPreferences preferences =
                        context.getSharedPreferences(
                                PREFS_NAME,
                                Context.MODE_PRIVATE
                        );

                String token = preferences.getString(
                        TOKEN_KEY,
                        null
                );

                if (token == null || token.trim().isEmpty()) {
                    throw new Exception(
                            "Your session has expired. Please log in again."
                    );
                }

                String eventResponse = request(
                        "GET",
                        "/api/events",
                        token
                );

                fetchedEvents = new JSONArray(eventResponse);

                try {

                    String registrationResponse = request(
                            "GET",
                            "/api/events/my-registrations",
                            token
                    );

                    fetchedRegistrations =
                            new JSONArray(registrationResponse);

                } catch (Exception registrationError) {

                    /*
                     * Keep event browsing available even if the
                     * registrations request fails.
                     */
                    fetchedRegistrations = new JSONArray();

                    error = registrationError.getMessage();
                }

            } catch (Exception exception) {

                error = exception.getMessage();
            }

            final JSONArray finalEvents = fetchedEvents;
            final JSONArray finalRegistrations =
                    fetchedRegistrations;
            final String finalError = error;

            mainHandler.post(() -> {

                if (!isAdded() || root == null) {
                    return;
                }

                loading = false;

                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                if (finalEvents == null) {

                    events = new JSONArray();
                    registrations = new JSONArray();

                    showError(
                            finalError == null
                                    ? "Unable to load campus events."
                                    : finalError
                    );

                    return;
                }

                events = finalEvents;

                registrations =
                        finalRegistrations == null
                                ? new JSONArray()
                                : finalRegistrations;

                if (finalError != null
                        && showingMyRegistrations) {

                    showError(
                            "Unable to load your registrations. "
                                    + finalError
                    );

                    return;
                }

                if (finalError != null
                        && statusText != null) {

                    statusText.setText(
                            "Your events loaded, but registration "
                                    + "information could not be refreshed."
                    );
                }

                renderContent();
            });
        });
    }

    private String request(
            String method,
            String path,
            String token
    ) throws Exception {

        HttpURLConnection connection = null;

        try {

            URL url = new URL(BASE_URL + path);

            connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod(method);

            connection.setConnectTimeout(15000);
            connection.setReadTimeout(20000);

            connection.setRequestProperty(
                    "Accept",
                    "application/json"
            );

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + token
            );

            connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
            );

            int responseCode =
                    connection.getResponseCode();

            InputStream inputStream;

            if (responseCode >= 200 && responseCode < 300) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }

            StringBuilder response = new StringBuilder();

            if (inputStream != null) {

                try (BufferedReader reader =
                             new BufferedReader(
                                     new InputStreamReader(
                                             inputStream,
                                             StandardCharsets.UTF_8
                                     )
                             )) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
            }

            String responseBody = response.toString();

            if (responseCode < 200 || responseCode >= 300) {

                throw new Exception(
                        extractErrorMessage(
                                responseBody,
                                responseCode
                        )
                );
            }

            return responseBody;

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String extractErrorMessage(
            String body,
            int statusCode
    ) {

        if (statusCode == 401) {
            return "Your session has expired. Please log in again.";
        }

        if (statusCode == 403) {
            return "You do not have permission to perform this action.";
        }

        if (body != null && !body.trim().isEmpty()) {

            try {

                JSONObject json = new JSONObject(body);

                String[] possibleKeys = {
                        "message",
                        "detail",
                        "error"
                };

                for (String key : possibleKeys) {

                    if (json.has(key)
                            && !json.isNull(key)) {

                        String value = json.optString(key, "");

                        if (!value.trim().isEmpty()) {
                            return value;
                        }
                    }
                }

            } catch (Exception ignored) {
                // Fall through to a generic error.
            }
        }

        return "Request failed (HTTP "
                + statusCode
                + "). Please try again.";
    }

    private void renderContent() {

        if (eventContainer == null || statusText == null) {
            return;
        }

        eventContainer.removeAllViews();

        statusText.setVisibility(View.GONE);

        Button retryButton = getRetryButton();

        if (retryButton != null) {
            retryButton.setVisibility(View.GONE);
        }

        updateNavigation();

        if (showingMyRegistrations) {
            renderMyRegistrations();
        } else {
            renderEvents();
        }
    }

    private void renderEvents() {

        if (events.length() == 0) {

            showEmptyState(
                    "No campus events yet",
                    "Published campus events will appear here."
            );

            return;
        }

        LinearLayout upcomingContainer =
                new LinearLayout(requireContext());

        upcomingContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout pastContainer =
                new LinearLayout(requireContext());

        pastContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        int upcomingCount = 0;
        int pastCount = 0;

        Date now = new Date();

        for (int i = 0; i < events.length(); i++) {

            JSONObject event = events.optJSONObject(i);

            if (event == null) {
                continue;
            }

            String startText =
                    event.optString("startDateTime", "");

            Date startDate = parseDate(startText);

            if (startDate != null
                    && startDate.before(now)) {

                pastCount++;

                pastContainer.addView(
                        createEventCard(event, true),
                        marginParams(0, 0, 0, 14)
                );

            } else {

                upcomingCount++;

                upcomingContainer.addView(
                        createEventCard(event, false),
                        marginParams(0, 0, 0, 14)
                );
            }
        }

        if (upcomingCount > 0) {

            addSectionHeading(
                    "Upcoming Events",
                    upcomingCount + " event"
                            + (upcomingCount == 1 ? "" : "s")
            );

            eventContainer.addView(
                    upcomingContainer,
                    marginParams(0, 0, 0, 8)
            );
        }

        if (pastCount > 0) {

            addSectionHeading(
                    "Past Events",
                    pastCount + " event"
                            + (pastCount == 1 ? "" : "s")
            );

            eventContainer.addView(
                    pastContainer,
                    marginParams(0, 0, 0, 8)
            );
        }

        if (upcomingCount == 0 && pastCount == 0) {

            showEmptyState(
                    "No events available",
                    "Check back later for campus activities."
            );
        }
    }

    private View createEventCard(
            JSONObject event,
            boolean pastEvent
    ) {

        LinearLayout card = new LinearLayout(
                requireContext()
        );

        card.setOrientation(LinearLayout.VERTICAL);

        card.setPadding(
                dp(17),
                dp(17),
                dp(17),
                dp(17)
        );

        card.setBackground(createCardBackground());

        TextView category = createText(
                event.optString(
                        "category",
                        "Campus Event"
                ).toUpperCase(Locale.getDefault()),
                11,
                COLOR_PRIMARY,
                true
        );

        category.setLetterSpacing(0.05f);

        card.addView(
                category,
                marginParams(0, 0, 0, 8)
        );

        TextView title = createText(
                event.optString(
                        "title",
                        "Untitled Event"
                ),
                19,
                COLOR_TEXT,
                true
        );

        card.addView(
                title,
                marginParams(0, 0, 0, 8)
        );

        TextView venue = createText(
                "📍 " + event.optString(
                        "venue",
                        "Venue not specified"
                ),
                13,
                COLOR_SECONDARY,
                false
        );

        card.addView(
                venue,
                marginParams(0, 0, 0, 7)
        );

        TextView date = createText(
                "🗓 " + formatDateTime(
                        event.optString(
                                "startDateTime",
                                ""
                        )
                ),
                13,
                COLOR_SECONDARY,
                false
        );

        card.addView(
                date,
                marginParams(0, 0, 0, 10)
        );

        String description = event.optString(
                "description",
                ""
        );

        if (!description.trim().isEmpty()) {

            TextView descriptionView = createText(
                    description,
                    14,
                    COLOR_SECONDARY,
                    false
            );

            descriptionView.setMaxLines(3);

            card.addView(
                    descriptionView,
                    marginParams(0, 0, 0, 12)
            );
        }

        LinearLayout informationRow = new LinearLayout(
                requireContext()
        );

        informationRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        informationRow.setGravity(Gravity.CENTER_VERTICAL);

        int registrationCount = event.optInt(
                "registrationCount",
                0
        );

        Integer capacity = getCapacity(event);

        String capacityText;

        if (capacity == null) {

            capacityText = registrationCount
                    + " registered";

        } else {

            capacityText = registrationCount
                    + " / "
                    + capacity
                    + " registered";
        }

        TextView registrationsText = createText(
                capacityText,
                12,
                COLOR_SECONDARY,
                false
        );

        informationRow.addView(
                registrationsText,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        TextView eventStatus = createText(
                pastEvent ? "PAST EVENT" : "VIEW DETAILS",
                11,
                pastEvent ? COLOR_SECONDARY : COLOR_PRIMARY,
                true
        );

        informationRow.addView(eventStatus);

        card.addView(
                informationRow,
                marginParams(0, 0, 0, 4)
        );

        card.setOnClickListener(
                v -> showEventDetails(event)
        );

        return card;
    }

    private void renderMyRegistrations() {

        if (registrations.length() == 0) {

            showEmptyState(
                    "No registrations yet",
                    "When you register for a campus event, "
                            + "it will appear here."
            );

            return;
        }

        addSectionHeading(
                "My Registrations",
                registrations.length()
                        + " record"
                        + (registrations.length() == 1 ? "" : "s")
        );

        for (int i = 0; i < registrations.length(); i++) {

            JSONObject registration =
                    registrations.optJSONObject(i);

            if (registration == null) {
                continue;
            }

            eventContainer.addView(
                    createRegistrationCard(registration),
                    marginParams(0, 0, 0, 14)
            );
        }
    }

    private View createRegistrationCard(
            JSONObject registration
    ) {

        LinearLayout card = new LinearLayout(
                requireContext()
        );

        card.setOrientation(LinearLayout.VERTICAL);

        card.setPadding(
                dp(17),
                dp(17),
                dp(17),
                dp(17)
        );

        card.setBackground(createCardBackground());

        TextView title = createText(
                registration.optString(
                        "eventTitle",
                        "Campus Event"
                ),
                18,
                COLOR_TEXT,
                true
        );

        card.addView(
                title,
                marginParams(0, 0, 0, 9)
        );

        TextView venue = createText(
                "📍 " + registration.optString(
                        "venue",
                        "Venue not specified"
                ),
                13,
                COLOR_SECONDARY,
                false
        );

        card.addView(
                venue,
                marginParams(0, 0, 0, 7)
        );

        TextView date = createText(
                "🗓 " + formatDateTime(
                        registration.optString(
                                "eventStartDateTime",
                                ""
                        )
                ),
                13,
                COLOR_SECONDARY,
                false
        );

        card.addView(
                date,
                marginParams(0, 0, 0, 10)
        );

        String registrationStatus =
                registration.optString(
                        "status",
                        "UNKNOWN"
                );

        boolean isRegistered =
                "REGISTERED".equalsIgnoreCase(
                        registrationStatus
                );

        TextView status = createText(
                isRegistered
                        ? "● REGISTERED"
                        : "● CANCELLED",
                12,
                isRegistered ? COLOR_GREEN : COLOR_RED,
                true
        );

        card.addView(
                status,
                marginParams(0, 0, 0, 8)
        );

        TextView registeredAt = createText(
                "Registration date: "
                        + formatDateTime(
                                registration.optString(
                                        "registeredAt",
                                        ""
                                )
                        ),
                12,
                COLOR_SECONDARY,
                false
        );

        card.addView(
                registeredAt,
                marginParams(0, 0, 0, 12)
        );

        Button actionButton = new Button(
                requireContext()
        );

        UUIDHolder holder = new UUIDHolder(
                registration.optString("eventId", "")
        );

        JSONObject matchingEvent =
                findEvent(holder.eventId);

        Date eventStart = parseDate(
                registration.optString(
                        "eventStartDateTime",
                        ""
                )
        );

        boolean eventHasNotStarted =
                eventStart != null
                        && eventStart.after(new Date());

        if (isRegistered && eventHasNotStarted) {

            actionButton.setText("Cancel Registration");
            actionButton.setAllCaps(false);

            actionButton.setOnClickListener(v ->
                    confirmCancellation(
                            holder.eventId,
                            registration.optString(
                                    "eventTitle",
                                    "this event"
                            )
                    )
            );

            card.addView(
                    actionButton,
                    marginParams(0, 0, 0, 0)
            );

        } else if (!isRegistered && matchingEvent != null) {

            actionButton.setText("View Event");
            actionButton.setAllCaps(false);

            actionButton.setOnClickListener(v ->
                    showEventDetails(matchingEvent)
            );

            card.addView(
                    actionButton,
                    marginParams(0, 0, 0, 0)
            );
        }

        return card;
    }

    private static class UUIDHolder {

        final String eventId;

        UUIDHolder(String eventId) {
            this.eventId = eventId;
        }
    }

    private JSONObject findEvent(String eventId) {

        if (eventId == null || eventId.trim().isEmpty()) {
            return null;
        }

        for (int i = 0; i < events.length(); i++) {

            JSONObject event = events.optJSONObject(i);

            if (event != null
                    && eventId.equals(
                            event.optString("id", "")
                    )) {

                return event;
            }
        }

        return null;
    }

    private void showEventDetails(JSONObject event) {

        if (!isAdded()) {
            return;
        }

        String eventId = event.optString("id", "");

        String title = event.optString(
                "title",
                "Campus Event"
        );

        String description = event.optString(
                "description",
                "No description provided."
        );

        String category = event.optString(
                "category",
                "General"
        );

        String venue = event.optString(
                "venue",
                "Venue not specified"
        );

        String start = formatDateTime(
                event.optString("startDateTime", "")
        );

        String end = formatDateTime(
                event.optString("endDateTime", "")
        );

        String deadline = formatDateTime(
                event.optString("registrationDeadline", "")
        );

        Integer capacity = getCapacity(event);

        int registrationCount = event.optInt(
                "registrationCount",
                0
        );

        String capacityInfo;

        if (capacity == null) {

            capacityInfo = registrationCount
                    + " registrations; no capacity limit";

        } else {

            capacityInfo = registrationCount
                    + " / "
                    + capacity
                    + " registrations";
        }

        String details =
                category
                        + "\n\n"
                        + description
                        + "\n\nVenue: "
                        + venue
                        + "\n\nStarts: "
                        + start
                        + "\nEnds: "
                        + end
                        + "\n\nRegistration deadline: "
                        + deadline
                        + "\n\n"
                        + capacityInfo;

        boolean published = event.optBoolean(
                "published",
                true
        );

        Date startDate = parseDate(
                event.optString("startDateTime", "")
        );

        Date deadlineDate = parseDate(
                event.optString(
                        "registrationDeadline",
                        ""
                )
        );

        Date endDate = parseDate(
                event.optString("endDateTime", "")
        );

        Date now = new Date();

        boolean eventNotStarted =
                startDate != null
                        && startDate.after(now);

        boolean deadlineOpen =
                deadlineDate != null
                        && deadlineDate.after(now);

        boolean eventNotEnded =
                endDate != null
                        && endDate.after(now);

        boolean capacityAvailable =
                capacity == null
                        || registrationCount < capacity;

        JSONObject existingRegistration =
                findRegistration(eventId);

        String existingStatus =
                existingRegistration == null
                        ? ""
                        : existingRegistration.optString(
                                "status",
                                ""
                        );

        boolean alreadyRegistered =
                "REGISTERED".equalsIgnoreCase(
                        existingStatus
                );

        boolean canRegister =
                published
                        && eventNotStarted
                        && eventNotEnded
                        && deadlineOpen
                        && capacityAvailable
                        && !alreadyRegistered;

        boolean canCancel =
                alreadyRegistered
                        && eventNotStarted;

        String actionLabel;

        if (alreadyRegistered) {

            actionLabel = canCancel
                    ? "Cancel Registration"
                    : "Already Registered";

        } else if (!published) {

            actionLabel = "Event Unavailable";

        } else if (!eventNotEnded) {

            actionLabel = "Event Ended";

        } else if (!eventNotStarted) {

            actionLabel = "Event Started";

        } else if (!deadlineOpen) {

            actionLabel = "Registration Closed";

        } else if (!capacityAvailable) {

            actionLabel = "Event Full";

        } else {

            actionLabel = existingRegistration == null
                    ? "Register"
                    : "Register Again";
        }

        AlertDialog dialog = new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(title)
                .setMessage(details)
                .setPositiveButton(
                        actionLabel,
                        null
                )
                .setNegativeButton(
                        "Close",
                        (d, which) -> d.dismiss()
                )
                .create();

        dialog.setOnShowListener(ignored -> {

            Button positiveButton =
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    );

            if (!canRegister && !canCancel) {

                positiveButton.setTextColor(
                        COLOR_SECONDARY
                );

                positiveButton.setOnClickListener(
                        v -> dialog.dismiss()
                );

            } else if (canCancel) {

                positiveButton.setTextColor(COLOR_RED);

                positiveButton.setOnClickListener(v -> {

                    dialog.dismiss();

                    confirmCancellation(
                            eventId,
                            title
                    );
                });

            } else {

                positiveButton.setTextColor(COLOR_PRIMARY);

                positiveButton.setOnClickListener(v -> {

                    dialog.dismiss();

                    confirmRegistration(
                            eventId,
                            title
                    );
                });
            }
        });

        dialog.show();
    }

    private JSONObject findRegistration(String eventId) {

        for (int i = 0; i < registrations.length(); i++) {

            JSONObject registration =
                    registrations.optJSONObject(i);

            if (registration != null
                    && eventId.equals(
                            registration.optString(
                                    "eventId",
                                    ""
                            )
                    )) {

                return registration;
            }
        }

        return null;
    }

    private Integer getCapacity(JSONObject event) {

        if (event.isNull("capacity")
                || !event.has("capacity")) {

            return null;
        }

        Object value = event.opt("capacity");

        if (value == null
                || value == JSONObject.NULL) {

            return null;
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private void confirmRegistration(
            String eventId,
            String eventTitle
    ) {

        if (eventId == null || eventId.trim().isEmpty()) {
            showMessage("Invalid event ID.");
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Register for event?")
                .setMessage(
                        "Do you want to register for "
                                + eventTitle
                                + "?"
                )
                .setPositiveButton(
                        "Register",
                        (dialog, which) ->
                                performRegistration(eventId)
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    private void confirmCancellation(
            String eventId,
            String eventTitle
    ) {

        if (eventId == null || eventId.trim().isEmpty()) {
            showMessage("Invalid event ID.");
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Cancel registration?")
                .setMessage(
                        "Are you sure you want to cancel "
                                + "your registration for "
                                + eventTitle
                                + "?"
                )
                .setPositiveButton(
                        "Cancel Registration",
                        (dialog, which) ->
                                performCancellation(eventId)
                )
                .setNegativeButton(
                        "Keep Registration",
                        null
                )
                .show();
    }

    private void performRegistration(String eventId) {

        performAction(
                "POST",
                "/api/events/" + eventId + "/register",
                "Registering for event..."
        );
    }

    private void performCancellation(String eventId) {

        performAction(
                "DELETE",
                "/api/events/" + eventId + "/register",
                "Cancelling registration..."
        );
    }

    private void performAction(
            String method,
            String path,
            String progressMessage
    ) {

        if (!isAdded() || loading) {
            return;
        }

        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        loading = true;

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        if (statusText != null) {
            statusText.setVisibility(View.VISIBLE);
            statusText.setText(progressMessage);
        }

        executor.execute(() -> {

            String error = null;

            try {

                Context context =
                        requireContext().getApplicationContext();

                SharedPreferences preferences =
                        context.getSharedPreferences(
                                PREFS_NAME,
                                Context.MODE_PRIVATE
                        );

                String token = preferences.getString(
                        TOKEN_KEY,
                        null
                );

                if (token == null || token.trim().isEmpty()) {
                    throw new Exception(
                            "Your session has expired. Please log in again."
                    );
                }

                request(method, path, token);

            } catch (Exception exception) {

                error = exception.getMessage();
            }

            final String actionError = error;

            mainHandler.post(() -> {

                if (!isAdded() || root == null) {
                    return;
                }

                loading = false;

                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                if (actionError != null) {

                    if (statusText != null) {
                        statusText.setVisibility(View.GONE);
                    }

                    new AlertDialog.Builder(requireContext())
                            .setTitle("Unable to complete action")
                            .setMessage(actionError)
                            .setPositiveButton(
                                    "OK",
                                    (dialog, which) -> refreshData()
                            )
                            .show();

                    return;
                }

                String successMessage =
                        "POST".equals(method)
                                ? "Registration successful."
                                : "Registration cancelled.";

                new AlertDialog.Builder(requireContext())
                        .setTitle("Success")
                        .setMessage(successMessage)
                        .setPositiveButton(
                                "OK",
                                (dialog, which) -> refreshData()
                        )
                        .show();
            });
        });
    }

    private void addSectionHeading(
            String title,
            String subtitle
    ) {

        LinearLayout heading = new LinearLayout(
                requireContext()
        );

        heading.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView titleView = createText(
                title,
                20,
                COLOR_TEXT,
                true
        );

        heading.addView(
                titleView,
                marginParams(0, 0, 0, 4)
        );

        TextView subtitleView = createText(
                subtitle,
                12,
                COLOR_SECONDARY,
                false
        );

        heading.addView(
                subtitleView,
                marginParams(0, 0, 0, 0)
        );

        eventContainer.addView(
                heading,
                marginParams(0, 4, 0, 15)
        );
    }

    private void showEmptyState(
            String title,
            String message
    ) {

        LinearLayout card = new LinearLayout(
                requireContext()
        );

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setGravity(Gravity.CENTER);

        card.setPadding(
                dp(22),
                dp(28),
                dp(22),
                dp(28)
        );

        card.setBackground(createCardBackground());

        TextView titleView = createText(
                title,
                18,
                COLOR_TEXT,
                true
        );

        titleView.setGravity(Gravity.CENTER);

        card.addView(
                titleView,
                marginParams(0, 0, 0, 9)
        );

        TextView messageView = createText(
                message,
                14,
                COLOR_SECONDARY,
                false
        );

        messageView.setGravity(Gravity.CENTER);

        card.addView(messageView);

        eventContainer.addView(
                card,
                marginParams(0, 0, 0, 14)
        );

        Button retry = new Button(requireContext());

        retry.setText("Refresh");
        retry.setAllCaps(false);
        retry.setOnClickListener(v -> refreshData());

        eventContainer.addView(
                retry,
                marginParams(0, 0, 0, 0)
        );
    }

    private void showError(String message) {

        if (statusText != null) {

            statusText.setVisibility(View.VISIBLE);

            statusText.setText(
                    message == null
                            ? "Something went wrong."
                            : message
            );
        }

        Button retryButton = getRetryButton();

        if (retryButton != null) {
            retryButton.setVisibility(View.VISIBLE);
        }

        if (eventContainer != null) {
            eventContainer.removeAllViews();
        }
    }

    private void showMessage(String message) {

        if (!isAdded()) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private TextView createText(
            String text,
            float size,
            int color,
            boolean bold
    ) {

        TextView view = new TextView(requireContext());

        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);

        if (bold) {
            view.setTypeface(
                    null,
                    Typeface.BOLD
            );
        }

        return view;
    }

    private GradientDrawable createCardBackground() {

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(COLOR_CARD);

        background.setCornerRadius(dp(16));

        background.setStroke(
                dp(1),
                COLOR_BORDER
        );

        return background;
    }

    private LinearLayout.LayoutParams marginParams(
            int left,
            int top,
            int right,
            int bottom
    ) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                dp(left),
                dp(top),
                dp(right),
                dp(bottom)
        );

        return params;
    }

    private int dp(float value) {

        return (int) (
                value
                        * requireContext()
                        .getResources()
                        .getDisplayMetrics()
                        .density
                        + 0.5f
        );
    }

    private Date parseDate(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm",
                "yyyy-MM-dd HH:mm:ss"
        };

        for (String pattern : patterns) {

            SimpleDateFormat formatter =
                    new SimpleDateFormat(
                            pattern,
                            Locale.getDefault()
                    );

            formatter.setLenient(false);

            try {

                return formatter.parse(value);

            } catch (ParseException ignored) {
                // Try the next supported date format.
            }
        }

        return null;
    }

    private String formatDateTime(String value) {

        Date date = parseDate(value);

        if (date == null) {

            if (value == null || value.trim().isEmpty()) {
                return "Not specified";
            }

            return value.replace('T', ' ');
        }

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "dd MMM yyyy, hh:mm a",
                        Locale.getDefault()
                );

        return formatter.format(date);
    }

    @Override
    public void onDestroyView() {

        mainHandler.removeCallbacksAndMessages(null);

        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        root = null;
        eventContainer = null;
        progressBar = null;
        statusText = null;
        eventsButton = null;
        registrationsButton = null;

        super.onDestroyView();
    }
}