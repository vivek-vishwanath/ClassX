package com.example.classx.ui.forums.navigation.thread;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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

import androidx.recyclerview.widget.RecyclerView;

import com.example.classx.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ForumPostAdapter extends RecyclerView.Adapter<ForumPostAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView usernameTextView;
        public TextView messageTextView;
        public TextView timeTextView;
        public ImageView pfp;
        private final View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            usernameTextView = itemView.findViewById(R.id.senderUsername);
            messageTextView = itemView.findViewById(R.id.postText);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            pfp = itemView.findViewById(R.id.postUserPFP);
        }

        public View getItemView() {
            return itemView;
        }
    }

    private final List<ForumPost> forumPosts;
    private final Context context;
    ForumPostAdapter.ViewHolder viewHolder;
    static ArrayList<ForumPostAdapter.ViewHolder> holders;

    public ForumPostAdapter(List<ForumPost> forumPosts, Context context) {
        this.forumPosts = forumPosts;
        holders = new ArrayList<>();
        this.context = context;
    }

    @Override
    public ForumPostAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View forumPost = inflater.inflate(R.layout.forum_post_row, parent, false);

        // Return a new holder instance
        viewHolder = new ForumPostAdapter.ViewHolder(forumPost);
        holders.add(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ForumPostAdapter.ViewHolder holder, int position) {
        ForumPost forumPost = forumPosts.get(position);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String uid = forumPost.getUid();
        StorageReference reference = storage.getReference().child("pfp");

        Log.wtf("(ForumPostAdapter.java:87", "");
        // Set item views based on your views and data model
        TextView username = holder.usernameTextView;
        firestore.collection("users").document(uid).get().addOnCompleteListener(task -> {
            Log.wtf("(ForumPostAdapter.java:91", "Complete");
            if (task.isSuccessful()) {
                String firstName = (String) task.getResult().getData().get("first_name");
                String lastName = (String) task.getResult().getData().get("last_name");
                Log.wtf("(ForumPostAdapter.java:93", firstName + " " + lastName);
                username.setText(firstName + " " + lastName);
            } else {
                Log.wtf("(ForumPostAdapter.java:96)", "User Task not Found", task.getException());
            }
        });


        final long MAX_SIZE = (long) Math.pow(2, 23);
        reference.child(uid + ".png").getBytes(MAX_SIZE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            holder.pfp.setImageBitmap(getCircularBitmap(bitmap));
        }).addOnFailureListener(exception ->
                reference.child(uid + ".png").getBytes(MAX_SIZE)
                        .addOnSuccessListener(bytes -> holder.pfp.setImageBitmap(getCircularBitmap(getBitmap(bytes))))
                        .addOnFailureListener(exception2 -> {
                            Log.wtf("(ForumPostAdapter.java:106)", "Unsuccessful Download", exception);
                            BitmapDrawable drawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.account_circle_grey);
                            Bitmap bitmap = drawable.getBitmap();
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                            byte[] data = outputStream.toByteArray();
                            reference.child("pfp").child(uid + ".png").putBytes(data);
                        }));

        TextView messageTextView = holder.messageTextView;
        TextView timeTextView = holder.timeTextView;
        messageTextView.setText(forumPost.getMessage());
        timeTextView.setText(forumPost.getTime());

    }

    public List<ForumPost> getForumPosts() {
        return forumPosts;
    }

    @Override
    public int getItemCount() {
        return forumPosts.size();
    }

    public Bitmap getBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    protected Bitmap getCircularBitmap(Bitmap bitmap) {
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
}