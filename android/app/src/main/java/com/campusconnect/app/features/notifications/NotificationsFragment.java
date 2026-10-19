package com.campusconnect.app.features.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.campusconnect.app.R;
import com.campusconnect.app.StudentDashboardActivity;
import com.campusconnect.app.features.notices.NoticesFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationsFragment extends Fragment {


private static final String BASE_URL = "http://127.0.0.1:8080";
private static final String PREFS_NAME = "campusconnect_session";
private static final String TOKEN_KEY = "jwt_token";

private static final String STATE_SHOWING_NOTICES =
        "notifications_showing_notices";

private static final int CONTENT_CONTAINER_ID = 0x00CC0011;

private final Handler mainHandler =
        new Handler(Looper.getMainLooper());

private ExecutorService executor;

private FrameLayout contentContainer;
private LinearLayout notificationContent;
private LinearLayout notificationList;

private Button notificationsTab;
private Button noticesTab;
private Button allFilterButton;
private Button unreadFilterButton;
private Button markAllButton;
private Button retryButton;

private TextView unreadSummary;
private TextView statusText;
private ProgressBar progressBar;

private final List<NotificationItem> notifications =
        new ArrayList<>();

private boolean unreadOnly = false;
private boolean showingNotices = false;
private boolean loading = false;
private boolean requestInProgress = false;

public NotificationsFragment() {
    // Required empty constructor.
}

@Nullable
@Override
public View onCreateView(
        @NonNull android.view.LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
) {
    if (executor == null || executor.isShutdown()) {
        executor = Executors.newSingleThreadExecutor();
    }

    Context context = requireContext();

    ScrollView page = new ScrollView(context);
    page.setFillViewport(true);
    page.setBackgroundColor(
            context.getColor(R.color.main_background)
    );

    LinearLayout root = new LinearLayout(context);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(dp(20), dp(20), dp(20), dp(28));

    page.addView(
            root,
            new ScrollView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
    );

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
    title.setText("Updates & Notices");
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
            "Stay informed about academics and campus announcements."
    );
    subtitle.setTextColor(
            context.getColor(R.color.secondary_text)
    );
    subtitle.setTextSize(14);
    subtitle.setLineSpacing(dp(3), 1f);

    LinearLayout.LayoutParams subtitleParams =
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

    subtitleParams.topMargin = dp(5);
    root.addView(subtitle, subtitleParams);

    LinearLayout tabs = new LinearLayout(context);
    tabs.setOrientation(LinearLayout.HORIZONTAL);

    LinearLayout.LayoutParams tabsParams =
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

    tabsParams.topMargin = dp(18);
    root.addView(tabs, tabsParams);

    notificationsTab = new Button(context);
    notificationsTab.setText("Notifications");
    notificationsTab.setAllCaps(false);
    notificationsTab.setOnClickListener(v -> showNotificationsTab());

    noticesTab = new Button(context);
    noticesTab.setText("Campus Notices");
    noticesTab.setAllCaps(false);
    noticesTab.setOnClickListener(v -> showNoticesTab());

    tabs.addView(
            notificationsTab,
            new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
            )
    );

    tabs.addView(
            noticesTab,
            new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
            )
    );

    contentContainer = new FrameLayout(context);
    contentContainer.setId(CONTENT_CONTAINER_ID);

    LinearLayout.LayoutParams contentParams =
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

    contentParams.topMargin = dp(8);
    root.addView(contentContainer, contentParams);

    notificationContent = buildNotificationContent(context);

    Fragment restoredChild =
            getChildFragmentManager().findFragmentById(
                    CONTENT_CONTAINER_ID
            );

    boolean restoreNotices =
            savedInstanceState != null
                    && savedInstanceState.getBoolean(
                            STATE_SHOWING_NOTICES,
                            false
                    );

    if (restoreNotices
            && restoredChild instanceof NoticesFragment) {

        showingNotices = true;
        updateTabButtons();

    } else {
        if (restoredChild != null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .remove(restoredChild)
                    .commitNow();
        }

        showNotificationsTab();
    }

    return page;
}

