package com.angel.feel;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class CooldownManager {

    private static final String PREFS_NAME = "FeelAppCooldowns";

    // Keys for SharedPreferences
    private static final String KEY_COOLDOWN_END_TIME_PREFIX = "cooldown_end_time_";
    private static final String KEY_LAST_CLICK_TIME_PREFIX = "last_click_time_";
    private static final String KEY_COOLDOWN_LEVEL_PREFIX = "cooldown_level_";

    // Cooldown levels in milliseconds
    private static final long[] COOLDOWN_LEVELS_MS = {
            TimeUnit.SECONDS.toMillis(5),
            TimeUnit.SECONDS.toMillis(10),
            TimeUnit.SECONDS.toMillis(30),
            TimeUnit.MINUTES.toMillis(1),
            TimeUnit.MINUTES.toMillis(2),
            TimeUnit.MINUTES.toMillis(3),
            TimeUnit.MINUTES.toMillis(5),
            TimeUnit.MINUTES.toMillis(10),
            TimeUnit.MINUTES.toMillis(30)
    };

    public static void startCooldown(Context context, String category) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        long currentTime = System.currentTimeMillis();
        long lastClickTime = prefs.getLong(KEY_LAST_CLICK_TIME_PREFIX + category, 0);
        int currentLevel = prefs.getInt(KEY_COOLDOWN_LEVEL_PREFIX + category, 0);

        long timeSinceLastClick = (lastClickTime == 0) ? Long.MAX_VALUE : currentTime - lastClickTime;

        int newLevel;

        if (timeSinceLastClick > TimeUnit.MINUTES.toMillis(51)) {
            // Reset to minimum if clicked after 51 minutes
            newLevel = 0;
        } else if (timeSinceLastClick >= TimeUnit.MINUTES.toMillis(50)) {
            // Apply maximum penalty if clicked between 50 and 51 minutes
            newLevel = COOLDOWN_LEVELS_MS.length - 1;
        } else if (timeSinceLastClick < TimeUnit.MINUTES.toMillis(3)) {
            // Increase level if clicked within 3 minutes
            newLevel = Math.min(currentLevel + 1, COOLDOWN_LEVELS_MS.length - 1);
        } else {
            // Keep current level if clicked between 3 and 50 minutes
            newLevel = currentLevel;
        }

        long cooldownDurationMs = COOLDOWN_LEVELS_MS[newLevel];
        long cooldownEndTime = currentTime + cooldownDurationMs;

        // Save all the new states
        editor.putLong(KEY_COOLDOWN_END_TIME_PREFIX + category, cooldownEndTime);
        editor.putLong(KEY_LAST_CLICK_TIME_PREFIX + category, currentTime);
        editor.putInt(KEY_COOLDOWN_LEVEL_PREFIX + category, newLevel);
        editor.apply();

        // Schedule the notification with the new duration
        scheduleNotification(context, category, cooldownDurationMs);
    }

    public static long getRemainingCooldown(Context context, String category) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long cooldownEndTime = prefs.getLong(KEY_COOLDOWN_END_TIME_PREFIX + category, 0);
        long currentTime = System.currentTimeMillis();
        return Math.max(0, cooldownEndTime - currentTime);
    }

    private static void scheduleNotification(Context context, String category, long delayInMs) {
        Data inputData = new Data.Builder()
                .putString(NotificationWorker.KEY_CATEGORY_NAME, category)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(delayInMs, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                "cooldown_" + category,
                ExistingWorkPolicy.REPLACE,
                workRequest
        );
    }

    public static void cancelNotification(Context context, String category) {
        WorkManager.getInstance(context).cancelUniqueWork("cooldown_" + category);
    }
}
