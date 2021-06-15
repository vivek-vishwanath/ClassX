package com.example.classx.ui.calendar;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.classx.DrawableBackground;
import com.example.classx.R;
import com.example.classx.ui.forums.fragment.ForumsAdapter;
import com.example.classx.ui.forums.navigation.calendar.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    FirebaseFirestore firestore;
    private static ArrayList<DrawableBackground> sideBarDrawables;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView eventNameTextView;
        public TextView eventTimeTextView;
        public View colorView;
        private final View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            eventNameTextView = itemView.findViewById(R.id.eventTitle);
            eventTimeTextView = itemView.findViewById(R.id.eventTime);
            colorView = itemView.findViewById(R.id.colorView);
        }

        public View getItemView(){
            return itemView;
        }
    }

    private final List<Event> events;
    EventAdapter.ViewHolder viewHolder;
    static ArrayList<EventAdapter.ViewHolder> holders;

    public EventAdapter(List<Event> events){
        this.events = new ArrayList<>();
        this.events.addAll(events);
        holders = new ArrayList<>();
        sideBarDrawables = ForumsAdapter.setSideBarDrawables();
    }

    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View event = inflater.inflate(R.layout.event_row, parent, false);

        // Return a new holder instance
        viewHolder = new ViewHolder(event);
        holders.add(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(EventAdapter.ViewHolder holder, int position) {
        Event event = events.get(position);
        View colorView = holder.colorView;

        firestore = FirebaseFirestore.getInstance();
        Log.wtf("(EventAdapter.java:88)", event.getForumID());
        firestore.collection("forums").document(event.getForumID()).get().addOnSuccessListener(task -> {
            String type = task.getString("type");
            if(type == null) type = "Other";
            Log.wtf("(EventAdapter.java:92)", type);
            colorView.setBackground(getSideDrawable(type));
        }).addOnFailureListener(task -> Log.wtf("(EventAdapter.java:93)", "Document Not Found"));

        holder.eventNameTextView.setText(event.getName());
        holder.eventTimeTextView.setText(event.getDate().toString().substring(0, 16));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public DrawableBackground getSideDrawable(String type) {
        switch (type) {
            case "Class":
                return sideBarDrawables.get(0);
            case "Club":
                return sideBarDrawables.get(1);
            case "Team":
                return sideBarDrawables.get(2);
            case "Study Group":
                return sideBarDrawables.get(3);
            case "Interest Group":
                return sideBarDrawables.get(4);
            default:
                return sideBarDrawables.get(5);
        }
    }
}

