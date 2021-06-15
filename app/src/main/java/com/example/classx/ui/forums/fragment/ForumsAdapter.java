package com.example.classx.ui.forums.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.classx.DrawableBackground;
import com.example.classx.ui.forums.navigation.NavigationDrawerActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class ForumsAdapter extends BaseAdapter {

    private static ArrayList<DrawableBackground> sideBarDrawables;
    private final Context context;
    private final ArrayList<String> forumNames, forumIDs, forumTypes;
    private static Drawable color;
    private final ArrayList<Button> buttons;
    private final Resources resources;

    public ForumsAdapter(Context context, ArrayList<String> forumIDs, ArrayList<String> forumNames, ArrayList<String> forumTypes) {
        Log.wtf("(ForumsAdapter.java:32)", "ForumsAdapter(Context, ArrayList<String>) called");
        this.context = context;
        color = new DrawableBackground(191, 223, 255);
        buttons = new ArrayList<>();
        this.resources = context.getResources();
        this.forumIDs = forumIDs;
        this.forumNames = forumNames;
        this.forumTypes = forumTypes;
        sideBarDrawables = setSideBarDrawables();
    }

    public static ArrayList<DrawableBackground> setSideBarDrawables() {
        return new ArrayList<>(Arrays.asList(
                new DrawableBackground(205, 222, 204),
                new DrawableBackground(235, 175, 160),
                new DrawableBackground(240, 177, 224),
                new DrawableBackground(237, 232, 166),
                new DrawableBackground(250, 197, 97),
                new DrawableBackground(152, 251, 152)
        ));
    }

    @Override
    public int getCount() {
        return forumNames.size();
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Log.wtf("(ForumsAdapter.java:68)", "ForumsAdapter.getView() called");
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        int width = (getScreenWidth() - getPixelDistance(8)) * 13 / 32;
        int height = getScreenHeight() / 6;

        View sideBar = new View(context);
        sideBar.setBackground(getSideDrawable(i));     //Insert View Color Here
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width * 2 / 9, height);
        sideBar.setLayoutParams(params);

        Button button = new Button(context);
        button.setText(forumNames.get(i));
        button.setTextColor(Color.BLACK);
        button.setBackground(color);
        button.setTextSize(24);
        button.setHeight(height);
        button.setWidth(width);

        buttons.add(button);
        Log.wtf("(ForumsAdapter.java:78)", forumNames.get(i));
        button.setOnClickListener(v -> {
            Intent intent = new Intent(context, NavigationDrawerActivity.class);
            intent.putExtra("ForumID", forumIDs.get(i));
            intent.putExtra("ForumName", forumNames.get(i));
            Log.wtf("(ForumsAdapter.java:82)", intent.getStringExtra("ForumID"));
            context.startActivity(intent);
        });

        layout.addView(sideBar);
        layout.addView(button);

        return layout;
    }

    public DrawableBackground getSideDrawable(int i) {
        switch (forumTypes.get(i)) {
            case "Class":
                return sideBarDrawables.get(0);
            case "Club":
                return sideBarDrawables.get(1);
            case "Team":
                return sideBarDrawables.get(2);
            case "Study Group":
                return sideBarDrawables.get(3);
            case "Interest Group":
                return sideBarDrawables.get(4);
            default:
                return sideBarDrawables.get(5);
        }
    }

    public ArrayList<Button> getButtons() {
        return buttons;
    }


    public int getPixelDistance(int dp) {
        return (int) (dp * resources.getDisplayMetrics().densityDpi / 160d);
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
}