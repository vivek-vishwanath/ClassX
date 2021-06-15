package com.example.classx.ui.messages;

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
import android.widget.Toast;

import com.example.classx.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class FindContactActivity extends AppCompatActivity {

    ListView queryList;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> contactNames;
    ArrayList<String> queryUIDs;
    EditText searchET;
    SharedPreferences sharedPreferences;

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mDatabaseReference;
    FirebaseFirestore mFirebaseFirestore;
    FirebaseStorage mStorage;
    StorageReference mStorageReference;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_contact);

        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
        setTitle("New Conversation");

        //Connects listView and search bar from their resource values to their objects
        queryList = findViewById(R.id.groupQueryList);
        searchET = findViewById(R.id.contactSearchET);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();

        userId = mAuth.getUid();

        //Initializes shared preferences so that info can be moved back to the previous activity
        sharedPreferences = this.getSharedPreferences("com.example.classx.ui.messages", Context.MODE_PRIVATE);

        //Checks if a user from the query list has been clicked on
        queryList.setOnItemClickListener((adapterView, view, i, l) -> {

            //Saves contact name and bio in shared preferences for previous fragment
            sharedPreferences.edit().putString("Contact Name", contactNames.get(i)).apply();

            mFirebaseFirestore.collection("users").document(userId).get().addOnCompleteListener(task -> {
                ArrayList<String> contacts;
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Object o = document.getData().get("contactUIDs");
                        if (o != null) {
                            contacts = (ArrayList<String>) o;
                        } else {
                            contacts = new ArrayList<>();
                        }
                        Log.wtf("(FindContactActivity.java:89)", "DocumentSnapshot data: " + document.getData().get("contactUIDs"));
                    } else {
                        contacts = new ArrayList<>();
                        Log.wtf("(FindContactActivity.java:92)", "No such document");
                    }
                } else {
                    contacts = new ArrayList<>();
                    Log.wtf("(FindContactActivity.java:96)", "get failed with ", task.getException());
                }
                Log.wtf("(FindContactActivity.java:98)", contacts.toString());
                Log.wtf("(FindContactActivity.java:99)", queryUIDs.get(i));
                contacts.add(queryUIDs.get(i));
                Log.wtf("(FindContactActivity.java:101)", contacts.toString());
                mFirebaseFirestore.collection("users").document(userId).update("contactUIDs", contacts);
                Toast.makeText(getApplicationContext(), "Contact Added", Toast.LENGTH_SHORT).show();

                // Sets Result for startActivityResult method so that the recyclerview from
                // Messages fragment can be updated when the back button is pressed
                setResult(RESULT_OK);

                //Goes Back to MessagesFragment
                onBackPressed();
            });

            mFirebaseFirestore.collection("users").document(queryUIDs.get(i)).get().addOnCompleteListener(task -> {
                ArrayList<String> contacts;
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Object o = document.getData().get("contactUIDs");
                        if (o != null) {
                            contacts = (ArrayList<String>) o;
                        } else {
                            contacts = new ArrayList<>();
                        }
                        Log.wtf("(FindContactActivity.java:122)", "DocumentSnapshot data: " + document.getData().get("contactUIDs"));
                    } else {
                        contacts = new ArrayList<>();
                        Log.wtf("(FindContactActivity.java:125)", "No such document");
                    }
                } else {
                    contacts = new ArrayList<>();
                    Log.wtf("(FindContactActivity.java:129)", "get failed with ", task.getException());
                }
                Log.wtf("(FindContactActivity.java:131)", contacts.toString());
                contacts.add(userId);
                Log.wtf("(FindContactActivity.java:133)", contacts.toString());
                mFirebaseFirestore.collection("users").document(queryUIDs.get(i)).update("contactUIDs", contacts);
                Toast.makeText(getApplicationContext(), "Contact Added", Toast.LENGTH_SHORT).show();

                // Sets Result for startActivityResult method so that the recyclerview from
                // Messages fragment can be updated when the back button is pressed
                setResult(RESULT_OK);

                //Goes Back to MessagesFragment
                onBackPressed();
            });
        });
    }

    public void findQuery(View view) {
        //Creates ArrayLists to store contact names and ParseUsers
        contactNames = new ArrayList<>();
        queryUIDs = new ArrayList<>();

        mFirebaseFirestore.collection("users")
                .whereNotEqualTo("email", mAuth.getCurrentUser().getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String email = (String) document.getData().get("email");
                            String uid = (String) document.getData().get("uid");
                            if (containsString(email, searchET.getText().toString())) {
                                contactNames.add(email);
                                queryUIDs.add(uid);
                            }
                            Log.wtf("(FindContactActivity.java:149)", document.getId() + " => " + document.getData());
                        }

                        //Constructs adapter for listView of users
                        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, contactNames) {
                            @Override
                            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                                // Get the Item from ListView
                                View view = super.getView(position, convertView, parent);

                                // Initialize a TextView for ListView each Item
                                TextView tv = view.findViewById(android.R.id.text1);

                                // Set the text color of TextView (ListView Item)
                                tv.setTextColor(getResources().getColor(R.color.text_color));
                                tv.setTextSize(24);

                                // Generate ListView Item using TextView
                                return view;
                            }
                        };
                        //Sets the constructed adapter to the listView of users
                        queryList.setAdapter(arrayAdapter);
                    } else {
                        Log.wtf("(FindContactActivity.java:172)", "Error getting documents: ", task.getException());
                    }
                });
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
}