private LinearLayout buildNotificationContent(Context context) {
    LinearLayout content = new LinearLayout(context);
    content.setOrientation(LinearLayout.VERTICAL);

    LinearLayout summaryCard = new LinearLayout(context);
    summaryCard.setOrientation(LinearLayout.VERTICAL);
    summaryCard.setPadding(dp(18), dp(18), dp(18), dp(18));
    summaryCard.setBackgroundResource(R.drawable.bg_card);

    content.addView(
            summaryCard,
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
    );

    TextView label = new TextView(context);
    label.setText("YOUR INBOX");
    label.setTextColor(
            context.getColor(R.color.secondary_text)
    );
    label.setTextSize(11);
    label.setLetterSpacing(0.08f);
    label.setTypeface(null, Typeface.BOLD);
    summaryCard.addView(label);

    unreadSummary = new TextView(context);
    unreadSummary.setText("Loading unread count...");
    unreadSummary.setTextColor(
            context.getColor(R.color.primary_text)
    );
    unreadSummary.setTextSize(18);
    unreadSummary.setTypeface(null, Typeface.BOLD);

    LinearLayout.LayoutParams summaryParams =
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

    summaryParams.topMargin = dp(8);
    summaryCard.addView(unreadSummary, summaryParams);

    markAllButton = new Button(context);
    markAllButton.setText("Mark all as read");
    markAllButton.setAllCaps(false);
    markAllButton.setEnabled(false);
    markAllButton.setOnClickListener(v -> markAllAsRead());

    LinearLayout.LayoutParams markAllParams =
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

    markAllParams.topMargin = dp(8);
    summaryCard.addView(markAllButton, markAllParams);

    LinearLayout filters = new LinearLayout(context);
    filters.setOrientation(LinearLayout.HORIZONTAL);

    LinearLayout.LayoutParams filtersParams =
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

    filtersParams.topMargin = dp(10);
    content.addView(filters, filtersParams);

    allFilterButton = new Button(context);
    allFilterButton.setText("All");
    allFilterButton.setAllCaps(false);
    allFilterButton.setOnClickListener(v -> {
        unreadOnly = false;
        updateFilterButtons();
        renderNotifications();
    });

    unreadFilterButton = new Button(context);
    unreadFilterButton.setText("Unread");
    unreadFilterButton.setAllCaps(false);
    unreadFilterButton.setOnClickListener(v -> {
        unreadOnly = true;
        updateFilterButtons();
        renderNotifications();
    });

    filters.addView(
            allFilterButton,
            new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
            )
    );

    filters.addView(
            unreadFilterButton,
            new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
            )
    );

    progressBar = new ProgressBar(context);
    progressBar.setIndeterminate(true);

    LinearLayout.LayoutParams progressParams =
            new LinearLayout.LayoutParams(dp(36), dp(36));

    progressParams.gravity = Gravity.CENTER_HORIZONTAL;
    progressParams.topMargin = dp(20);
    content.addView(progressBar, progressParams);

    statusText = new TextView(context);
    statusText.setTextColor(
            context.getColor(R.color.secondary_text)
    );
    statusText.setTextSize(14);
    statusText.setGravity(Gravity.CENTER);
    statusText.setLineSpacing(dp(3), 1f);

    LinearLayout.LayoutParams statusParams =
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

    statusParams.topMargin = dp(10);
    content.addView(statusText, statusParams);

    retryButton = new Button(context);
    retryButton.setText("Retry");
    retryButton.setAllCaps(false);
    retryButton.setVisibility(View.GONE);
    retryButton.setOnClickListener(v -> loadNotifications());

    LinearLayout.LayoutParams retryParams =
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

    retryParams.gravity = Gravity.CENTER_HORIZONTAL;
    content.addView(retryButton, retryParams);

    notificationList = new LinearLayout(context);
    notificationList.setOrientation(LinearLayout.VERTICAL);

    LinearLayout.LayoutParams listParams =
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

    listParams.topMargin = dp(4);
    content.addView(notificationList, listParams);

    updateFilterButtons();

    return content;
}

