package com.angel.feel;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Random;
import java.util.Set;

public class PhraseFragment extends Fragment {

    private static final String ARG_CATEGORY = "category";
    private static final String ARG_COLOR_RES_ID = "colorResId";
    private static final int DISPLAY_DURATION_MS = 4000; // 4 seconds

    public static PhraseFragment newInstance(String category, int colorResId) {
        PhraseFragment fragment = new PhraseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        args.putInt(ARG_COLOR_RES_ID, colorResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_phrase, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView phraseTextView = view.findViewById(R.id.fullscreen_phrase_text);
        String category = getArguments().getString(ARG_CATEGORY);
        int colorResId = getArguments().getInt(ARG_COLOR_RES_ID);

        view.setBackgroundColor(ContextCompat.getColor(getContext(), colorResId));

        phraseTextView.setAlpha(0f);

        String[] phrases = getPhrasesForCategory(category);

        if (phrases != null && phrases.length > 0) {
            int randomIndex = new Random().nextInt(phrases.length);
            String randomPhrase = phrases[randomIndex];
            phraseTextView.setText(randomPhrase);
        } else {
            if ("you".equals(category.toLowerCase())) {
                phraseTextView.setText("you haven\'t added any phrases, yet...");
            } else {
                phraseTextView.setText("could not load phrases.");
            }
        }

        phraseTextView.animate()
                .alpha(1f)
                .setDuration(3000)
                .start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                getParentFragmentManager().popBackStack();
            }
        }, DISPLAY_DURATION_MS);
    }

    private String[] getPhrasesForCategory(String category) {
        if (category == null) {
            return null;
        }

        switch (category.toLowerCase()) {
            case "you":
                SharedPreferences prefs = requireContext().getSharedPreferences("user_phrases", Context.MODE_PRIVATE);
                Set<String> userPhrases = prefs.getStringSet("you_phrases", null);
                if (userPhrases == null || userPhrases.isEmpty()) {
                    return null;
                }
                return userPhrases.toArray(new String[0]);
            case "rising":
                return getResources().getStringArray(R.array.rising_phrases);
            case "falling":
                return getResources().getStringArray(R.array.falling_phrases);
            default:
                return null;
        }
    }
}
