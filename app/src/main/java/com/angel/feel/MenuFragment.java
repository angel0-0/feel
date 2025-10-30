package com.angel.feel;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import java.util.concurrent.TimeUnit;

public class MenuFragment extends Fragment {

    private static final String PREFS_NAME = "FeelAppPrefs";
    private static final String KEY_FALLING_UNLOCK_TIME = "falling_unlock_time";
    private static final String KEY_RISING_UNLOCK_TIME = "rising_unlock_time";
    private static final long BLOCK_DURATION_MS = 3000; // 60 seconds (para fines de tests son 3 segundos)

    private Button fallingButton;
    private Button risingButton;
    private TextView fallingCountdownText;
    private TextView risingCountdownText;

    private CountDownTimer fallingTimer;
    private CountDownTimer risingTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fallingButton = view.findViewById(R.id.falling_button);
        risingButton = view.findViewById(R.id.rising_button);
        fallingCountdownText = view.findViewById(R.id.falling_countdown_text);
        risingCountdownText = view.findViewById(R.id.rising_countdown_text);

        Typeface anotherTagFont = ResourcesCompat.getFont(requireContext(), R.font.another_tag);
        fallingCountdownText.setTypeface(anotherTagFont);
        risingCountdownText.setTypeface(anotherTagFont);

        view.findViewById(R.id.you_button).setOnClickListener(v -> openPhraseFragment("you", R.color.eng_violet, false));

        fallingButton.setOnClickListener(v -> {
            openPhraseFragment("falling", R.color.ultraviolet_light, true);
            saveUnlockTimestamp(KEY_FALLING_UNLOCK_TIME);
        });

        risingButton.setOnClickListener(v -> {
            openPhraseFragment("rising", R.color.mn_blue, true);
            saveUnlockTimestamp(KEY_RISING_UNLOCK_TIME);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateButtonStates();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (fallingTimer != null) {
            fallingTimer.cancel();
        }
        if (risingTimer != null) {
            risingTimer.cancel();
        }
    }

    private void updateButtonStates() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        long fallingUnlockTime = prefs.getLong(KEY_FALLING_UNLOCK_TIME, 0);
        long risingUnlockTime = prefs.getLong(KEY_RISING_UNLOCK_TIME, 0);

        long currentTime = System.currentTimeMillis();

        checkAndStartTimer(fallingButton, fallingCountdownText, fallingUnlockTime - currentTime, KEY_FALLING_UNLOCK_TIME);
        checkAndStartTimer(risingButton, risingCountdownText, risingUnlockTime - currentTime, KEY_RISING_UNLOCK_TIME);
    }

    private void checkAndStartTimer(Button button, TextView countdownText, long millisRemaining, String key) {
        if (millisRemaining > 0) {
            startTimer(button, countdownText, millisRemaining, key);
        } else {
            enableButton(button, countdownText);
        }
    }

    private void startTimer(Button button, TextView countdownText, long millisRemaining, String key) {
        button.setEnabled(false);
        button.setAlpha(0.5f);
        countdownText.setVisibility(View.VISIBLE);

        CountDownTimer timer = new CountDownTimer(millisRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdownText.setText(String.format("%ds", TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)));
            }

            @Override
            public void onFinish() {
                enableButton(button, countdownText);
                clearUnlockTimestamp(key);
            }
        };

        if (key.equals(KEY_FALLING_UNLOCK_TIME)) {
            fallingTimer = timer;
        } else {
            risingTimer = timer;
        }
        timer.start();
    }

    private void enableButton(Button button, TextView countdownText) {
        button.setEnabled(true);
        button.setAlpha(1.0f);
        countdownText.setVisibility(View.GONE);
    }

    private void saveUnlockTimestamp(String key) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, System.currentTimeMillis() + BLOCK_DURATION_MS);
        editor.apply();
    }

    private void clearUnlockTimestamp(String key) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(key).apply();
    }

    private void openPhraseFragment(String category, int colorResId, boolean shouldClose) {
        PhraseFragment phraseFragment = PhraseFragment.newInstance(category, colorResId);

        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                )
                .replace(R.id.fragment_container, phraseFragment)
                .addToBackStack(null)
                .commit();
    }
}
