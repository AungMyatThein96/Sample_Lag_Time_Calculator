package com.example.lagtimecalculator;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.provider.Settings;

public class MainActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.setWebViewClient(new WebViewClient());

        // JavaScript bridge for the sample alarm.
        webView.addJavascriptInterface(
                new AndroidAlarmBridge(this),
                "AndroidAlarm"
        );

        // JavaScript bridge for Android Print / Save PDF.
        webView.addJavascriptInterface(
                new AndroidPrintBridge(this, webView),
                "AndroidPrint"
        );

        webView.loadUrl(
                "file:///android_asset/lag_time_calculator.html"
        );

        // Android 13+ notification permission.
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    1001
            );
        }
    }

    public static class AndroidAlarmBridge {

        private final Context context;

        AndroidAlarmBridge(Context context) {
            this.context = context.getApplicationContext();
        }

        @JavascriptInterface
        public void setAlarm(long triggerAtMillis) {

            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(
                            Context.ALARM_SERVICE
                    );

            Intent intent =
                    new Intent(context, SampleAlarmReceiver.class);

            PendingIntent pi =
                    PendingIntent.getBroadcast(
                            context,
                            1001,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT |
                                    PendingIntent.FLAG_IMMUTABLE
                    );

            // Android 12+ requires the exact-alarm permission.
            if (Build.VERSION.SDK_INT >= 31 &&
                    !alarmManager.canScheduleExactAlarms()) {

                Intent settingsIntent =
                        new Intent(
                                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        );

                settingsIntent.setData(
                        Uri.parse("package:" + context.getPackageName())
                );

                settingsIntent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );

                context.startActivity(settingsIntent);
                return;
            }

            if (Build.VERSION.SDK_INT >= 23) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pi
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pi
                );
            }
        }

        @JavascriptInterface
        public void cancelAlarm() {

            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(
                            Context.ALARM_SERVICE
                    );

            Intent intent =
                    new Intent(context, SampleAlarmReceiver.class);

            PendingIntent pi =
                    PendingIntent.getBroadcast(
                            context,
                            1001,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT |
                                    PendingIntent.FLAG_IMMUTABLE
                    );

            alarmManager.cancel(pi);
        }
    }

    public static class AndroidPrintBridge {

        private final Activity activity;
        private final WebView webView;

        AndroidPrintBridge(Activity activity, WebView webView) {
            this.activity = activity;
            this.webView = webView;
        }

        @JavascriptInterface
        public void printPage() {

            // Run Android PrintManager on the UI thread.
            activity.runOnUiThread(() -> {

                PrintManager printManager =
                        (PrintManager) activity.getSystemService(
                                Context.PRINT_SERVICE
                        );

                if (printManager == null) {
                    return;
                }

                String jobName =
                        "Well Site Lag Time Calculator";

                printManager.print(
                        jobName,
                        webView.createPrintDocumentAdapter(jobName),
                        new PrintAttributes.Builder()
                                .setMediaSize(
                                        PrintAttributes.MediaSize.ISO_A4
                                )
                                .setMinMargins(
                                        PrintAttributes.Margins.NO_MARGINS
                                )
                                .build()
                );
            });
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