private void showNotificationsTab() {
    if (!isAdded() || contentContainer == null) {
        return;
    }

    Fragment child =
            getChildFragmentManager().findFragmentById(
                    CONTENT_CONTAINER_ID
            );

    if (child != null) {
        getChildFragmentManager()
                .beginTransaction()
                .remove(child)
                .commitNow();
    }

    showingNotices = false;
    contentContainer.removeAllViews();

    if (notificationContent.getParent() instanceof ViewGroup) {
        ((ViewGroup) notificationContent.getParent())
                .removeView(notificationContent);
    }

    contentContainer.addView(
            notificationContent,
            new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
    );

    updateTabButtons();
    loadNotifications();
}

private void showNoticesTab() {
    if (!isAdded() || contentContainer == null || showingNotices) {
        return;
    }

    showingNotices = true;
    contentContainer.removeAllViews();
    updateTabButtons();

    getChildFragmentManager()
            .beginTransaction()
            .replace(
                    CONTENT_CONTAINER_ID,
                    new NoticesFragment()
            )
            .commit();
}

private void updateTabButtons() {
    if (notificationsTab != null) {
        notificationsTab.setEnabled(showingNotices);
    }

    if (noticesTab != null) {
        noticesTab.setEnabled(!showingNotices);
    }
}

@Override
public void onResume() {
    super.onResume();

    if (isAdded()
            && !showingNotices
            && getActivity() instanceof StudentDashboardActivity) {

        ((StudentDashboardActivity) getActivity())
                .refreshUnreadBadge();
    }
}

private void loadNotifications() {
    if (!isAdded()
            || showingNotices
            || loading
            || executor == null
            || executor.isShutdown()) {
        return;
    }

    SharedPreferences preferences =
            requireContext().getSharedPreferences(
                    PREFS_NAME,
                    Context.MODE_PRIVATE
            );

    String token = preferences.getString(TOKEN_KEY, null);

    if (token == null || token.trim().isEmpty()) {
        setLoadingState(
                false,
                "Your session has expired. Please sign in again.",
                false
        );
        return;
    }

    loading = true;
    setLoadingState(true, "Loading notifications...", false);

    executor.execute(() -> {
        try {
            JSONArray response = new JSONArray(
                    request(
                            "GET",
                            "/api/notifications/my",
                            token
                    )
            );

            List<NotificationItem> loaded = new ArrayList<>();

            for (int i = 0; i < response.length(); i++) {
                JSONObject object = response.optJSONObject(i);

                if (object == null) {
                    continue;
                }

                String id = object.optString("id", "");
                String title = object.optString(
                        "title",
                        "Notification"
                );
                String message = object.optString("message", "");
                String type = object.optString("type", "GENERAL");

                boolean read = object.has("isRead")
                        ? object.optBoolean("isRead", false)
                        : object.optBoolean("read", false);

                loaded.add(
                        new NotificationItem(
                                id,
                                title,
                                message,
                                type,
                                read,
                                object.optString("createdAt", "")
                        )
                );
            }

            long unreadCount = parseUnreadCount(
                    request(
                            "GET",
                            "/api/notifications/unread-count",
                            token
                    )
            );

            mainHandler.post(() -> {
                if (!isAdded() || showingNotices) {
                    return;
                }

                notifications.clear();
                notifications.addAll(loaded);

                updateUnreadSummary(unreadCount);
                setLoadingState(false, "", false);
                renderNotifications();

                if (getActivity()
                        instanceof StudentDashboardActivity) {
                    ((StudentDashboardActivity) getActivity())
                            .updateUnreadBadge(unreadCount);
                }
            });

        } catch (Exception exception) {
            mainHandler.post(() -> {
                if (!isAdded() || showingNotices) {
                    return;
                }

                setLoadingState(
                        false,
                        "Unable to load notifications. Check your connection and retry.",
                        true
                );
            });

        } finally {
            loading = false;
        }
    });
}

