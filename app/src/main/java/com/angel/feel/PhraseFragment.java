package com.angel.feel;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class PhraseFragment extends Fragment {

    private static final String ARG_CATEGORY = "category";
    private static final String ARG_COLOR_RES_ID = "colorResId";
    private static final int DISPLAY_DURATION_MS = 4000;

    private String currentPhrase;
    private Handler autoCloseHandler = new Handler(Looper.getMainLooper());
    private Runnable autoCloseRunnable;

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
        ImageButton deleteButton = view.findViewById(R.id.delete_phrase_button);
        String category = getArguments().getString(ARG_CATEGORY);
        int colorResId = getArguments().getInt(ARG_COLOR_RES_ID);

        view.setBackgroundColor(ContextCompat.getColor(getContext(), colorResId));

        // Set font based on category
        Typeface typeface;
        if ("you".equalsIgnoreCase(category)) {
            typeface = ResourcesCompat.getFont(requireContext(), R.font.lostar);
        } else {
            typeface = ResourcesCompat.getFont(requireContext(), R.font.tribtwo);
        }
        phraseTextView.setTypeface(typeface);

        phraseTextView.setAlpha(0f);

        String[] phrases = getPhrasesForCategory(category);

        if (phrases != null && phrases.length > 0) {
            int randomIndex = new Random().nextInt(phrases.length);
            currentPhrase = phrases[randomIndex];
            phraseTextView.setText(currentPhrase);

            if ("you".equalsIgnoreCase(category)) {
                deleteButton.setVisibility(View.VISIBLE);
                deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
            }
        } else {
            if ("you".equalsIgnoreCase(category)) {
                phraseTextView.setText("you haven't added any phrases, yet...");
            }
        }

        phraseTextView.animate().alpha(1f).setDuration(3000).start();

        autoCloseRunnable = () -> {
            if (isAdded()) {
                getParentFragmentManager().popBackStack();
            }
        };
        autoCloseHandler.postDelayed(autoCloseRunnable, DISPLAY_DURATION_MS);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        autoCloseHandler.removeCallbacks(autoCloseRunnable);
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

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Phrase")
                .setMessage("Are you sure you want to delete this phrase?")
                .setPositiveButton("Delete", (dialog, which) -> deleteCurrentPhrase())
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteCurrentPhrase() {
        autoCloseHandler.removeCallbacks(autoCloseRunnable);

        if (currentPhrase == null || currentPhrase.isEmpty()) {
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("user_phrases", Context.MODE_PRIVATE);
        Set<String> currentPhrases = prefs.getStringSet("you_phrases", new HashSet<>());
        Set<String> newPhrases = new HashSet<>(currentPhrases);
        
        if (newPhrases.remove(currentPhrase)) {
            prefs.edit().putStringSet("you_phrases", newPhrases).apply();
        }

        if (isAdded()) {
            getParentFragmentManager().popBackStack();
        }
    }
}
