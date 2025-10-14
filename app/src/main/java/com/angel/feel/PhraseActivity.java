package com.angel.feel;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class PhraseActivity extends AppCompatActivity {

    private static final int DISPLAY_DURATION_MS = 5000; // 5 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phrase);

        TextView phraseTextView = findViewById(R.id.fullscreen_phrase_text);
        String category = getIntent().getStringExtra("CATEGORY");

        int phraseArrayId = -1;
        if ("falling".equals(category)) {
            phraseArrayId = R.array.falling_phrases;
        } else if ("rising".equals(category)) {
            phraseArrayId = R.array.rising_phrases;
        }

        if (phraseArrayId != -1) {
            String[] phrases = getResources().getStringArray(phraseArrayId);
            if (phrases.length > 0) {
                String randomPhrase = phrases[new Random().nextInt(phrases.length)];
                phraseTextView.setText(randomPhrase);
            }
        }

        // Programar el cierre de la actividad
        new Handler(Looper.getMainLooper()).postDelayed(this::finish, DISPLAY_DURATION_MS);
    }
}