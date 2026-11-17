package com.bentleyplayz.notificationstudio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL = "notification_studio_messages";

    @Override public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("notificationId", (int)(System.currentTimeMillis() & 0x7fffffff));
        show(context, intent, id);
    }

    public static void show(Context context, Intent data, int id) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL, "Messages", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications created in Notification Studio.");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        String title = data.getStringExtra("title");
        String message = data.getStringExtra("message");
        if (title == null || title.trim().isEmpty()) title = "Test notification";
        if (message == null || message.trim().isEmpty()) message = "Created with Notification Studio.";

        Intent tap = launchIntent(context, data.getStringExtra("package"));
        PendingIntent content = PendingIntent.getActivity(context, id, tap,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
            ? new Notification.Builder(context, CHANNEL) : new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(new Notification.BigTextStyle().bigText(message))
            .setContentIntent(content)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_MESSAGE);

        String packageName = data.getStringExtra("package");
        if (packageName != null) {
            try {
                Drawable icon = context.getPackageManager().getApplicationIcon(packageName);
                builder.setLargeIcon(toBitmap(context, icon));
            } catch (PackageManager.NameNotFoundException ignored) { }
        }

        addAction(context, builder, id + 1, data.getStringExtra("action1Label"), data.getStringExtra("action1Url"));
        addAction(context, builder, id + 2, data.getStringExtra("action2Label"), data.getStringExtra("action2Url"));

        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(id, builder.build());
    }

    private static Intent launchIntent(Context context, String packageName) {
        Intent launch = packageName == null ? null : context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launch == null) launch = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (launch == null) launch = new Intent(context, MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return launch;
    }

    private static void addAction(Context context, Notification.Builder builder, int requestCode, String label, String url) {
        if (label == null || label.trim().isEmpty() || url == null ||
            !(url.startsWith("https://") || url.startsWith("http://"))) return;
        Intent view = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        PendingIntent action = PendingIntent.getActivity(context, requestCode, view,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(new Notification.Action.Builder(
            Icon.createWithResource(context, R.drawable.ic_notification), label, action).build());
    }

    private static Bitmap toBitmap(Context context, Drawable drawable) {
        int size = (int)(context.getResources().getDisplayMetrics().density * 64);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, size, size);
        drawable.draw(canvas);
        return bitmap;
    }
}
