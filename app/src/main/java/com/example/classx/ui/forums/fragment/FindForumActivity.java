package com.example.classx.ui.forums.fragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.classx.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;


public class FindForumActivity extends AppCompatActivity {

    ListView queryList;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> groupNames;
    ArrayList<String> groupIDs;
    EditText searchET;
    SharedPreferences sharedPreferences;

    FirebaseFirestore firestore;
    FirebaseAuth auth;
    String uid, forumID;
    DocumentReference thisForum;
    DocumentReference thisUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_forum);

        //Connects listView and search bar from their resource values to their objects
        queryList = findViewById(R.id.groupQueryList);
        searchET = findViewById(R.id.forumSearchET);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        uid = auth.getUid();

        //Initializes shared preferences so that info can be moved back to the previous activity
        sharedPreferences = this.getSharedPreferences("com.example.classx.ui.groups", Context.MODE_PRIVATE);

        //Checks if a user from the query list has been clicked on
        queryList.setOnItemClickListener((adapterView, view, i, l) -> {
            sharedPreferences.edit().putString("Group Name", groupNames.get(i)).apply();
            sharedPreferences.edit().putString("Group IDs", groupIDs.get(i)).apply();

            thisForum = firestore.collection("forums").document(groupIDs.get(i));
            thisUser = firestore.collection("users").document(uid);

            forumID = groupIDs.get(i);

            thisForum.get().addOnCompleteListener(this::onCompleteForums);
            thisUser.get().addOnCompleteListener(this::onCompleteUsers);
        });
    }

    public void findQuery(View view) {
        //Creates ArrayLists to store contact names and ParseUsers
        groupNames = new ArrayList<>();
        groupIDs = new ArrayList<>();

        firestore.collection("forums").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.wtf("(FindForumActivity.java:83)", document.getData().toString());
                    String name = document.getString("name");
                    Boolean privacy = document.getBoolean("private");
                    ArrayList<String> users = (ArrayList<String>) document.get("users");
                    assert name != null;
                    if (name.toLowerCase().contains(searchET.getText().toString().toLowerCase()) && privacy != null && !privacy) {
                        assert users != null;
                        if (!users.contains(uid)) {
                            groupNames.add(name);
                            groupIDs.add(document.getId());
                            Log.wtf("(FindForumActivity.java:93)", name);
                        }
                    }
                }
                Log.wtf("(FindForumActivity.java:109)", groupIDs.toString());
            } else {
                Log.wtf("(FindForumActivity:111)", "Forum Task Not Found");
            }
        });

        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, groupNames) {
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                // Get the Item from ListView
                View view = super.getView(position, convertView, parent);
                TextView tv = view.findViewById(android.R.id.text1);
                tv.setTextColor(Color.WHITE);
                return view;
            }
        };
        queryList.setAdapter(arrayAdapter);
    }

    // Substrings input to find a match in UserNames
    public boolean containsString(String string, String substring) {
        string = string.toLowerCase();
        substring = substring.toLowerCase();
        int n = substring.length();
        for (int i = 0; i <= string.length() - n; i++) {
            if (string.substring(i, i + n).equals(substring)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(ArrayList<String> arrayList, String string) {
        for (String s : arrayList) {
            if (s.equals(string))
                return true;
        }
        return false;
    }

    private void onCompleteForums(Task<DocumentSnapshot> task) {
        if (task.isSuccessful() && task.getResult() != null) {
            ArrayList<String> users = (ArrayList<String>) task.getResult().get("users");
            if (users == null) users = new ArrayList<>();
            users.add(uid);
            thisForum.update("users", users);
        } else {
            Log.wtf("(FindForumActivity.java:72)", "Forum Task Not Found");
        }
    }

    private void onCompleteUsers(Task<DocumentSnapshot> task) {
        if (task.isSuccessful() && task.getResult() != null) {
            ArrayList<String> forums = (ArrayList<String>) task.getResult().get("forums");
            if (forums == null) forums = new ArrayList<>();
            forums.add(forumID);
            thisUser.update("forums", forums);
        }
        FindForumActivity.this.setResult(RESULT_OK);
        FindForumActivity.this.onBackPressed();
    }
}