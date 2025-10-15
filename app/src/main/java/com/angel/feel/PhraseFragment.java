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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhraseFragment extends Fragment {

    private static final String ARG_CATEGORY = "category";
    private static final String ARG_COLOR_RES_ID = "colorResId";
    private static final int DISPLAY_DURATION_MS = 4000; // 4 seconds

    private static final String PREFS_NAME = "FeelAppPrefs";
    private static final String KEY_HISTORY_PREFIX = "history_";
    private static final String KEY_PROGRESSIVE_PREFIX = "progressive_";
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

        String displayText = selectedPhrase.replaceAll("^\\s*\\(\\w+\\s+(\\d+/\\d+)\\)\\s*", "($1) ");
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

        // Priority 1: Find a mandatory sequential phrase that MUST be shown next.
        String mandatoryPhrase = findNextMandatoryPhrase(allPhrases, category, prefs);
        if (mandatoryPhrase != null) {
            return mandatoryPhrase;
        }

        // Priority 2: If no mandatory phrase, get a random one from the pool of "starter" phrases.
        List<String> availablePhrases = getAvailableInitialPhrases(allPhrases);
        availablePhrases.removeAll(getRecentHistory(category, prefs));

        // If the pool is empty, it means we've seen everything recently.
        if (availablePhrases.isEmpty()) {
            List<String> allInitialPhrases = getAvailableInitialPhrases(allPhrases);
            Set<String> historySet = new HashSet<>(getRecentHistory(category, prefs));

            // If the history contains all possible initial phrases, we've completed a full cycle.
            if (historySet.containsAll(new HashSet<>(allInitialPhrases))) {
                // Clear history and progressive state to start over.
                prefs.edit()
                    .remove(KEY_HISTORY_PREFIX + category)
                    .remove(KEY_PROGRESSIVE_PREFIX + category)
                    .commit();
                // Rerun logic to get a fresh phrase.
                return getNextPhrase(category);
            }
            return MSG_END_OF_PHRASES; // Nothing new to show right now.
        }

        // Return a random phrase from the available pool.
        return availablePhrases.get(new Random().nextInt(availablePhrases.size()));
    }

    // Finds a phrase that is the direct continuation of a series (e.g., part 2 after part 1).
    private String findNextMandatoryPhrase(String[] allPhrases, String category, SharedPreferences prefs) {
        Set<String> unlocked = prefs.getStringSet(KEY_PROGRESSIVE_PREFIX + category, new HashSet<>());
        if (unlocked.isEmpty()) {
            return null;
        }
        Pattern pattern = Pattern.compile("^\\s*\\((\\w+)\\s(\\d+)/(\\d+)\\)");

        for (String phrase : allPhrases) {
            Matcher matcher = pattern.matcher(phrase);
            if (matcher.find()) {
                String group = matcher.group(1);
                int step = Integer.parseInt(matcher.group(2));
                if (step > 1) {
                    String prevStepKey = group + "_" + (step - 1);
                    String currentStepKey = group + "_" + step;
                    // If previous part is unlocked AND current part is not, this is the one.
                    if (unlocked.contains(prevStepKey) && !unlocked.contains(currentStepKey)) {
                        return phrase;
                    }
                }
            }
        }
        return null;
    }

    // Gets a pool of phrases for random selection: non-progressive and 1st parts of series.
    private List<String> getAvailableInitialPhrases(String[] allPhrases) {
        List<String> available = new ArrayList<>();
        Pattern pattern = Pattern.compile("^\\s*\\((\\w+)\\s(\\d+)/(\\d+)\\)");
        for (String phrase : allPhrases) {
            Matcher matcher = pattern.matcher(phrase);
            if (matcher.find()) {
                if (Integer.parseInt(matcher.group(2)) == 1) {
                    available.add(phrase); // It's a part 1, so it's a "starter"
                }
            } else {
                available.add(phrase); // Not a progressive phrase
            }
        }
        return available;
    }

    private List<String> getRecentHistory(String category, SharedPreferences prefs) {
        String historyString = prefs.getString(KEY_HISTORY_PREFIX + category, "");
        if (historyString.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(historyString.split("\\|\\|")));
    }

    private void saveProgress(String category, String chosenPhrase) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Pattern pattern = Pattern.compile("^\\s*\\((\\w+)\\s(\\d+)/(\\d+)\\)");
        Matcher matcher = pattern.matcher(chosenPhrase);
        if (matcher.find()) {
            String group = matcher.group(1);
            int step = Integer.parseInt(matcher.group(2));
            String unlockKey = group + "_" + step;
            Set<String> unlocked = new HashSet<>(prefs.getStringSet(KEY_PROGRESSIVE_PREFIX + category, new HashSet<>()));
            unlocked.add(unlockKey);
            editor.putStringSet(KEY_PROGRESSIVE_PREFIX + category, unlocked);
        }

        List<String> history = getRecentHistory(category, prefs);
        history.add(0, chosenPhrase);
        while (history.size() > HISTORY_SIZE) {
            history.remove(history.size() - 1);
        }
        editor.putString(KEY_HISTORY_PREFIX + category, String.join("||", history));

        editor.commit(); // Use commit() to ensure data is saved before the next fragment loads.
    }

    private String[] getPhrasesForCategory(String category) {
        if (category == null) return null;
        if (category.equals("you")) {
            // "you" phrases are stored in strings.xml now
            int arrayId = getResources().getIdentifier("you_phrases", "array", requireActivity().getPackageName());
            return (arrayId == 0) ? null : getResources().getStringArray(arrayId);
        } else {
            int arrayId = getResources().getIdentifier(category + "_phrases", "array", requireActivity().getPackageName());
            if (arrayId == 0) return null;
            return getResources().getStringArray(arrayId);
        }
    }
}
