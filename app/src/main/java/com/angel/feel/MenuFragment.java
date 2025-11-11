package com.angel.feel;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

    private Button fallingButton;
    private Button risingButton;
    private TextView fallingCountdownText;
    private TextView risingCountdownText;

    private final Handler countdownHandler = new Handler(Looper.getMainLooper());
    private Runnable countdownRunnable;

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

        MainActivity mainActivity = (MainActivity) getActivity();

        view.findViewById(R.id.you_button).setOnClickListener(v -> {
            if (mainActivity != null) mainActivity.setWindowBrightness(-1f); // Restore auto brightness
            openPhraseFragment("you", R.color.eng_violet);
        });

        fallingButton.setOnClickListener(v -> {
            if (mainActivity != null) mainActivity.setFallingBrightness(); // Conditionally set brightness to low
            CooldownManager.startCooldown(requireContext(), "falling");
            openPhraseFragment("falling", R.color.ultraviolet_light);
        });

        risingButton.setOnClickListener(v -> {
            if (mainActivity != null) mainActivity.setRisingBrightness(); // Conditionally set brightness to high
            CooldownManager.startCooldown(requireContext(), "rising");
            openPhraseFragment("rising", R.color.mn_blue);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Restore auto-brightness when returning to the menu
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setWindowBrightness(-1f);
        }
        startUpdatingCountdown();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopUpdatingCountdown();
    }

    private void startUpdatingCountdown() {
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                updateButtonState("falling", fallingButton, fallingCountdownText);
                updateButtonState("rising", risingButton, risingCountdownText);
                countdownHandler.postDelayed(this, 1000);
            }
        };
        countdownHandler.post(countdownRunnable);
    }

    private void stopUpdatingCountdown() {
        countdownHandler.removeCallbacks(countdownRunnable);
    }

    private void updateButtonState(String category, Button button, TextView countdownText) {
        if (!isAdded()) return; // Ensure fragment is still attached

        long remainingMillis = CooldownManager.getRemainingCooldown(requireContext(), category);

        if (remainingMillis > 0) {
            button.setEnabled(false);
            button.setAlpha(0.5f);
            countdownText.setVisibility(View.VISIBLE);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis);
            countdownText.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
        } else {
            button.setEnabled(true);
            button.setAlpha(1.0f);
            countdownText.setVisibility(View.GONE);
        }
    }

    private void openPhraseFragment(String category, int colorResId) {
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
