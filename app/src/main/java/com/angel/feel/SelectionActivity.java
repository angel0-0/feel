package com.angel.feel;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        Button fallingButton = findViewById(R.id.falling_button);
        Button risingButton = findViewById(R.id.rising_button);
        Button youButton = findViewById(R.id.you_button);

        fallingButton.setOnClickListener(v -> {
            Intent intent = new Intent(SelectionActivity.this, PhraseActivity.class);
            intent.putExtra("category", "falling");
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        risingButton.setOnClickListener(v -> {
            Intent intent = new Intent(SelectionActivity.this, PhraseActivity.class);
            intent.putExtra("category", "rising");
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // El botón "you" no hará nada por ahora.
        youButton.setOnClickListener(v -> {});
    }
}
