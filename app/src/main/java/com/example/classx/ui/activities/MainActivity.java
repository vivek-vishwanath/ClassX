package com.example.classx.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.classx.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class MainActivity extends AppCompatActivity {

    EditText usernameET, passwordET;
    String username, password;

    FirebaseAuth mAuth;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connecting username and password to resource IDS from activity_main.xml
        usernameET = findViewById(R.id.usernameET);
        passwordET = findViewById(R.id.passwordET);

        mAuth = FirebaseAuth.getInstance();

        Log.wtf("(MainActivity.java:52)", "MainActivity.onCreate() called");

        if(mAuth.getCurrentUser() != null){
            launchActivity(BottomNavigationActivity.class);
        }
    }


    public void signIn(View view){
        username = usernameET.getText().toString();
        password = passwordET.getText().toString();
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()){
                        Log.wtf("(MainActivity.java:64)", "Successful");
                        Toast.makeText(getApplicationContext(), "You are signed into \"" + mAuth.getCurrentUser().getEmail() + "\"", Toast.LENGTH_LONG).show();
                        launchActivity(BottomNavigationActivity.class);
                    } else {
                        Log.wtf("(MainActivity.java:68)", "Unsuccessful", task.getException());
                        Toast.makeText(getApplicationContext(), "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void createAccount(View view){
        launchActivity(CreateAccountActivity.class);
    }

    public void launchActivity(Class c){
        Intent intent = new Intent(getApplicationContext(), c);
        startActivityForResult(intent, 1);
    }

    public void goToPrivacyPolicy(View view){
        launchActivity(PrivacyPolicyActivity.class);
    }
}