private void markAsRead(NotificationItem item) {
    if (item.read
            || item.id.isEmpty()
            || requestInProgress
            || executor == null
            || executor.isShutdown()) {
        return;
    }

    requestInProgress = true;

    executor.execute(() -> {
        try {
            request(
                    "PUT",
                    "/api/notifications/" + item.id + "/read",
                    getToken()
            );

            mainHandler.post(() -> {
                if (!isAdded()) {
                    return;
                }

                item.read = true;
                renderNotifications();

                if (getActivity()
                        instanceof StudentDashboardActivity) {
                    ((StudentDashboardActivity) getActivity())
                            .refreshUnreadBadge();
                }
            });

        } catch (Exception exception) {
            mainHandler.post(() -> {
                if (isAdded()) {
                    showToast("Unable to mark notification as read.");
                }
            });

        } finally {
            mainHandler.post(() -> requestInProgress = false);
        }
    });
}

private void markAllAsRead() {
    if (requestInProgress
            || getUnreadCountLocally() == 0
            || executor == null
            || executor.isShutdown()) {
        return;
    }

    requestInProgress = true;
    markAllButton.setEnabled(false);

    executor.execute(() -> {
        try {
            request(
                    "PUT",
                    "/api/notifications/read-all",
                    getToken()
            );

            mainHandler.post(() -> {
                if (!isAdded()) {
                    return;
                }

                for (NotificationItem item : notifications) {
                    item.read = true;
                }

                unreadOnly = false;
                updateFilterButtons();
                renderNotifications();
                updateUnreadSummary(0);

                if (getActivity()
                        instanceof StudentDashboardActivity) {
                    ((StudentDashboardActivity) getActivity())
                            .updateUnreadBadge(0);
                }

                showToast("All notifications marked as read.");
            });

        } catch (Exception exception) {
            mainHandler.post(() -> {
                if (isAdded()) {
                    showToast(
                            "Unable to mark all notifications as read."
                    );
                }
            });

        } finally {
            mainHandler.post(() -> {
                requestInProgress = false;

                if (isAdded() && markAllButton != null) {
                    markAllButton.setEnabled(
                            getUnreadCountLocally() > 0
                    );
                }
            });
        }
    });
}

private void renderNotifications() {
    if (!isAdded()
            || notificationList == null
            || showingNotices) {
        return;
    }

    notificationList.removeAllViews();

    List<NotificationItem> visible = new ArrayList<>();

    for (NotificationItem item : notifications) {
        if (!unreadOnly || !item.read) {
            visible.add(item);
        }
    }

    if (visible.isEmpty()) {
        TextView empty = new TextView(requireContext());

        empty.setText(
                unreadOnly
                        ? "You're all caught up. No unread notifications."
                        : "No notifications yet."
        );

        empty.setTextColor(
                requireContext().getColor(R.color.secondary_text)
        );
        empty.setTextSize(14);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(dp(16), dp(28), dp(16), dp(28));

        notificationList.addView(empty);
        return;
    }

    for (NotificationItem item : visible) {
        addNotificationCard(item);
    }
}

