package com.example.classx.ui.forums.navigation.calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.classx.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddEventActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    EditText nameET, descriptionET;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    static Button pickDateButton, pickTimeButton;
    Intent intent;
    String forumID;
    static DateTime dateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        nameET = findViewById(R.id.eventNameEditText);
        descriptionET = findViewById(R.id.descriptionEditText);
        intent = getIntent();
        forumID = intent.getStringExtra("ForumID");
        sharedPreferences = this.getSharedPreferences("com.example.classx.ui.forums.navigation.calendar", Context.MODE_PRIVATE);
        Log.wtf("(AddEventActivity.java:50)", intent.getStringExtra("ForumID"));

        dateTime = new DateTime();

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        pickDateButton = findViewById(R.id.pickDateButton);
        pickTimeButton = findViewById(R.id.pickTimeButton);
    }

    public void createEvent(View view) {
        // Gets input values and stores them
        String name = nameET.getText().toString();
        String description = descriptionET.getText().toString();
        Date date = dateTime.getDate();

        // Generates ID and Constructs Object
        String eventID = UUID.randomUUID().toString();
        Event event = new Event(name, description, date, eventID, forumID);

        // Serializes event into string and stores it in a map
        String serialized = ObjectSerializer.serialize(event);
        Map<String, Object> map = new HashMap<>();
        map.put("event", serialized);

        // Accesses Firestore database and stores the event
        firestore.collection("forums").document(forumID)
                .collection("events").document(eventID).set(map)
                .addOnCompleteListener(task -> finish());
    }

//        sharedPreferences.edit().putString("serialized_event", serialized).apply();
    private void onComplete(Task<Void> task) {
        if(task.isSuccessful()){
            Log.wtf("(AddEventActivity.java:77)", "Event Uploaded to Firestore");
        } else {
            Log.wtf("(AddEventActivity.java:77)", task.getException());
        }
        setResult(RESULT_OK);
        onBackPressed();
    }

    public void pickDate(View view) {
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void pickTime(View view) {
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.show(getSupportFragmentManager(), "timePicker");
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            dateTime.hour = hourOfDay;
            dateTime.minute = minute;

            boolean am = false;
            if (hourOfDay < 12) am = true;
            hourOfDay %= 12;
            if (hourOfDay == 0) hourOfDay = 12;
            String hour = String.valueOf(hourOfDay);
            String min = String.valueOf(minute);
            if (hour.length() == 1) hour = "0" + hour;
            if (min.length() == 1) min = "0" + min;
            if (am) min += " AM";
            else min += " PM";
            pickTimeButton.setText(hour + ":" + min);
            pickTimeButton.setBackgroundColor(Color.GREEN);
        }
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            dateTime.year = year;
            dateTime.month = month;
            dateTime.day = day;

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            dateTime.day = day;
            dateTime.month = month;
            dateTime.year = year;
            String debug = "year = " + (dateTime.year - 1900) + ", month = " + dateTime.month + ", day = "
                    + dateTime.day + ", hour = " + dateTime.hour;
            Log.wtf("(AddEventActivity.java:168)", debug);
            pickDateButton.setText((month + 1) + "/" + day + "/" + year);
            pickDateButton.setBackgroundColor(Color.GREEN);
        }
    }

}