package com.angel.feel;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashSet;
import java.util.Set;

public class AddPhraseFragment extends Fragment {

    private EditText newPhraseEditText;
    private Button savePhraseButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_phrase, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        newPhraseEditText = view.findViewById(R.id.new_phrase_edittext);
        savePhraseButton = view.findViewById(R.id.save_phrase_button);

        savePhraseButton.setOnClickListener(v -> savePhrase());
    }

    private void savePhrase() {
        // Context is required for SharedPreferences and Toasts, get it from the fragment's activity
        Context context = getContext();
        if (context == null) return; // Fragment is not attached

        String phrase = newPhraseEditText.getText().toString().trim();
        if (phrase.isEmpty()) {
            Toast.makeText(context, "nothing to save", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences("user_phrases", Context.MODE_PRIVATE);
        // It's crucial to get a new, mutable copy of the set before editing
        Set<String> phrases = new HashSet<>(prefs.getStringSet("you_phrases", new HashSet<>()));
        phrases.add(phrase);
        prefs.edit().putStringSet("you_phrases", phrases).apply();

        Toast.makeText(context, "saved", Toast.LENGTH_SHORT).show();

        // Go back to the previous screen
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}
