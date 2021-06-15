package com.example.classx.ui.forums.navigation.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classx.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    ArrayList<String> userIDs;
    Context context;
    String forumID;

    FirebaseFirestore firestore;
    FirebaseStorage storage;
    StorageReference reference;
    private static final long MAX_SIZE = (long) Math.pow(2,23);

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.user_row, parent, false);

        Log.wtf("(UserAdapter.java:49)","");

        int[] attrs = new int[]{R.attr.selectableItemBackground};
        TypedArray typedArray = context.obtainStyledAttributes(attrs);
        int backgroundResource = typedArray.getResourceId(0, 0);
        view.setBackgroundResource(backgroundResource);

        return new ViewHolder(view);
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView nameTextView = holder.nameTV;
        TextView emailTextView = holder.emailTV;
        ImageView pfpImageView = holder.pfpIV;

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();
        firestore.collection("users").document(userIDs.get(position)).get().addOnSuccessListener(task2 -> {
            String username = task2.getString("first_name") + " " + task2.getString("last_name");
            String email = task2.getString("email");

            nameTextView.setText(username);
            emailTextView.setText(email);
            Log.wtf("(UserAdapter.java:81)", "Name = " + username);
            Log.wtf("(UserAdapter.java:82)", "Email = " + email);
        });
        reference.child("pfp").child(userIDs.get(position) + ".png").getBytes(MAX_SIZE)
                .addOnSuccessListener(bytes -> pfpImageView.setImageBitmap(getBitmap(bytes)))
                .addOnFailureListener(exception -> pfpImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.account_circle_grey)));
    }

    public Bitmap getBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public int getItemCount() {
        Log.wtf("(UserAdapter.java:103)", String.valueOf(userIDs.size()));
        return userIDs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTV;
        private final TextView emailTV;
        private final ImageView pfpIV;
        private final View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.nameTV = itemView.findViewById(R.id.usernameTV);
            this.emailTV = itemView.findViewById(R.id.user_emailTV);
            this.pfpIV = itemView.findViewById(R.id.pfp_imageView);
            Log.wtf("(UserAdapter.java:120)", "");
        }

        public View getItemView(){return itemView;}
    }

    public UsersAdapter(Context context, ArrayList<String> userIDs, String forumID) {
        Log.wtf("(UserAdapter.java:127)", "");
        this.userIDs = userIDs;
        this.context = context;
        this.forumID = forumID;
        Log.wtf("(UserAdapter.java:131)", "");
    }
}
