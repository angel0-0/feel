package com.angel.feel;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Random;

public class SplashFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 800;

    private int[] backgroundColors;
    private final Random random = new Random();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        initializeColors();

        Button feelButton = view.findViewById(R.id.feel_button);
        FloatingActionButton addPhraseButton = view.findViewById(R.id.add_phrase_button);

        MainActivity mainActivity = (MainActivity) getActivity();

        if (mainActivity != null) {
            feelButton.setOnClickListener(v -> mainActivity.navigateTo(new MenuFragment(), true));
            addPhraseButton.setOnClickListener(v -> mainActivity.navigateTo(new AddPhraseFragment(), true));
        }
    }

    private void initializeColors() {
        backgroundColors = new int[]{
                R.color.raisin,
                R.color.eng_violet,
                R.color.ultraviolet,
                R.color.ultraviolet_light,
                R.color.yi_blue,
                R.color.mn_blue,
                R.color.grayblue,
                R.color.onyx,
                R.color.indigo,
                R.color.true_blue
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    changeBackgroundColor();
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this implementation
    }

    private void changeBackgroundColor() {
        if (getView() != null && getContext() != null) {
            int randomColorId = backgroundColors[random.nextInt(backgroundColors.length)];
            getView().setBackgroundColor(ContextCompat.getColor(getContext(), randomColorId));
        }
    }
}
