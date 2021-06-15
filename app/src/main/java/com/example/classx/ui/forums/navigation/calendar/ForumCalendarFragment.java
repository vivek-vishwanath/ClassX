package com.example.classx.ui.forums.navigation.calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classx.R;
import com.example.classx.ui.calendar.EventAdapter;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ForumCalendarFragment extends Fragment {

    RecyclerView recyclerView;
    FloatingActionButton addEventButton;
    List<Event> events;
    EventAdapter adapter;

    SharedPreferences sharedPreferences;
    Intent intent;
    String forumID;

    FirebaseFirestore firestore;

    private void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) {
        if (e == null && snapshot != null)
            for (QueryDocumentSnapshot doc : snapshot) {
                Event event = ObjectSerializer.deserialize(doc.getString("event"));
                if (event != null)
                    for (int i = 0; i < events.size(); i++)
                        if (events.get(i).getDate().after(event.getDate())) {
                            events.add(i, event);
                            break;
                        } else
                            Log.wtf("(ForumCalendarFragment.java:56)", "Event deserialization failed");
            }
        else
            Log.wtf("(ForumCalendarFragment.java:56)", "Event deserialization failed");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_forum_calendar, container, false);

        recyclerView = root.findViewById(R.id.forumCalendarRV);
        addEventButton = root.findViewById(R.id.addEventButton);
        events = new ArrayList<>();

        sharedPreferences = getActivity().getSharedPreferences("com.example.classx.ui.forums.navigation.calendar", Context.MODE_PRIVATE);
        intent = getActivity().getIntent();
        forumID = intent.getStringExtra("ForumID");

        firestore = FirebaseFirestore.getInstance();
        updateLayout();

        addEventButton.setOnClickListener(this::onClick);

        firestore.collection("forums").document(forumID)
                .collection("events").addSnapshotListener(this::onEvent);

        adapter = new EventAdapter(events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return root;
    }

    private void onClick(View view) {
        Intent intent = new Intent(getContext(), AddEventActivity.class);
        intent.putExtra("ForumID", forumID);
        if (getActivity() != null)
            getActivity().setResult(Activity.RESULT_OK);
        startActivityForResult(intent, 1);
    }

    private void onComplete(Task<QuerySnapshot> task) {
        if (task.isSuccessful() && task.getResult() != null) {
            for (QueryDocumentSnapshot document : task.getResult()) {
                String serialized = (String) document.get("event");
                Event event = ObjectSerializer.deserialize(serialized);
                Log.wtf("(ForumCalendarFragment.java:103)", event.toString());
                events.add(event);
            }
            Log.wtf("(ForumCalendarFragment.java:106)", events.toString());
            events = sort(events);
            Log.wtf("(ForumCalendarFragment.java:108)", events.toString());
            adapter = new EventAdapter(events);
            recyclerView.setAdapter(adapter);
        } else {
            Log.wtf("(ForumCalendarFragment.java:112)", "Unsuccessful Event Task");
        }
    }

    private List<Event> sort(List<Event> events) {
        if(events.size() == 0) return events;
        ArrayList<Event> list = new ArrayList<>();
        list.add(events.remove(0));
        loop:
        for(int i = 0; i < events.size();){
            for(int j = 0; j < list.size(); j++){
                if(list.get(j).getDate().after(events.get(i).getDate())){
                    list.add(j, events.remove(i));
                    Log.wtf("(ForumCalendarFragment.java:123)", list.toString());
                    continue loop;
                }
            }
            list.add(events.remove(i));
        }
        Log.wtf("(ForumCalendarFragment.java:129)", events.toString());
        events = new ArrayList<>();
        events.addAll(list);
        Log.wtf("(ForumCalendarFragment.java:132)", events.toString());
        return events;
    }

    private void updateLayout() {
        firestore.collection("forums").document(forumID)
                .collection("events").get().addOnCompleteListener(this::onComplete);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        events = new ArrayList<>();
        updateLayout();
    }
}
