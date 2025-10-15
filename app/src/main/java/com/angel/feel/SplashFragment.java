package com.angel.feel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SplashFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button feelButton = view.findViewById(R.id.feel_button);
        FloatingActionButton addPhraseButton = view.findViewById(R.id.add_phrase_button);

        // Get the MainActivity instance to call the navigateTo method
        MainActivity mainActivity = (MainActivity) getActivity();

        if (mainActivity != null) {
            feelButton.setOnClickListener(v -> {
                // Navigate to MenuFragment, and allow back navigation
                mainActivity.navigateTo(new MenuFragment(), true);
            });

            addPhraseButton.setOnClickListener(v -> {
                // Navigate to AddPhraseFragment, and allow back navigation
                mainActivity.navigateTo(new AddPhraseFragment(), true);
            });
        }
    }
}
