package com.example.classx.ui.settings;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.classx.R;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    FirebaseAuth auth;
    Button signOutButton;
    AppCompatActivity activity;
    Button spacerView;
    int[] rowIds;
    LinearLayout[] rows;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        auth = FirebaseAuth.getInstance();
        signOutButton = root.findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(this::signOut);

        rowIds = new int[]{R.id.row1, R.id.row2, R.id.row3, R.id.row4, R.id.row5, R.id.row6};
        spacerView = root.findViewById(R.id.spacerView);
        rows = new LinearLayout[6];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, getScreenHeight() / 9);
        for (int i = 0; i < 6; i++) {
            rows[i] = root.findViewById(rowIds[i]);
            rows[i].setLayoutParams(params);
        }

        rows[2].setOnClickListener(this::onClick);

        activity = (AppCompatActivity) requireActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.app_theme_blue)));
        }
        activity.setTitle("Settings");

        return root;
    }

    private void onClick(View view) {
        Intent intent = new Intent(getContext(), ThemeActivity.class);
        startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    public void signOut(View view) {
        auth.signOut();
        if (getActivity() != null)
            getActivity().finish();
    }


    public int getPixelDistance(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().densityDpi / 160d);
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
}