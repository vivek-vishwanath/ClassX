package com.example.classx.ui.forums.fragment;

import androidx.appcompat.app.ActionBar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.classx.R;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ForumsFragment extends Fragment {

    GridView gridView;
    FloatingActionButton addForumButton;
    EditText postText;
    DialogInterface dialogInterface;
    ForumsAdapter adapter;

    FirebaseAuth auth;
    FirebaseFirestore firestore;
    String userId, forumID;
    ArrayList<String> forumIdList, forumNames, forumTypes;
    String privateForumName, privateForumType;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_forums, container, false);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        ActionBar actionBar = activity.getSupportActionBar();

        if (actionBar != null)
            actionBar.show();

        activity.setTitle("Forums");

        gridView = root.findViewById(R.id.forumsView);
        addForumButton = root.findViewById(R.id.addForum);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        if (userId == null) activity.onBackPressed();

        forumIdList = new ArrayList<>();

        addForumButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), NewForumActivity.class);
            startActivityForResult(intent, 1);
        });

        Log.wtf("(ForumsFragment.java:69)", "ForumsFragment.onCreate() called");

        firestore.collection("users").document(userId).get().addOnCompleteListener(this::onActivityResultComplete);
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.wtf("(ForumsFragment.java:79)", "OnActivityResult Reached!");
        Log.wtf("(ForumsFragment.java:80)", "Request Code = " + requestCode);
        firestore.collection("users").document(userId).get().addOnCompleteListener(this::onActivityResultComplete);
    }

    private void onActivityResultComplete(Task<DocumentSnapshot> task) {
        forumIdList = new ArrayList<>();
        forumNames = new ArrayList<>();
        forumTypes = new ArrayList<>();
        listenerIndex = 0;
        if (task.isSuccessful()) {
            ArrayList<String> forums = (ArrayList<String>) Objects.requireNonNull(task.getResult()).get("forums");
            if (forums != null)
                forumIdList.addAll(forums);
        }
        Log.wtf("(ForumsFragment.java:103)", forumIdList.toString());
        if (forumIdList.size() > 0)
            firestore.collection("forums").document(forumIdList.get(0)).get().addOnCompleteListener(this::onCompleteForumNames);
    }

    private static int listenerIndex = 0;

    private void onCompleteForumNames(Task<DocumentSnapshot> task) {
        if (task.isSuccessful() && task.getResult() != null) {
            forumNames.add(task.getResult().getString("name"));
            String type = task.getResult().getString("type");
            if (type == null) type = "Class";
            forumTypes.add(type);
        }
        Log.wtf("(ForumsFragment.java:116)", listenerIndex + ": " + forumNames);
        listenerIndex++;
        if (listenerIndex < forumIdList.size())
            firestore.collection("forums").document(forumIdList.get(listenerIndex)).get().addOnCompleteListener(this::onCompleteForumNames);
        Log.wtf("(ForumsFragment.java:115)", Objects.requireNonNull(task.getResult()).getString("name"));
        adapter = new ForumsAdapter(requireContext(), forumIdList, forumNames, forumTypes);
        gridView.setAdapter(adapter);
        Log.wtf("(ForumsFragment.java:118)", forumNames.toString());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.options_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.create_forum) {
            Intent intent = new Intent(getContext(), NewForumActivity.class);
            startActivityForResult(intent, 1);

        } else if (id == R.id.join_private_forum) {
            joinPrivateForum();
        } else if (id == R.id.join_public_forum) {
            Intent intent = new Intent(getContext(), FindForumActivity.class);
            startActivityForResult(intent, 1);
        }

        return super.onOptionsItemSelected(item);
    }

    public void joinPrivateForum() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        postText = new EditText(getContext());
        builder.setTitle("Enter join code:")
                .setView(postText)
                .setPositiveButton("Join", this::onClick)
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel()).show();
        onActivityResult(1, RESULT_OK, null);
    }

    private void onClick(DialogInterface dialogInterface, int i) {
        this.dialogInterface = dialogInterface;
        firestore.collection("forums").whereEqualTo("key", postText.getText().toString())
                .get().addOnCompleteListener(this::onCompleteForums);
    }

    private void onCompleteForums(Task<QuerySnapshot> task) {
        if (task.isSuccessful() && task.getResult() != null) {
            if (task.getResult().getDocuments().size() == 0) {
                Log.wtf("(ForumsFragment.java:156", "Zero Documents Found with that Key (" + postText.getText() + ")");
            }
            DocumentSnapshot document = task.getResult().getDocuments().get(0);
            if (document.getData() != null) {
                privateForumName = document.getString("name");
                privateForumType = document.getString("type");
                ArrayList<String> users = (ArrayList<String>) document.get("users");
                if (users == null) users = new ArrayList<>();
                users.add(userId);
                forumID = document.getId();
                firestore.collection("forums").document(forumID).update("users", users);
                firestore.collection("users").document(userId)
                        .get().addOnCompleteListener(this::onCompleteUser);
            }
        } else {
            this.dialogInterface.cancel();
        }
    }

    private void onCompleteUser(Task<DocumentSnapshot> task) {
        if (task.isSuccessful() && task.getResult() != null) {
            ArrayList<String> forums = (ArrayList<String>) task.getResult().get("forums");
            if (forums == null) forums = new ArrayList<>();
            forums.add(forumID);
            firestore.collection("users").document(userId).update("forums", forums);
            forumIdList.add(forumID);
            if (privateForumName != null) forumNames.add(privateForumName);
            if (privateForumType != null) forumTypes.add(privateForumType);
            else forumTypes.add("Class");
            adapter = new ForumsAdapter(requireContext(), forumIdList, forumNames, forumTypes);
            gridView.setAdapter(adapter);
        }
    }
}