private void addNotificationCard(NotificationItem item) {
    Context context = requireContext();

    LinearLayout card = new LinearLayout(context);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setPadding(dp(16), dp(16), dp(16), dp(16));
    card.setBackgroundResource(R.drawable.bg_card);

    LinearLayout.LayoutParams cardParams =
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

    cardParams.topMargin = dp(12);
    notificationList.addView(card, cardParams);

    LinearLayout header = new LinearLayout(context);
    header.setOrientation(LinearLayout.HORIZONTAL);
    header.setGravity(Gravity.CENTER_VERTICAL);
    card.addView(header);

    TextView category = new TextView(context);
    category.setText(formatCategory(item.type));
    category.setTextColor(
            context.getColor(R.color.primary_accent)
    );
    category.setTextSize(11);
    category.setTypeface(null, Typeface.BOLD);

    header.addView(
            category,
            new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
            )
    );

    TextView readState = new TextView(context);
    readState.setText(item.read ? "READ" : "UNREAD");
    readState.setTextColor(
            context.getColor(
                    item.read
                            ? R.color.secondary_text
                            : R.color.primary_accent
            )
    );
    readState.setTextSize(10);
    readState.setTypeface(null, Typeface.BOLD);
    header.addView(readState);

    TextView title = new TextView(context);
    title.setText(item.title);
    title.setTextColor(
            context.getColor(R.color.primary_text)
    );
    title.setTextSize(16);
    title.setTypeface(null, Typeface.BOLD);

    LinearLayout.LayoutParams titleParams =
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

    titleParams.topMargin = dp(10);
    card.addView(title, titleParams);

    TextView message = new TextView(context);
    message.setText(item.message);
    message.setTextColor(
            context.getColor(R.color.secondary_text)
    );
    message.setTextSize(14);
    message.setLineSpacing(dp(3), 1f);

    LinearLayout.LayoutParams messageParams =
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

    messageParams.topMargin = dp(6);
    card.addView(message, messageParams);

    TextView timestamp = new TextView(context);
    timestamp.setText(formatTimestamp(item.createdAt));
    timestamp.setTextColor(
            context.getColor(R.color.secondary_text)
    );
    timestamp.setTextSize(11);

    LinearLayout.LayoutParams timestampParams =
            new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

    timestampParams.topMargin = dp(10);
    card.addView(timestamp, timestampParams);

    if (!item.read) {
        Button markReadButton = new Button(context);
        markReadButton.setText("Mark as read");
        markReadButton.setAllCaps(false);
        markReadButton.setOnClickListener(v -> markAsRead(item));

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        buttonParams.topMargin = dp(4);
        card.addView(markReadButton, buttonParams);
    }
}

private void updateUnreadSummary(long count) {
    if (unreadSummary != null) {
        unreadSummary.setText(
                count == 1
                        ? "1 unread notification"
                        : count + " unread notifications"
        );
    }

    if (markAllButton != null) {
        markAllButton.setEnabled(count > 0);
    }
}

private void updateFilterButtons() {
    if (allFilterButton != null) {
        allFilterButton.setEnabled(unreadOnly);
    }

    if (unreadFilterButton != null) {
        unreadFilterButton.setEnabled(!unreadOnly);
    }
}

private void setLoadingState(
        boolean isLoading,
        String message,
        boolean allowRetry
) {
    loading = isLoading;

    if (progressBar != null) {
        progressBar.setVisibility(
                isLoading ? View.VISIBLE : View.GONE
        );
    }

    if (statusText != null) {
        statusText.setText(message);
        statusText.setVisibility(
                message.isEmpty() ? View.GONE : View.VISIBLE
        );
    }

    if (retryButton != null) {
        retryButton.setVisibility(
                allowRetry ? View.VISIBLE : View.GONE
        );
    }

    if (isLoading && notificationList != null) {
        notificationList.removeAllViews();
    }
}

private long getUnreadCountLocally() {
    long count = 0;

    for (NotificationItem item : notifications) {
        if (!item.read) {
            count++;
        }
    }

    return count;
}

private long parseUnreadCount(String response)
        throws Exception {
    String value = response.trim();

    try {
        return Long.parseLong(value);
    } catch (NumberFormatException ignored) {
        JSONObject object = new JSONObject(value);

        if (object.has("unreadCount")) {
            return object.optLong("unreadCount", 0);
        }

        if (object.has("count")) {
            return object.optLong("count", 0);
        }

        throw new Exception("Unexpected unread-count response");
    }
}

