package com.angel.feel;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhraseFragment extends Fragment {

    private static final String ARG_CATEGORY = "category";
    private static final String ARG_COLOR_RES_ID = "colorResId";
    private static final int DISPLAY_DURATION_MS = 4000; // 4 seconds

    private static final String PREFS_NAME = "FeelAppPrefs";
    private static final String KEY_HISTORY_PREFIX = "history_";
    private static final String KEY_ACTIVE_GROUP_PREFIX = "active_group_";
    private static final String KEY_NEXT_STEP_PREFIX = "next_step_";
    private static final int HISTORY_SIZE = 3;

    // Messages
    private static final String MSG_NO_PHRASES_ADDED = "you haven't added any phrases, yet...";
    private static final String MSG_END_OF_PHRASES = "you´ve reached the end, for now.";

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

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), colorResId));

        if ("you".equals(category)) {
            try {
                Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.lostar);
                phraseTextView.setTypeface(typeface);
            } catch (Exception e) {
                Log.e("PhraseFragment", "Could not load font: R.font.lostar", e);
            }
        }

        phraseTextView.setAlpha(0f);

        String selectedPhrase = getNextPhrase(category);

        // Regex to show only the counter, not the group name. E.g. "(2/3) some text"
        String displayText = selectedPhrase.replaceAll("^\\s*\\(.+?\\s+(\\d+/\\d+)\\)\\s*", "($1) ");
        phraseTextView.setText(displayText);

        phraseTextView.animate().alpha(1f).setDuration(1500).start();

        if (!selectedPhrase.equals(MSG_NO_PHRASES_ADDED) && !selectedPhrase.equals(MSG_END_OF_PHRASES)) {
            saveProgress(category, selectedPhrase);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) {
                getParentFragmentManager().popBackStack();
            }
        }, DISPLAY_DURATION_MS);
    }

    private String getNextPhrase(String category) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String[] allPhrases = getPhrasesForCategory(category);

        if (allPhrases == null || allPhrases.length == 0) {
            return MSG_NO_PHRASES_ADDED;
        }

        // Priority 1: Check for an active, mandatory sequence.
        String activeGroup = prefs.getString(KEY_ACTIVE_GROUP_PREFIX + category, null);
        if (activeGroup != null) {
            int nextStep = prefs.getInt(KEY_NEXT_STEP_PREFIX + category, -1);
            if (nextStep != -1) {
                // Pattern to find the exact next phrase in the sequence.
                Pattern pattern = Pattern.compile("^\\s*\\(" + Pattern.quote(activeGroup) + "\\s+" + nextStep + "/\\d+\\)");
                for (String phrase : allPhrases) {
                    if (pattern.matcher(phrase).find()) {
                        return phrase; // Found the mandatory next phrase.
                    }
                }
            }
            // Fallback: If the mandatory phrase wasn't found (data error), clear the sequence to prevent getting stuck.
            prefs.edit()
                    .remove(KEY_ACTIVE_GROUP_PREFIX + category)
                    .remove(KEY_NEXT_STEP_PREFIX + category)
                    .commit();
        }

        // Priority 2: Find a random "starter" phrase (part 1 or non-progressive) that is not in recent history.
        List<String> initialPhrases = getAvailableInitialPhrases(allPhrases);
        if (initialPhrases.isEmpty()) {
            return MSG_END_OF_PHRASES;
        }

        List<String> availablePhrases = new ArrayList<>(initialPhrases);
        availablePhrases.removeAll(getRecentHistory(category, prefs));

        if (!availablePhrases.isEmpty()) {
            return availablePhrases.get(new Random().nextInt(availablePhrases.size()));
        }

        // Priority 3: All starters have been seen recently. To avoid getting stuck, clear the history for this category and pick one.
        prefs.edit().remove(KEY_HISTORY_PREFIX + category).commit();
        return initialPhrases.get(new Random().nextInt(initialPhrases.size()));
    }

    private List<String> getAvailableInitialPhrases(String[] allPhrases) {
        List<String> available = new ArrayList<>();
        Pattern pattern = Pattern.compile("^\\s*\\((.+?)\\s+(\\d+)/(\\d+)\\)");
        for (String phrase : allPhrases) {
            Matcher matcher = pattern.matcher(phrase);
            if (matcher.find()) {
                // It's a progressive phrase. Only add it if it's part 1.
                if ("1".equals(matcher.group(2))) {
                    available.add(phrase);
                }
            } else {
                // Not a progressive phrase, so it's a starter.
                available.add(phrase);
            }
        }
        return available;
    }

    private List<String> getRecentHistory(String category, SharedPreferences prefs) {
        String historyString = prefs.getString(KEY_HISTORY_PREFIX + category, "");
        return historyString.isEmpty() ? new ArrayList<>() : new ArrayList<>(Arrays.asList(historyString.split("\\|\\|")));
    }

    private void saveProgress(String category, String chosenPhrase) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Pattern pattern = Pattern.compile("^\\s*\\((.+?)\\s+(\\d+)/(\\d+)\\)");
        Matcher matcher = pattern.matcher(chosenPhrase);

        if (matcher.find()) {
            String group = matcher.group(1);
            int step = Integer.parseInt(matcher.group(2));
            int total = Integer.parseInt(matcher.group(3));

            if (step < total) {
                // Sequence in progress: set the next mandatory step.
                editor.putString(KEY_ACTIVE_GROUP_PREFIX + category, group);
                editor.putInt(KEY_NEXT_STEP_PREFIX + category, step + 1);
            } else {
                // Sequence complete: clear the state.
                editor.remove(KEY_ACTIVE_GROUP_PREFIX + category);
                editor.remove(KEY_NEXT_STEP_PREFIX + category);
            }
        } else {
            // Not a progressive phrase: ensure no sequence is active.
            editor.remove(KEY_ACTIVE_GROUP_PREFIX + category);
            editor.remove(KEY_NEXT_STEP_PREFIX + category);
        }

        // Update recent history to avoid immediate repeats.
        List<String> history = getRecentHistory(category, prefs);
        history.add(0, chosenPhrase);
        while (history.size() > HISTORY_SIZE) {
            history.remove(history.size() - 1);
        }
        editor.putString(KEY_HISTORY_PREFIX + category, String.join("||", history));

        // Commit synchronously to ensure the state is saved before the next phrase is requested.
        editor.commit();
    }

    private String[] getPhrasesForCategory(String category) {
        if (category == null) return new String[0];
        String packageName = requireActivity().getPackageName();
        int arrayId = getResources().getIdentifier(category + "_phrases", "array", packageName);
        return (arrayId == 0) ? new String[0] : getResources().getStringArray(arrayId);
    }
}
