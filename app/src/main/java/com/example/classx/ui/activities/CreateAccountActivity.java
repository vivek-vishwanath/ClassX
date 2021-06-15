package com.example.classx.ui.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.classx.R;

import java.util.ArrayList;
import java.util.Arrays;

public class CreateAccountActivity extends AppCompatActivity {

    EditText firstNameET, lastNameET, schoolET;
    String firstName, lastName, schoolName, gradeLevel;
    Spinner gradeSpinner;
    ArrayList<String> grades = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Connecting first name, last name, School, and grade to resource IDS from activity_create_account.xml
        firstNameET = findViewById(R.id.firstNameET);
        lastNameET = findViewById(R.id.lastNameET);
        schoolET = findViewById(R.id.schoolET);
        gradeSpinner = findViewById(R.id.gradeSpinner);

        // Setting grade options to grade spinner
        grades = new ArrayList<>(Arrays.asList("9th Grade", "10th Grade", "11th Grade", "12th Grade", "Freshman (College)", "Sophomore (College)", "Junior (College)", "Senior (College)"));

        //Creates array adapter for spinner
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, grades);

        //Sets array adapter with grades arrayList to the grade spinner
        gradeSpinner.setAdapter(arrayAdapter);
    }

    public void signUp(View view) {
        firstName = firstNameET.getText().toString();
        lastName = lastNameET.getText().toString();
        schoolName = schoolET.getText().toString();
        gradeLevel = gradeSpinner.getSelectedItem().toString();
        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
        intent.putExtra("First Name", firstName);
        intent.putExtra("Last Name", lastName);
        intent.putExtra("School Name", schoolName);
        intent.putExtra("Grade Level" , gradeLevel);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null){
            finish();
        }
    }
}