private String getToken() {
    Context context = getContext();

    if (context == null) {
        return null;
    }

    SharedPreferences preferences =
            context.getSharedPreferences(
                    PREFS_NAME,
                    Context.MODE_PRIVATE
            );

    return preferences.getString(TOKEN_KEY, null);
}

private String request(
        String method,
        String path,
        String token
) throws Exception {
    if (token == null || token.trim().isEmpty()) {
        throw new Exception("Session token is missing");
    }

    HttpURLConnection connection = null;

    try {
        connection = (HttpURLConnection)
                new URL(BASE_URL + path).openConnection();

        connection.setRequestMethod(method);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);

        connection.setRequestProperty(
                "Authorization",
                "Bearer " + token
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        int status = connection.getResponseCode();

        InputStream stream =
                status >= 200 && status < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();

        StringBuilder response = new StringBuilder();

        if (stream != null) {
            try (
                    InputStream input = stream;
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    input,
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

        if (status < 200 || status >= 300) {
            throw new Exception(
                    "HTTP " + status + ": " + response
            );
        }

        return response.toString();

    } finally {
        if (connection != null) {
            connection.disconnect();
        }
    }
}

private String formatCategory(String type) {
    if (type == null || type.trim().isEmpty()) {
        return "General";
    }

    String[] words =
            type.toLowerCase(Locale.ROOT).split("_");

    StringBuilder result = new StringBuilder();

    for (String word : words) {
        if (result.length() > 0) {
            result.append(' ');
        }

        if (!word.isEmpty()) {
            result.append(
                    word.substring(0, 1).toUpperCase(Locale.ROOT)
            );

            if (word.length() > 1) {
                result.append(word.substring(1));
            }
        }
    }

    return result.toString();
}

private String formatTimestamp(String timestamp) {
    if (timestamp == null
            || timestamp.trim().isEmpty()
            || "null".equalsIgnoreCase(timestamp)) {
        return "Date unavailable";
    }

    try {
        String normalized = timestamp.replace('T', ' ');

        if (normalized.length() >= 19) {
            normalized = normalized.substring(0, 19);

            SimpleDateFormat inputFormat =
                    new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()
                    );

            inputFormat.setLenient(false);

            Date date = inputFormat.parse(normalized);

            if (date != null) {
                SimpleDateFormat outputFormat =
                        new SimpleDateFormat(
                                "dd MMM yyyy, hh:mm a",
                                Locale.getDefault()
                        );

                return outputFormat.format(date);
            }
        }
    } catch (Exception ignored) {
        // Use the original server timestamp if parsing fails.
    }

    return timestamp.replace('T', ' ');
}

private void showToast(String message) {
    Context context = getContext();

    if (context != null) {
        Toast.makeText(
                context,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}

private int dp(int value) {
    return Math.round(
            value * getResources()
                    .getDisplayMetrics()
                    .density
    );
}

@Override
public void onSaveInstanceState(@NonNull Bundle outState) {
    outState.putBoolean(
            STATE_SHOWING_NOTICES,
            showingNotices
    );

    super.onSaveInstanceState(outState);
}

@Override
public void onDestroyView() {
    if (executor != null) {
        executor.shutdownNow();
        executor = null;
    }

    contentContainer = null;
    notificationContent = null;
    notificationList = null;
    notificationsTab = null;
    noticesTab = null;
    allFilterButton = null;
    unreadFilterButton = null;
    markAllButton = null;
    retryButton = null;
    unreadSummary = null;
    statusText = null;
    progressBar = null;

    super.onDestroyView();
}

private static class NotificationItem {

    final String id;
    final String title;
    final String message;
    final String type;
    final String createdAt;

    boolean read;

    NotificationItem(
            String id,
            String title,
            String message,
            String type,
            boolean read,
            String createdAt
    ) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = read;
        this.createdAt = createdAt;
    }
}

}