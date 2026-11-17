package com.bentleyplayz.notificationstudio;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {
    private static final String CHANNEL_ID = "notification_studio_simulations";
    private final List<ResolveInfo> launchableApps = new ArrayList<>();
    private ResolveInfo selectedApp;
    private TextView selectedLabel;
    private EditText titleInput;
    private EditText messageInput;
    private AppAdapter adapter;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        createChannel();
        buildScreen();
        loadLaunchableApps();
        requestNotificationPermissionIfNeeded();
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, float size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(16));
        root.setBackgroundColor(Color.rgb(20, 19, 29));

        TextView heading = text("Notification Studio", 26, Color.WHITE);
        heading.setTypeface(null, 1);
        root.addView(heading);

        TextView intro = text("Choose an installed app for its icon and optional launch action. Notifications are clearly marked as simulated and are sent by Notification Studio.", 14, Color.LTGRAY);
        intro.setPadding(0, dp(8), 0, dp(14));
        root.addView(intro);

        TextView pickerTitle = text("1. SELECT AN INSTALLED APP", 12, Color.rgb(190, 175, 255));
        pickerTitle.setTypeface(null, 1);
        root.addView(pickerTitle);

        selectedLabel = text("Selected: none", 15, Color.WHITE);
        selectedLabel.setPadding(0, dp(8), 0, dp(8));
        root.addView(selectedLabel);

        ListView appList = new ListView(this);
        appList.setBackgroundColor(Color.rgb(37, 34, 49));
        adapter = new AppAdapter();
        appList.setAdapter(adapter);
        root.addView(appList, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(220)));

        appList.setOnItemClickListener((parent, view, position, id) -> {
            selectedApp = launchableApps.get(position);
            selectedLabel.setText("Selected: " + selectedApp.loadLabel(getPackageManager()));
            adapter.notifyDataSetChanged();
        });

        TextView formTitle = text("2. NOTIFICATION CONTENT", 12, Color.rgb(190, 175, 255));
        formTitle.setTypeface(null, 1);
        formTitle.setPadding(0, dp(16), 0, dp(8));
        root.addView(formTitle);

        titleInput = new EditText(this);
        titleInput.setSingleLine(true);
        titleInput.setText("A sample notification");
        titleInput.setHint("Notification title");
        titleInput.setTextColor(Color.WHITE);
        titleInput.setHintTextColor(Color.GRAY);
        titleInput.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(170, 150, 230)));
        root.addView(titleInput);

        messageInput = new EditText(this);
        messageInput.setText("This is a test created with Notification Studio.");
        messageInput.setHint("Notification message");
        messageInput.setTextColor(Color.WHITE);
        messageInput.setHintTextColor(Color.GRAY);
        messageInput.setMinLines(2);
        messageInput.setGravity(Gravity.TOP);
        messageInput.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(170, 150, 230)));
        root.addView(messageInput);

        Button send = new Button(this);
        send.setText("SEND SIMULATED NOTIFICATION");
        root.addView(send);
        send.setOnClickListener(v -> sendSimulation());

        TextView footer = text("Prototype 0.1.0 • App discovery test • No app impersonation", 11, Color.LTGRAY);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(10), 0, 0);
        root.addView(footer);

        setContentView(root);
    }

    private void loadLaunchableApps() {
        Intent query = new Intent(Intent.ACTION_MAIN);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> found = getPackageManager().queryIntentActivities(query, 0);
        launchableApps.clear();
        for (ResolveInfo info : found) {
            if (info.activityInfo != null &&
                !getPackageName().equals(info.activityInfo.packageName)) {
                launchableApps.add(info);
            }
        }
        Collections.sort(launchableApps, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo a, ResolveInfo b) {
                return a.loadLabel(getPackageManager()).toString()
                    .compareToIgnoreCase(b.loadLabel(getPackageManager()).toString());
            }
        });
        adapter.notifyDataSetChanged();
        if (launchableApps.isEmpty()) {
            selectedLabel.setText("No launchable apps were returned by Android.");
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Simulated notifications", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Clearly labeled test notifications created by Notification Studio.");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 41);
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        int width = Math.max(1, drawable.getIntrinsicWidth());
        int height = Math.max(1, drawable.getIntrinsicHeight());
        width = Math.min(width, dp(128));
        height = Math.min(height, dp(128));
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    private void sendSimulation() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestNotificationPermissionIfNeeded();
            Toast.makeText(this, "Allow notifications, then tap send again.", Toast.LENGTH_LONG).show();
            return;
        }

        String title = titleInput.getText().toString().trim();
        String message = messageInput.getText().toString().trim();
        if (title.isEmpty()) title = "Test notification";
        if (message.isEmpty()) message = "Created with Notification Studio.";

        Intent tapIntent = new Intent(this, MainActivity.class);
        String destinationName = "Notification Studio";
        if (selectedApp != null && selectedApp.activityInfo != null) {
            Intent launch = getPackageManager().getLaunchIntentForPackage(selectedApp.activityInfo.packageName);
            if (launch != null) {
                tapIntent = launch;
                destinationName = selectedApp.loadLabel(getPackageManager()).toString();
            }
        }

        PendingIntent contentIntent = PendingIntent.getActivity(
            this, 100, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
            ? new Notification.Builder(this, CHANNEL_ID)
            : new Notification.Builder(this);

        builder.setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("[SIMULATED] " + title)
            .setContentText(message)
            .setStyle(new Notification.BigTextStyle().bigText(message))
            .setSubText("SIMULATED • Sent by Notification Studio")
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_MESSAGE);

        if (selectedApp != null) {
            builder.setLargeIcon(drawableToBitmap(selectedApp.loadIcon(getPackageManager())));
        }

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify((int) (System.currentTimeMillis() & 0x7fffffff), builder.build());
        Toast.makeText(this, "Simulation sent. Tap opens " + destinationName + ".", Toast.LENGTH_LONG).show();
    }

    private class AppAdapter extends BaseAdapter {
        @Override public int getCount() { return launchableApps.size(); }
        @Override public Object getItem(int position) { return launchableApps.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout row;
            if (convertView instanceof LinearLayout) {
                row = (LinearLayout) convertView;
            } else {
                row = new LinearLayout(MainActivity.this);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(10), dp(6), dp(10), dp(6));
                ImageView icon = new ImageView(MainActivity.this);
                row.addView(icon, new LinearLayout.LayoutParams(dp(40), dp(40)));
                TextView label = text("", 15, Color.WHITE);
                LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                labelParams.leftMargin = dp(12);
                row.addView(label, labelParams);
            }
            ResolveInfo info = launchableApps.get(position);
            ImageView iconView = (ImageView) row.getChildAt(0);
            TextView labelView = (TextView) row.getChildAt(1);
            iconView.setImageDrawable(info.loadIcon(getPackageManager()));
            labelView.setText(info.loadLabel(getPackageManager()).toString());
            row.setBackgroundColor(info == selectedApp ? Color.rgb(75, 61, 105) : Color.TRANSPARENT);
            return row;
        }
    }
}
