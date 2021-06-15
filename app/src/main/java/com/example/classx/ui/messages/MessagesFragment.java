package com.example.classx.ui.messages;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classx.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MessagesFragment extends Fragment {

    ArrayList<String> contacts;
    SharedPreferences sharedPreferences;
    RecyclerView contactsRV;

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mDatabaseReference;
    FirebaseFirestore mFirebaseFirestore;
    FirebaseStorage mStorage;
    StorageReference mStorageReference;

    AppCompatActivity activity;
    private String userId;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_messages, container, false);

        activity = (AppCompatActivity) requireActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.app_theme_blue)));
        }
        activity.setTitle("Messages");

        sharedPreferences = requireContext().getSharedPreferences("com.example.classx.ui.messages", Context.MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();

        userId = mAuth.getUid();
        if(userId == null) getActivity().onBackPressed();

        FloatingActionButton fab = root.findViewById(R.id.addContactButton);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), FindContactActivity.class);
            startActivityForResult(intent, 1);
        });
        contactsRV = root.findViewById(R.id.contactsRV);

        updateCollection();
        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        updateCollection();
    }

    public void updateCollection(){
        mFirebaseFirestore.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    contacts = (ArrayList<String>) document.get("contactUIDs");
                    if(contacts == null){
                        contacts = new ArrayList<>();
                    }
                    Log.wtf("(MessagesFragment.java:91)", "DocumentSnapshot data: " + contacts);
                } else {
                    contacts = new ArrayList<>();
                    Log.wtf("(MessagesFragment.java:94)", "No such document");
                }
            } else {
                contacts = new ArrayList<>();
                Log.wtf("(MessagesFragment.java:98)", "get failed with ", task.getException());
            }

            ContactsAdapter adapter = new ContactsAdapter(contacts, getContext());
            contactsRV.setAdapter(adapter);
            contactsRV.setLayoutManager(new LinearLayoutManager(getContext()));
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                for(int i = 0; i < ContactsAdapter.holders.size(); i++) {
                    final int finalI = i;
                    ContactsAdapter.holders.get(i).getItemView().setOnClickListener(view -> {
                        Intent intent = new Intent(getContext(), ChatActivity.class);
                        intent.putExtra("Contact Name", ContactsAdapter.holders.get(finalI).nameTextView.getText().toString());
                        intent.putExtra("Contact UID", contacts.get(finalI));
                        startActivity(intent);
                        requireActivity().overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    });
                }
            }, 1000);
        });
    }
}
