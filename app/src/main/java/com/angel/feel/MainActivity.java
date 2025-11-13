package com.angel.feel;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public static final String CHANNEL_ID = "cooldown_channel";

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private boolean isAutoBrightnessEnabled = true;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> { /* Handle permission grant/denial if needed */ }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        createNotificationChannel();
        requestNotificationPermission();

        if (savedInstanceState == null) {
            navigateTo(new SplashFragment(), false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public void navigateTo(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.setCustomAnimations(
                android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out
        );

        fragmentTransaction.replace(R.id.fragment_container, fragment);

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.toString());
        }

        fragmentTransaction.commit();
    }

    public void setWindowBrightness(float brightness) {
        isAutoBrightnessEnabled = (brightness < 0);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = brightness;
        getWindow().setAttributes(layoutParams);
    }

    public void setRisingBrightness() {
        isAutoBrightnessEnabled = false;
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        if (layoutParams.screenBrightness < 0.7f) {
            layoutParams.screenBrightness = 0.7f;
            getWindow().setAttributes(layoutParams);
        }
    }

    public void setFallingBrightness() {
        isAutoBrightnessEnabled = false;
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        if (layoutParams.screenBrightness > 0.3f || layoutParams.screenBrightness < 0) {
            layoutParams.screenBrightness = 0.3f;
            getWindow().setAttributes(layoutParams);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT && isAutoBrightnessEnabled) {
            float lux = event.values[0];
            float brightness = getBrightnessForLux(lux);
            setWindowBrightness(brightness);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }

    private float getBrightnessForLux(float lux) {
        if (lux < 50) return 0.1f;    // Very dark
        if (lux < 200) return 0.3f;   // Dim indoor light
        if (lux < 1000) return 0.6f;  // Normal indoor light
        if (lux < 5000) return 0.8f;  // Bright indoor or cloudy outdoor
        return 1.0f;                  // Direct sunlight
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Cooldowns";
            String description = "Notifications for when a category is available again";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
