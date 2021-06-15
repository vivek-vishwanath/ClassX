package com.example.classx.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.classx.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {

    Button editBioButton, saveBioButton;
    TextView profileUsername, profileName, profileSchool, bioTV;
    EditText bioET;
    ImageView pfp;
    SharedPreferences sharedPreferences;

    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference reference;
    FirebaseFirestore firestore;

    AppCompatActivity activity;

    private String userId;
    private static long MAX_SIZE;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        activity = (AppCompatActivity) requireActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        //Connects objects to resource values for all texts
        editBioButton = root.findViewById(R.id.editBioButton);
        saveBioButton = root.findViewById(R.id.saveButton);
        profileUsername = root.findViewById(R.id.profileUsernameTV);
        profileName = root.findViewById(R.id.profileNameTV);
        profileSchool = root.findViewById(R.id.profileSchoolTV);
        pfp = root.findViewById(R.id.profilePic);
        bioTV = root.findViewById(R.id.bioTV);
        bioET = root.findViewById(R.id.bioET);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();
        firestore = FirebaseFirestore.getInstance();

        userId = auth.getUid();

        //SharedPreferences stores bitmap into byte array
        sharedPreferences = requireContext().getSharedPreferences("com.example.classx.ui.fragments", Context.MODE_PRIVATE);

        String email = sharedPreferences.getString("email", "");
        String firstName = sharedPreferences.getString("first_name", "");
        String lastName = sharedPreferences.getString("last_name", "");
        String bio = sharedPreferences.getString("bio", "");
        String name = firstName + lastName;
        String school = "School: " + sharedPreferences.getString("school_name", "");
        String gradeLevel = "\n" + sharedPreferences.getString("grade_level", "");
        String school_grade = school + gradeLevel;
        profileUsername.setText(email);
        profileName.setText(name);
        profileSchool.setText(school_grade);
        bioTV.setText(bio);
        bioET.setText(bio);

        String pfpString = sharedPreferences.getString("pfp_bytes", null);
        if (pfpString != null) {
            String[] pfpStringArray = pfpString.substring(1, pfpString.length() - 1).split(",");
            byte[] pfpBytes = new byte[pfpStringArray.length];
            for (int i = 0; i < pfpStringArray.length; i++)
                pfpBytes[i] = Byte.parseByte(pfpStringArray[i].substring(1));
            Bitmap bmp = BitmapFactory.decodeByteArray(pfpBytes, 0, pfpBytes.length);
            pfp.setImageBitmap(bmp);
        }

        MAX_SIZE = (long) Math.pow(2, 23);
        Log.wtf("(ProfileFragment.java:76)", "User ID = " + userId);
        reference.child("pfp").child(userId + ".png").getBytes(MAX_SIZE)
                .addOnSuccessListener(this::onSuccess)
                .addOnFailureListener(this::onFailure);


        // Sets parse Data to String
        firestore.collection("users").document(userId).get().addOnCompleteListener(this::onComplete);

        // Sets visibility of bio texts
        bioTV.setVisibility(View.VISIBLE);
        bioET.setVisibility(View.INVISIBLE);

        // When edit/save button is clicked
        editBioButton.setOnClickListener(this::onClick);
        saveBioButton.setOnClickListener(this::onClick);

        //When the photo is clicked, allow the user to select a new one
        pfp.setOnClickListener(this::getPhoto);

        return root;
    }

    public void getPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImage);
                bmp = getCircularBitmap(bmp);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                byte[] bmpData = outputStream.toByteArray();
                reference.child("pfp").child(userId + ".png").putBytes(bmpData);
                pfp.setImageBitmap(bmp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected Bitmap getCircularBitmap(Bitmap bitmap) {
        int squareBitmapWidth = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap dstBitmap = Bitmap.createBitmap(squareBitmapWidth, squareBitmapWidth, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dstBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Rect rect = new Rect(0, 0, squareBitmapWidth, squareBitmapWidth);
        RectF rectF = new RectF(rect);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        float left = (squareBitmapWidth - bitmap.getWidth()) / 2f;
        float top = (squareBitmapWidth - bitmap.getHeight()) / 2f;
        canvas.drawBitmap(bitmap, left, top, paint);
        return dstBitmap;
    }

    protected Bitmap getCircularBitmap(byte[] bytes) {
        return getCircularBitmap(getBitmap(bytes));
    }

    public byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public Bitmap getBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void onClick(View v) {
        if (bioTV.getVisibility() == View.VISIBLE) {
            bioTV.setVisibility(View.INVISIBLE);
            bioET.setVisibility(View.VISIBLE);
            editBioButton.setVisibility(View.INVISIBLE);
            saveBioButton.setVisibility(View.VISIBLE);
        } else {
            bioTV.setVisibility(View.VISIBLE);
            bioET.setVisibility(View.INVISIBLE);
            bioTV.setText(bioET.getText());
            firestore.collection("users").document(userId)
                    .update("bio", bioET.getText().toString());
            editBioButton.setVisibility(View.VISIBLE);
            saveBioButton.setVisibility(View.INVISIBLE);
        }
    }

    private void onSuccess(byte[] bytes) {
        sharedPreferences.edit().putString("pfp_bytes", Arrays.toString(bytes)).apply();
        pfp.setImageBitmap(getCircularBitmap(bytes));
        Log.wtf("(ProfileFragment.java:80)", "Successful Download");
    }

    private void onFailure(Exception exception) {
        reference.child("pfp").child(userId + ".png").getBytes(MAX_SIZE)
                .addOnSuccessListener(this::onSuccess).addOnFailureListener(this::onDoubleFailure);
    }

    private void onDoubleFailure(Exception exception) {
        Log.wtf("(ProfileFragment.java:81)", "Unsuccessful Download", exception);
        BitmapDrawable drawable = (BitmapDrawable) ResourcesCompat.getDrawable(
                getResources(), R.drawable.account_circle_grey, null);
        assert drawable != null;
        byte[] data = getBytes(drawable.getBitmap());
        reference.child("pfp").child(userId + ".png").putBytes(data);
    }

    private void onComplete(Task<DocumentSnapshot> task) {
        Map<String, Object> map = Objects.requireNonNull(task.getResult()).getData();
        assert map != null;
        Log.wtf("(ProfileFragment.java:122)", map.toString());
        //Sets string from parse to textViews
        String fEmail = (String) map.get("email");
        String fFirstName = (String) map.get("first_name");
        String fLastName = (String) map.get("last_name");
        String fBio = (String) map.get("bio");
        String fName = fFirstName + " " + fLastName;
        String fSchool = "School: " + map.get("school_name");
        String fGradeLevel = "\n" + map.get("grade_level");
        String fSchoolGrade = fSchool + fGradeLevel;
        profileUsername.setText(fEmail);
        profileName.setText(fName);
        profileSchool.setText(fSchoolGrade);
        bioTV.setText(fBio);
        bioET.setText(fBio);
        sharedPreferences.edit().putString("profile_email", fEmail).apply();
        sharedPreferences.edit().putString("profile_name", fName).apply();
        sharedPreferences.edit().putString("profile_school_grade", fSchoolGrade).apply();
        sharedPreferences.edit().putString("profile_bio", fBio).apply();
    }

    private void getPhoto(View view) {
        getPhoto();
    }
/*
    public void goToTwitter() {
        if (twitterTV.getText().toString().equals("Add Twitter Account")) {
            Log.i("Social Media", "Twitter");
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            final EditText editText = new EditText(getContext());
            builder.setTitle("Twitter Account")
                    .setMessage("Enter your Twitter account handle:")
                    .setIcon(R.drawable.twitter_logo_2_1)
                    .setView(editText)
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String s = editText.getText().toString();
                            if (s.charAt(0) != '@') {
                                s = "@" + s;
                            }
                            System.out.println(s);
                            ParseUser.getCurrentUser().put("twitter", s);
                            ParseUser.getCurrentUser().saveInBackground();
                            twitterTV.setText(s);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            })
                    .show();
        } else {
            Intent intent = new Intent(getContext(), SocialMediaActivity.class);
            intent.putExtra("handle", twitterTV.getText().toString());
            startActivity(intent);
        }
    }

    public void goToTallo() {
        Log.i("Button", "Clicked Tallo");
        if (talloTV.getText().toString().equals("Add Tallo Account")) {
            Log.i("Social Media", "Tallo");
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            final EditText editText = new EditText(getContext());
            builder.setTitle("Tallo Account")
                    .setMessage("Enter your Tallo account username:")
                    .setIcon(R.drawable.tallo_png)
                    .setView(editText)
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String s = editText.getText().toString();
                            if (s.charAt(0) != '@') {
                                s = "@" + s;
                            }
                            System.out.println(s);
                            ParseUser.getCurrentUser().put("tallo", s);
                            ParseUser.getCurrentUser().saveInBackground();
                            talloTV.setText(s);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            }).show();
        } else {

        }
    }*/
}
