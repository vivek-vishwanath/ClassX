package com.example.classx.ui.forums.fragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.classx.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewForumActivity extends AppCompatActivity {

    EditText nameET, descriptionET;
    TextView keyTextView;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    CheckBox privacyCheckBox;
    boolean isPrivate;
    Spinner forumTypeSpinner;
    String forumID;
    String[] forumTypes = new String[]{"Class", "Club", "Team", "Study Group", "Interest Group", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_forum);

        nameET = findViewById(R.id.eventNameEditText);
        descriptionET = findViewById(R.id.descriptionEditText);
        privacyCheckBox = findViewById(R.id.privacyCheckBox);
        forumTypeSpinner = findViewById(R.id.forumTypeSpinner);
        keyTextView = findViewById(R.id.keyTextView);

        forumID = UUID.randomUUID().toString();
        keyTextView.setText("Code: " + forumID.substring(0, 6));
        keyTextView.setVisibility(View.INVISIBLE);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, forumTypes){
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = view.findViewById(android.R.id.text1);
                tv.setTextColor(getResources().getColor(R.color.text_color));
                tv.setTextSize(24);
                return view;
            }
        };
        forumTypeSpinner.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        isPrivate = privacyCheckBox.isChecked();
        privacyCheckBox.setOnClickListener(view -> {
            isPrivate = !isPrivate;
            if(keyTextView.getVisibility() == View.VISIBLE)
                keyTextView.setVisibility(View.INVISIBLE);
            else
                keyTextView.setVisibility(View.VISIBLE);
        });
    }

    public void createGroup(View view){
        String name = nameET.getText().toString();
        String description = descriptionET.getText().toString();
        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> map = new HashMap<>();
        ArrayList<String> users = new ArrayList<>();
        users.add(uid);

        map.put("name", name);
        map.put("users", users);
        map.put("description", description);
        map.put("key", forumID.substring(0, 6));
        map.put("forum_id", forumID);
        map.put("private", isPrivate);
        map.put("type", forumTypeSpinner.getSelectedItem());

        firestore.collection("forums").document(forumID).set(map);
        firestore.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Map<String, Object> data = task.getResult().getData();
                ArrayList<String> userForums = (ArrayList<String>) data.get("forums");
                if(userForums == null) userForums = new ArrayList<>();
                userForums.add(forumID);
                firestore.collection("users").document(uid).update("forums", userForums);
                Log.wtf("(NewForumActivity.java:81)", data.toString());
            } else {
                Log.wtf("(NewForumActivity.java:83)", "Task Unsuccessful", task.getException());
            }
            setResult(RESULT_OK);
            onBackPressed();
        });
    }
}