package com.example.classx.ui.forums.navigation.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.classx.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ForumSettingsFragment extends Fragment {

    TextView groupNameTV, descriptionTV, codeTV, typeTV;
    ImageView lockIV;
    Intent intent;
    ArrayList<String> users;
    UsersAdapter adapter;
    RecyclerView userList;

    FirebaseAuth auth;
    FirebaseFirestore firestore;
    String forumID;

    AppCompatActivity activity;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_forum_settings, container, false);

        activity = (AppCompatActivity) requireActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        intent = activity.getIntent();

        forumID = intent.getStringExtra("ForumID");

        groupNameTV = root.findViewById(R.id.groupNameTV);
        descriptionTV = root.findViewById(R.id.descriptionTV);
        codeTV = root.findViewById(R.id.codeTV);
        typeTV = root.findViewById(R.id.typeTextView);
        lockIV = root.findViewById(R.id.lockImageView);

        users = new ArrayList<>();
        userList = root.findViewById(R.id.userRecyclerView);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        firestore.collection("forums").document(forumID).get().addOnCompleteListener(this::onComplete);

        return root;
    }

    private void onComplete(Task<DocumentSnapshot> task) {
        if(task.isSuccessful() && task.getResult() != null){
            DocumentSnapshot doc = task.getResult();
            String description = doc.getString("description");
            String key = "Code: " + doc.getString("key");
            String name = doc.getString("name");
            Boolean privacy = doc.getBoolean("private");
            String type = "Type: " + doc.getString("type");
            ArrayList<String> userIdList = (ArrayList<String>) doc.get("users");

            if(userIdList == null) userIdList = new ArrayList<>();

            Log.wtf("(ForumSettingsFragment.java:84)", userIdList.toString());
            adapter = new UsersAdapter(getContext(), userIdList, forumID);
            userList.setLayoutManager(new LinearLayoutManager(getContext()));
            userList.setAdapter(adapter);
            Log.wtf("(ForumSettingsFragment.java:88)", userList.getChildCount() + "");

            if (privacy != null && !privacy) {
                codeTV.setVisibility(View.GONE);
                lockIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_lock_open_24));
            }
            groupNameTV.setText(name);
            descriptionTV.setText(description);
            codeTV.setText(key);
            typeTV.setText(type);

            Log.wtf("(ForumSettingsFragment.java:102)", "");
        }
    }
}