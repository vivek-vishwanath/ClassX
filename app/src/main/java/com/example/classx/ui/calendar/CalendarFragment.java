package com.example.classx.ui.calendar;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classx.R;
import com.example.classx.ui.forums.navigation.calendar.Event;
import com.example.classx.ui.forums.navigation.calendar.ObjectSerializer;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CalendarFragment extends Fragment {

    FirebaseAuth auth;
    FirebaseFirestore firestore;

    RecyclerView eventsRecyclerView;
    EventAdapter adapter;

    String uid;
    ArrayList<String> serializedEvents;
    List<Event> eventsList;
    List<Date> dates;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendar, container, false);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        ActionBar actionBar = activity.getSupportActionBar();

        if (actionBar != null) {
            actionBar.show();
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.app_theme_blue)));
        }
        activity.setTitle("Your Calendar");

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        if (auth.getUid() == null) requireActivity().onBackPressed();
        uid = auth.getUid();

        serializedEvents = new ArrayList<>();
        eventsList = new ArrayList<>();
        eventsRecyclerView = root.findViewById(R.id.personalCalendarRV);

        firestore.collection("forums").get().addOnSuccessListener(this::onSuccess);

        return root;
    }

    private void onSuccess(QuerySnapshot snapshots) {
        for (QueryDocumentSnapshot document : snapshots) {
            ArrayList<String> users = ((ArrayList<String>) document.get("users"));
            if (users != null && users.contains(uid))
                document.getReference().collection("events")
                        .get().addOnCompleteListener(this::onCompleteEvents);
        }
    }

    private void onCompleteEvents(Task<QuerySnapshot> task) {
        serializedEvents = new ArrayList<>();
        if (task.isSuccessful() && task.getResult() != null)
            for (QueryDocumentSnapshot document : task.getResult()) {
                String event = document.getString("event");
                if (event != null) serializedEvents.add(event);
            }
        deserializeEvents();
    }

    private void deserializeEvents() {
        for (String s : serializedEvents)
            eventsList.add(ObjectSerializer.deserialize(s));
        eventsList = sortEvents(eventsList);
        setDates();
    }

    private List<Event> sortEvents(List<Event> events) {
        if (events.size() == 0) return events;
        ArrayList<Event> list = new ArrayList<>();
        list.add(events.remove(0));
        loop:
        for (int i = 0; i < events.size(); ) {
            for (int j = 0; j < list.size(); j++) {
                if (list.get(j).getDate().after(events.get(i).getDate())) {
                    list.add(j, events.remove(i));
                    continue loop;
                }
            }
            list.add(events.remove(i));
        }
        return list;
    }

    private void setDates() {
        dates = new ArrayList<>();
        for (Event event : eventsList) {
            Date date = new Date(event.getDate().getYear(), event.getDate().getMonth(), event.getDate().getDate());
            if (!dates.contains(date))
                dates.add(date);
        }
        Log.wtf("(CalendarFragment.java:121)", dates.toString());
        updateRecyclerView();
    }

    private void updateRecyclerView() {
        adapter = new EventAdapter(eventsList);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventsRecyclerView.setAdapter(adapter);
    }

}