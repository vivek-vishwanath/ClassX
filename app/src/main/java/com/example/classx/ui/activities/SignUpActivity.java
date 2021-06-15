package com.example.classx.ui.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.classx.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    EditText usernameET, passwordET, confirmPasswordET;
    String username, password, confirmPassword;

    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference reference;
    FirebaseFirestore firestore;

    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Connecting username, password, and confirm password to resource IDS from activity_create_account.xml
        usernameET = findViewById(R.id.newUsernameET);
        passwordET = findViewById(R.id.newPasswordET);
        confirmPasswordET = findViewById(R.id.confirmNewPasswordET);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();
        firestore = FirebaseFirestore.getInstance();
    }

    public void signUp(View view) {
        //Gets Intent from Create Account Activity
        Intent intent = getIntent();

        //Gets extras from CreateAccountActivity for First Name, Last Name, and School Name.
        String firstName = intent.getStringExtra("First Name");
        String lastName = intent.getStringExtra("Last Name");
        String schoolName = intent.getStringExtra("School Name");
        String gradeLevel = intent.getStringExtra("Grade Level");

        //Stores the values from the editTexts of username, password, and confirm password as strings.
        username = usernameET.getText().toString();
        password = passwordET.getText().toString();
        confirmPassword = confirmPasswordET.getText().toString();

        //Checks if password and confirm password are the same
        if (!password.equals(confirmPassword)) {
            //Makes toast message for user and breaks out from the method.
            Log.wtf("(SignUpActivity.java:77)", "Passwords do NOT match");
            Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(this,
                task -> {
                    if (task.isSuccessful()) {
                        Map<String, String> user = new HashMap<>();
                        userID = task.getResult().getUser().getUid();
                        user.put("email", username);
                        user.put("first_name", firstName);
                        user.put("last_name", lastName);
                        user.put("school_name", schoolName);
                        user.put("bio", "Hi, I'm using ClassX");
                        user.put("grade_level", gradeLevel);
                        user.put("uid", userID);

                        firestore.collection("users").document(task.getResult().getUser().getUid()).set(user)
                                .addOnSuccessListener(aVoid -> Log.wtf("(SignUpActivity.java:114)", "User Added!"))
                                .addOnFailureListener(aVoid -> Log.wtf("(SignUpActivity.java:114)", "Error adding user :(", aVoid));

                        // Create Profile
                        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.account_circle_grey);
                        Bitmap bmp = drawable.getBitmap();
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        byte[] data = outputStream.toByteArray();
                        reference.child("pfp").child(userID + ".png").putBytes(data).addOnCompleteListener(task2 -> {
                            launchActivity(BottomNavigationActivity.class);
                        });


                    } else {
                        Log.wtf("(SignUpActivity.java:119)", "Unsuccessful", task.getException());
                    }
                });
    }

    public void launchActivity(Class c) {
        Intent intent = new Intent(getApplicationContext(), c);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            setResult(RESULT_OK);
            finish();
        }
    }

    public Bitmap getBitmap(ImageView imageView) {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        return drawable.getBitmap();
    }
}