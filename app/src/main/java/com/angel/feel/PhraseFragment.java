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
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
    private static final int DISPLAY_DURATION_MS = 4000;

    private String currentPhrase;
    private Handler autoCloseHandler = new Handler(Looper.getMainLooper());
    private Runnable autoCloseRunnable;

    private static final String PREFS_NAME = "FeelAppPrefs";
    private static final String USER_PHRASES_PREFS = "user_phrases";
    private static final String KEY_USER_PHRASES = "you_phrases";

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
        ImageButton deleteButton = view.findViewById(R.id.delete_phrase_button);
        String category = getArguments().getString(ARG_CATEGORY);
        int colorResId = getArguments().getInt(ARG_COLOR_RES_ID);

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), colorResId));

        // Set font based on category
        Typeface typeface;
        if ("you".equalsIgnoreCase(category)) {
            typeface = ResourcesCompat.getFont(requireContext(), R.font.lostar);
        } else {
            typeface = ResourcesCompat.getFont(requireContext(), R.font.tribtwo);
        }
        phraseTextView.setTypeface(typeface);
        phraseTextView.setAlpha(0f);

        currentPhrase = getNextPhrase(category);

        // Regex to show only the counter, not the group name. E.g. "(2/3) some text"
        String displayText = currentPhrase.replaceAll("^\\s*\\(.+?\\s+(\\d+/\\d+)\\)\\s*", "($1) ");
        phraseTextView.setText(displayText);

        // Handle delete button visibility
        if ("you".equalsIgnoreCase(category) && !currentPhrase.equals(MSG_NO_PHRASES_ADDED)) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
        } else {
            deleteButton.setVisibility(View.GONE);
        }

        // Animate and schedule auto-close
        phraseTextView.animate().alpha(1f).setDuration(1500).start();

        if (!currentPhrase.equals(MSG_NO_PHRASES_ADDED) && !currentPhrase.equals(MSG_END_OF_PHRASES)) {
            saveProgress(category, currentPhrase);
        }

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
                Pattern pattern = Pattern.compile("^\\s*\\(" + Pattern.quote(activeGroup) + "\\s+" + nextStep + "/\\d+\\)");
                for (String phrase : allPhrases) {
                    if (pattern.matcher(phrase).find()) {
                        return phrase; // Found the mandatory next phrase.
                    }
                }
            }
            // Fallback: If mandatory phrase wasn't found (data error), clear sequence to prevent getting stuck.
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

        // Priority 3: All starters have been seen recently. Clear history and pick one to avoid getting stuck.
        prefs.edit().remove(KEY_HISTORY_PREFIX + category).commit();
        return initialPhrases.get(new Random().nextInt(initialPhrases.size()));
    }

    private String[] getPhrasesForCategory(String category) {
        if (category == null) return new String[0];

        if ("you".equalsIgnoreCase(category)) {
            SharedPreferences prefs = requireContext().getSharedPreferences(USER_PHRASES_PREFS, Context.MODE_PRIVATE);
            Set<String> userPhrases = prefs.getStringSet(KEY_USER_PHRASES, null);
            if (userPhrases == null || userPhrases.isEmpty()) {
                return new String[0];
            }
            return userPhrases.toArray(new String[0]);
        } else {
            String packageName = requireActivity().getPackageName();
            int arrayId = getResources().getIdentifier(category.toLowerCase() + "_phrases", "array", packageName);
            return (arrayId == 0) ? new String[0] : getResources().getStringArray(arrayId);
        }
    }

    private List<String> getAvailableInitialPhrases(String[] allPhrases) {
        List<String> available = new ArrayList<>();
        Pattern pattern = Pattern.compile("^\\s*\\((.+?)\\s+(\\d+)/(\\d+)\\)");
        for (String phrase : allPhrases) {
            Matcher matcher = pattern.matcher(phrase);
            if (matcher.find()) {
                if ("1".equals(matcher.group(2))) { // Progressive phrase, part 1
                    available.add(phrase);
                }
            } else { // Not a progressive phrase
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

        // Commit synchronously to ensure state is saved before the next phrase is requested.
        editor.commit();
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
        autoCloseHandler.removeCallbacks(autoCloseRunnable); // Stop auto-close

        if (currentPhrase != null && !currentPhrase.isEmpty()) {
            SharedPreferences prefs = requireContext().getSharedPreferences(USER_PHRASES_PREFS, Context.MODE_PRIVATE);
            Set<String> currentPhrases = prefs.getStringSet(KEY_USER_PHRASES, new HashSet<>());
            Set<String> newPhrases = new HashSet<>(currentPhrases);

            if (newPhrases.remove(currentPhrase)) {
                prefs.edit().putStringSet(KEY_USER_PHRASES, newPhrases).apply();
            }
        }

        if (isAdded()) {
            getParentFragmentManager().popBackStack(); // Close fragment
        }
    }
}
