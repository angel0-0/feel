package com.angel.feel;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            // If the activity is newly created, load the initial SplashFragment
            navigateTo(new SplashFragment(), false);
        }
    }

    /**
     * Replaces the current fragment with a new one, handling the transaction and animations.
     * @param fragment The new fragment to display.
     * @param addToBackStack True to add this transaction to the back stack, allowing the user to press "back".
     */
    public void navigateTo(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Use a consistent fade transition for all fragment changes
        fragmentTransaction.setCustomAnimations(
            android.R.anim.fade_in, android.R.anim.fade_out,
            android.R.anim.fade_in, android.R.anim.fade_out
        );

        fragmentTransaction.replace(R.id.fragment_container, fragment);

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.toString());
        }

        fragmentTransaction.commit();
    }
}
