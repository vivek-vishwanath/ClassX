package com.example.classx.ui.settings;

import android.content.res.Configuration;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;

import com.example.classx.R;

public class ThemeActivity extends AppCompatActivity {

    RadioButton lightButton, darkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle("Settings");
        lightButton = findViewById(R.id.radioButtonLight);
        darkButton = findViewById(R.id.radioButtonDark);

        Configuration configuration = getResources().getConfiguration();
        int currentNightMode = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                lightButton.setChecked(true);
                darkButton.setChecked(false);
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                lightButton.setChecked(false);
                darkButton.setChecked(true);
                break;
        }

        lightButton.setOnClickListener(this::lightClick);
        darkButton.setOnClickListener(this::rightClick);

        Log.wtf("(ThemeActivity.java:17)", "ThemeActivity.onCreate() called");

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }

    private void lightClick(View view) {
        darkButton.setChecked(false);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void rightClick(View view) {
        lightButton.setChecked(false);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.wtf("(ThemeActivity.java:79)", "Changing Configuration");
    }
}