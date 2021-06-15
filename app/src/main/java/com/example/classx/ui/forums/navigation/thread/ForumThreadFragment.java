package com.example.classx.ui.forums.navigation.thread;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classx.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ForumThreadFragment extends Fragment {

    Intent intent;
    RecyclerView forumPostRV;
    List<ForumPost> forumPosts;

    String forumName;
    String forumID;
    FirebaseAuth auth;
    FirebaseFirestore firestore;

    EditText postText;
    Date date;
    String serialized;

    int postCount = 0;

    //Number of most recent messages
    static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_forum_thread, container, false);

        forumPostRV = root.findViewById(R.id.forumPostRecyclerView);
        intent = requireActivity().getIntent();

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        forumID = intent.getStringExtra("ForumID");
        firestore.collection("forums").document(forumID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                forumName = task.getResult().getString("name");
                requireActivity().setTitle(forumName);
//                if (task.getResult().get("posts") != null)
//                    postCount = ((ArrayList<String>) task.getResult().get("posts")).size();
            } else {
                Log.wtf("(ForumThreadFragment.java:43)", "Failed to Find Forum from ID");
            }
        });

        FloatingActionButton fab = root.findViewById(R.id.writeNewPost);
        fab.setOnClickListener(this::writeNewPost);

        setMessages();

        Log.wtf("(ForumThreadFragment.java:74)", forumID);
        firestore.collection("forums").document(forumID).addSnapshotListener((value, error) -> {
            if (error == null && value != null && value.exists() && value.getData() != null) {
                ArrayList<String> list = (ArrayList<String>) value.getData().get("posts");
//                if (list != null && list.size() > postCount) {
//                    ForumPost post = ObjectSerializer.deserialize(list.get(0));
//                    if (post != null && !post.getUid().equals(auth.getUid()))
//                        updateLayout(post);
//                    postCount = list.size();
//                }
                forumPosts = new ArrayList<>();
                if (list != null) {
                    for (int i = list.size() - 1; i >= 0; i--) {
                        ForumPost post = ObjectSerializer.deserialize(list.get(i));
                        if (post != null && !post.getUid().equals(auth.getUid()))
                            updateLayout(post);
                    }
                }

            }
        });

        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void writeNewPost(View view) {
        date = new Date();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        postText = new EditText(getContext());
        builder.setTitle("Write a post")
                .setView(postText)
                .setPositiveButton("Send", this::onClick)
                .setNegativeButton("Cancel", ((dialog, which) -> dialog.cancel()))
                .show();
    }

    private void onClick(DialogInterface dialog, int which) {
        ForumPost post = new ForumPost(auth.getUid(), postText.getText().toString(), forumID, date);
        updateLayout(post);
        serialized = ObjectSerializer.serialize(post);
        firestore.collection("forums").document(forumID).get().addOnSuccessListener(this::onSuccessful);
    }

    private void onSuccessful(DocumentSnapshot snapshot) {
        List<String> posts = (ArrayList<String>) snapshot.get("posts");
        if (posts == null) posts = new ArrayList<>();
        posts.add(0, serialized);
        firestore.collection("forums").document(forumID).update("posts", posts);
    }

    public void updateLayout(ForumPost post) {
        if (forumPosts == null)
            forumPosts = new ArrayList<>();
        forumPosts.add(post);
        forumPostRV.scrollToPosition(forumPosts.size() - 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setMessages() {
        firestore.collection("forums")
                .document(forumID).get().addOnSuccessListener(this::onSuccess);
    }

    private void onSuccess(DocumentSnapshot snapshot) {
        ArrayList<String> messages = (ArrayList<String>) snapshot.get("posts");
        forumPosts = new ArrayList<>();
        if (messages != null)
            for (int i = Math.min(messages.size(), MAX_CHAT_MESSAGES_TO_SHOW) - 1; i >= 0; i--)
                forumPosts.add(ObjectSerializer.deserialize(messages.get(i)));
        forumPostRV.setAdapter(new ForumPostAdapter(forumPosts, getContext()));
        forumPostRV.setLayoutManager(new LinearLayoutManager(getContext()));
        forumPostRV.scrollToPosition(forumPosts.size() - 1);
    }
}