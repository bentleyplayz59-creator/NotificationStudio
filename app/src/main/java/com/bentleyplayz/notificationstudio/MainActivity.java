package com.bentleyplayz.notificationstudio;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {
    private static final int PERMISSION_REQUEST = 41;
    private static final int BG = Color.rgb(13, 18, 32);
    private static final int CARD = Color.rgb(25, 33, 52);
    private static final int ACCENT = Color.rgb(111, 139, 255);
    private static final int MUTED = Color.rgb(166, 179, 202);
    private final List<ResolveInfo> apps = new ArrayList<>();
    private ResolveInfo selectedApp;
    private TextView selectedLabel;
    private EditText titleInput, messageInput, actionOneLabel, actionOneUrl, actionTwoLabel, actionTwoUrl;
    private AppAdapter adapter;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        buildScreen();
        loadApps();
        requestNotificationPermission();
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }

    private TextView label(String value, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(null, 1);
        return t;
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(15), dp(16), dp(15));
        c.setBackground(shape(CARD, 18));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(14);
        return c;
    }

    private void addField(LinearLayout parent, String hint, EditText[] out, int index, boolean multiline) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextColor(Color.WHITE);
        e.setHintTextColor(MUTED);
        e.setTextSize(15);
        e.setPadding(dp(13), dp(10), dp(13), dp(10));
        e.setBackground(shape(Color.rgb(35, 45, 68), 12));
        if (multiline) {
            e.setMinLines(3);
            e.setGravity(Gravity.TOP);
        } else {
            e.setSingleLine(true);
        }
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(9);
        parent.addView(e, p);
        out[index] = e;
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(22), dp(18), dp(24));
        scroll.addView(root);

        TextView eyebrow = label("BENTLEY PLAYZ  /  ANDROID TOOL", 10, ACCENT, true);
        root.addView(eyebrow);
        TextView heading = label("Notification Studio", 29, Color.WHITE, true);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
        hp.topMargin = dp(5);
        root.addView(heading, hp);
        TextView intro = label("Build and schedule your own test notifications.", 14, MUTED, false);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(-1, -2);
        ip.topMargin = dp(5); ip.bottomMargin = dp(18);
        root.addView(intro, ip);

        LinearLayout appCard = card();
        appCard.addView(label("01  CHOOSE AN APP", 12, ACCENT, true));
        selectedLabel = label("No app selected", 14, Color.WHITE, true);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(-1, -2);
        slp.topMargin = dp(8);
        appCard.addView(selectedLabel, slp);
        ListView list = new ListView(this);
        list.setDivider(null);
        list.setSelector(android.R.color.transparent);
        list.setBackgroundColor(Color.TRANSPARENT);
        adapter = new AppAdapter();
        list.setAdapter(adapter);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(320));
        lp.topMargin = dp(8);
        appCard.addView(list, lp);
        list.setOnTouchListener((view, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });
        list.setOnItemClickListener((parent, view, position, id) -> {
            selectedApp = apps.get(position);
            selectedLabel.setText("Selected: " + selectedApp.loadLabel(getPackageManager()));
            adapter.notifyDataSetChanged();
        });
        root.addView(appCard);

        LinearLayout contentCard = card();
        contentCard.addView(label("02  NOTIFICATION CONTENT", 12, ACCENT, true));
        EditText[] fields = new EditText[2];
        addField(contentCard, "Notification title", fields, 0, false);
        titleInput = fields[0];
        titleInput.setText("A sample notification");
        addField(contentCard, "Notification message", fields, 1, true);
        messageInput = fields[1];
        messageInput.setText("This is a test notification created with Notification Studio.");
        TextView note = label("Notifications are visibly marked as SIMULATED and are sent by Notification Studio.", 11, MUTED, false);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1, -2);
        np.topMargin = dp(9);
        contentCard.addView(note, np);
        root.addView(contentCard);

        LinearLayout actionsCard = card();
        actionsCard.addView(label("03  OPTIONAL ACTION BUTTONS", 12, ACCENT, true));
        actionsCard.addView(label("Each button opens the web address you enter.", 12, MUTED, false));
        EditText[] actionFields = new EditText[4];
        addField(actionsCard, "Button 1 label (optional)", actionFields, 0, false);
        addField(actionsCard, "Button 1 URL (https://...)", actionFields, 1, false);
        addField(actionsCard, "Button 2 label (optional)", actionFields, 2, false);
        addField(actionsCard, "Button 2 URL (https://...)", actionFields, 3, false);
        actionOneLabel = actionFields[0]; actionOneUrl = actionFields[1];
        actionTwoLabel = actionFields[2]; actionTwoUrl = actionFields[3];
        root.addView(actionsCard);

        LinearLayout scheduleCard = card();
        scheduleCard.addView(label("04  SEND OR SCHEDULE", 12, ACCENT, true));
        Button send = makeButton("SEND NOW", ACCENT, Color.WHITE);
        Button schedule = makeButton("CHOOSE DATE & TIME", Color.rgb(49, 65, 96), Color.WHITE);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, dp(50));
        bp.topMargin = dp(12);
        scheduleCard.addView(send, bp);
        LinearLayout.LayoutParams bp2 = new LinearLayout.LayoutParams(-1, dp(50));
        bp2.topMargin = dp(9);
        scheduleCard.addView(schedule, bp2);
        send.setOnClickListener(v -> sendNow());
        schedule.setOnClickListener(v -> chooseSchedule());
        root.addView(scheduleCard);

        TextView footer = label("SIMULATION TOOL  •  NOT AFFILIATED WITH SELECTED APPS", 10, MUTED, true);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer);
        setContentView(scroll);
    }

    private Button makeButton(String text, int background, int foreground) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(13);
        b.setTextColor(foreground);
        b.setAllCaps(false);
        b.setBackground(shape(background, 14));
        return b;
    }

    private void loadApps() {
        Intent query = new Intent(Intent.ACTION_MAIN);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> found = getPackageManager().queryIntentActivities(query, 0);
        apps.clear();
        for (ResolveInfo info : found) {
            if (info.activityInfo != null && !getPackageName().equals(info.activityInfo.packageName)) apps.add(info);
        }
        Collections.sort(apps, new Comparator<ResolveInfo>() {
            @Override public int compare(ResolveInfo a, ResolveInfo b) {
                return a.loadLabel(getPackageManager()).toString().compareToIgnoreCase(b.loadLabel(getPackageManager()).toString());
            }
        });
        adapter.notifyDataSetChanged();
        if (apps.isEmpty()) selectedLabel.setText("Android returned no launchable apps.");
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST);
        }
    }

    private Intent makeNotificationData() {
        String title = titleInput.getText().toString().trim();
        String message = messageInput.getText().toString().trim();
        if (title.isEmpty()) title = "Test notification";
        if (message.isEmpty()) message = "Created with Notification Studio.";
        Intent data = new Intent(this, NotificationReceiver.class);
        data.putExtra("title", title);
        data.putExtra("message", message);
        if (selectedApp != null && selectedApp.activityInfo != null) {
            data.putExtra("package", selectedApp.activityInfo.packageName);
            data.putExtra("appLabel", selectedApp.loadLabel(getPackageManager()).toString());
        }
        data.putExtra("action1Label", actionOneLabel.getText().toString().trim());
        data.putExtra("action1Url", actionOneUrl.getText().toString().trim());
        data.putExtra("action2Label", actionTwoLabel.getText().toString().trim());
        data.putExtra("action2Url", actionTwoUrl.getText().toString().trim());
        return data;
    }

    private void sendNow() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestNotificationPermission();
            Toast.makeText(this, "Allow notifications, then tap Send Now again.", Toast.LENGTH_LONG).show();
            return;
        }
        NotificationReceiver.show(this, makeNotificationData(), (int)(System.currentTimeMillis() & 0x7fffffff));
        Toast.makeText(this, "Simulated notification sent.", Toast.LENGTH_SHORT).show();
    }

    private void chooseSchedule() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog date = new DatePickerDialog(this, (picker, year, month, day) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(year, month, day);
            new TimePickerDialog(this, (timePicker, hour, minute) -> {
                chosen.set(Calendar.HOUR_OF_DAY, hour);
                chosen.set(Calendar.MINUTE, minute);
                chosen.set(Calendar.SECOND, 0);
                chosen.set(Calendar.MILLISECOND, 0);
                if (chosen.getTimeInMillis() <= System.currentTimeMillis()) {
                    Toast.makeText(this, "Choose a future date and time.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestNotificationPermission();
                    Toast.makeText(this, "Allow notifications before scheduling.", Toast.LENGTH_LONG).show();
                    return;
                }
                int id = (int)(System.currentTimeMillis() & 0x7fffffff);
                Intent data = makeNotificationData();
                data.putExtra("notificationId", id);
                PendingIntent pending = PendingIntent.getBroadcast(this, id, data,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                AlarmManager alarms = (AlarmManager)getSystemService(ALARM_SERVICE);
                if (alarms != null) {
                    alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, chosen.getTimeInMillis(), pending);
                    Toast.makeText(this, "Scheduled for " + android.text.format.DateFormat.format("MMM d, h:mm a", chosen), Toast.LENGTH_LONG).show();
                }
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        date.getDatePicker().setMinDate(System.currentTimeMillis());
        date.show();
    }

    private class AppAdapter extends BaseAdapter {
        @Override public int getCount() { return apps.size(); }
        @Override public Object getItem(int position) { return apps.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout row;
            if (convertView instanceof LinearLayout) row = (LinearLayout)convertView;
            else {
                row = new LinearLayout(MainActivity.this);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(9), dp(7), dp(9), dp(7));
                ImageView icon = new ImageView(MainActivity.this);
                row.addView(icon, new LinearLayout.LayoutParams(dp(38), dp(38)));
                TextView name = label("", 14, Color.WHITE, false);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1);
                p.leftMargin = dp(12);
                row.addView(name, p);
                TextView tick = label("✓", 16, ACCENT, true);
                row.addView(tick);
            }
            ResolveInfo info = apps.get(position);
            ((ImageView)row.getChildAt(0)).setImageDrawable(info.loadIcon(getPackageManager()));
            ((TextView)row.getChildAt(1)).setText(info.loadLabel(getPackageManager()).toString());
            row.getChildAt(2).setVisibility(info == selectedApp ? View.VISIBLE : View.INVISIBLE);
            row.setBackground(info == selectedApp ? shape(Color.rgb(48, 61, 91), 12) : shape(Color.TRANSPARENT, 12));
            return row;
        }
    }
}
