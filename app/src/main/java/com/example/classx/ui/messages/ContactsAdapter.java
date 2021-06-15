package com.example.classx.ui.messages;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classx.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    FirebaseFirestore firestore;
    FirebaseStorage storage;
    StorageReference reference;
    FirebaseAuth auth;
    String userId;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView bioTextView;
        public ImageView profileImageView;
        private final View itemView;


        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            nameTextView = itemView.findViewById(R.id.usernameTV);
            bioTextView = itemView.findViewById(R.id.user_emailTV);
            profileImageView = itemView.findViewById(R.id.pfp_imageView);
        }

        public View getItemView() {
            return itemView;
        }
    }

    private final List<Contact> mContacts;
    ViewHolder viewHolder;
    static ArrayList<ViewHolder> holders;
    Context context;

    public ContactsAdapter(List<String> contactSer, Context context) {
        mContacts = new ArrayList<>();
        for (String s : contactSer) {
            mContacts.add(new Contact(s));
        }
        this.context = context;
        holders = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.contact_row, parent, false);

        // Return a new holder instance
        viewHolder = new ViewHolder(contactView);
        holders.add(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ContactsAdapter.ViewHolder holder, int position) {
        Contact contact = mContacts.get(position);

        // Set item views based on your views and data model
        TextView name = holder.nameTextView;
        TextView bio = holder.bioTextView;
        ImageView profileImage = holder.profileImageView;

        DocumentReference ref = firestore.collection("users").document(contact.getUid());

        Log.wtf("(ContactsAdapter.java:121)", contact.getUid());

        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    name.setText(document.getString("first_name") + " " + document.getString("last_name"));
                    bio.setText(document.getString("bio"));
                    Log.wtf("(ContactsAdapter.java:54)", "name = " + name.getText() + ", bio = " + bio.getText());
                }
            } else
                Log.wtf("(ContactsAdapter.java:116)", "get failed with ", task.getException());
        });

        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();
        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();

        final long MAX_SIZE = (long) Math.pow(2, 23);
        Log.wtf("(ContactsAdapter.java:138)", "User ID = " + userId);
        reference.child("pfp").child(contact.getUid() + ".png").getBytes(MAX_SIZE)
                .addOnSuccessListener(bytes -> {
                    profileImage.setImageBitmap(getCircularBitmap(bytes));
                    Log.wtf("(ContactsAdapter.java:145)", "Successful Download");
                }).addOnFailureListener(exception ->
                    reference.child("pfp").child(contact.getUid() + ".png").getBytes(MAX_SIZE)
                        .addOnSuccessListener(bytes -> profileImage.setImageBitmap(getCircularBitmap(bytes)))
                        .addOnFailureListener(exception2 -> {
                            Log.wtf("(ContactsAdapter.java:149)", "Unsuccessful Download", exception);
                            BitmapDrawable drawable = (BitmapDrawable) ResourcesCompat.getDrawable(
                                    context.getResources(), R.drawable.account_circle_grey, null);
                            assert drawable != null;
                            byte[] data = getBytes(drawable.getBitmap());
                            profileImage.setImageBitmap(drawable.getBitmap());
                            reference.child("pfp").child(contact.getUid() + ".png").putBytes(data);
                        }));
    }

    public byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public Bitmap getBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    protected Bitmap getCircularBitmap(byte[] bytes) {
        Bitmap bitmap = getBitmap(bytes);
        int squareBitmapWidth = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap dstBitmap = Bitmap.createBitmap(
                squareBitmapWidth, // Width
                squareBitmapWidth, // Height
                Bitmap.Config.ARGB_8888 // Config
        );
        Canvas canvas = new Canvas(dstBitmap);
        // Initialize a new Paint instance
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Rect rect = new Rect(0, 0, squareBitmapWidth, squareBitmapWidth);
        RectF rectF = new RectF(rect);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        // Calculate the left and top of copied bitmap
        float left = (squareBitmapWidth - bitmap.getWidth()) / 2;
        float top = (squareBitmapWidth - bitmap.getHeight()) / 2;
        canvas.drawBitmap(bitmap, left, top, paint);
        // Free the native object associated with this bitmap.
        bitmap.recycle();
        // Return the circular bitmap
        return dstBitmap;
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }
}