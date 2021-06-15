package com.example.classx.ui.messages;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.classx.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ChatActivity extends AppCompatActivity {

    //Send Icon Button
    ImageView send;

    //Message Edit Text
    EditText messageET;

    //Intent to get the other user
    Intent intent;

    LinearLayout layout;
    ScrollView scrollView;

    //Stores the sender & recipient for the ParseObject, "Message"
    String sender, recipient;

    //Number of most recent messages
    static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;

    FirebaseAuth mAuth;
    FirebaseFirestore mFirebaseFirestore;

    boolean startingUp = true;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Getting intent from the previous activity
        intent = getIntent();

        //Setting the title to the other user (who is texting with the current user)
        setTitle(intent.getStringExtra("Contact Name"));

        //Sets sender & Recipient
        mAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        sender = mAuth.getUid();
        recipient = intent.getStringExtra("Contact UID");

        // Connects variables to the XML page
        send = findViewById(R.id.sendMessageIcon);
        messageET = findViewById(R.id.messageET);
        layout = findViewById(R.id.linearLayout);
        scrollView = findViewById(R.id.chatScrollView);

        for (int i = 0; i < 5; i++)
            layout.addView(new View(this));

        setMessages();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void sendText(View view) {
        Message message = new Message(messageET.getText().toString(), sender, recipient);
        String serialized = ObjectSerializer.serialize(message);
        mFirebaseFirestore
                .collection("users").document(sender)
                .collection("chats").document(recipient)
                .get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                ArrayList<String> messages = (ArrayList<String>) snapshot.get("messages");
                if (messages == null)
                    messages = new ArrayList<>();
                messages.add(serialized);
                messageET.setText("");
                updateLayout(message);
                mFirebaseFirestore.collection("users").document(sender)
                        .collection("chats").document(recipient).update("messages", messages);
            } else {
                Map<String, Object> data = new HashMap<>();
                ArrayList<String> messages = new ArrayList<>();
                messages.add(serialized);
                messageET.setText("");
                data.put("messages", messages);
                updateLayout(message);
                mFirebaseFirestore.collection("users").document(sender)
                        .collection("chats").document(recipient).set(data);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void updateLayout(Message message) {
        TextView textView = new TextView(getApplicationContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textView.setText(message.text);
        textView.setTextSize(24);
        textView.setLayoutParams(params);
        textView.setWidth(getTextViewWidth(message));
        textView.setTextColor(Color.WHITE);
        textView.setPadding(8, 8, 8, 8);
        if (message.senderID.equals(sender)) {
            textView.setPadding(32, 0, 16, 0);
            params.gravity = LinearLayout.TEXT_ALIGNMENT_VIEW_START;
            textView.setBackgroundResource(R.drawable.rounded_blue);
        } else {
            textView.setPadding(32, 0, 0, 0);
            params.gravity = LinearLayout.TEXT_ALIGNMENT_VIEW_END;
            textView.setBackgroundResource(R.drawable.rounded_gray);
        }

        for (int i = 0; i < 5; i++)
            layout.removeViewAt(layout.getChildCount() - 1);

        layout.addView(textView);
        layout.addView(new TextView(getApplicationContext()));

        for (int i = 0; i < 5; i++)
            layout.addView(new View(getApplicationContext()));

        scrollView.post(() -> {
            scrollView.fullScroll(View.FOCUS_DOWN);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setMessages() {
        AtomicReference<List<String>> sent = new AtomicReference<>();
        AtomicReference<List<String>> received = new AtomicReference<>();

        mFirebaseFirestore.collection("users").document(sender)
                .collection("chats").document(recipient)
                .get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                ArrayList<String> arrayList = ((ArrayList<String>) snapshot.get("messages"));
                if (arrayList == null) arrayList = new ArrayList<>();
                LinkedList<String> linkedList = new LinkedList<>(arrayList);
                sent.set(linkedList);
                mFirebaseFirestore.collection("users").document(recipient)
                        .collection("chats").document(sender).get()
                        .addOnSuccessListener(snapshotTask -> {
                            if (snapshotTask.exists()) {
                                ArrayList<String> arrayList1 = ((ArrayList<String>) snapshotTask.get("messages"));
                                if (arrayList1 == null) arrayList1 = new ArrayList<>();
                                LinkedList<String> linkedList1 = new LinkedList<>(arrayList1);
                                received.set(linkedList1);
                                LinkedList<Message> messages = new LinkedList<>();
                                for (int i = 0; i < MAX_CHAT_MESSAGES_TO_SHOW; i++)
                                    if (sent.get().size() == 0)
                                        if (received.get().size() == 0)
                                            break;
                                        else
                                            messages.add(ObjectSerializer.deserialize(received.get().remove(0)));
                                    else if (received.get().size() == 0)
                                        messages.add(ObjectSerializer.deserialize(sent.get().remove(0)));
                                    else {
                                        Message sentMessage = ObjectSerializer.deserialize(sent.get().get(0));
                                        Message receivedMessage = ObjectSerializer.deserialize(received.get().get(0));
                                        if (sentMessage != null && receivedMessage != null) {
                                            Date sentTime = sentMessage.time;
                                            Date receivedTime = receivedMessage.time;
                                            String string = sentTime.after(receivedTime) ? received.get().remove(0) : sent.get().remove(0);
                                            messages.add(ObjectSerializer.deserialize(string));
                                        }
                                    }
                                ArrayList<Message> messageList = new ArrayList<>(messages);
                                for (int i = 0; i < messageList.size(); i++)
                                    updateLayout(messageList.get(i));
                            } else {
                                Map<String, Object> data = new HashMap<>();
                                data.put("messages", new ArrayList<>());
                                mFirebaseFirestore.collection("users").document(recipient)
                                        .collection("chats").document(sender).set(data);
                            }
                        });
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("messages", new ArrayList<>());
                mFirebaseFirestore.collection("users").document(sender)
                        .collection("chats").document(recipient).set(data);
            }
        });

        mFirebaseFirestore.collection("users").document(recipient)
                .collection("chats").document(sender).addSnapshotListener(
                (value, error) -> {
                    if (error == null && value != null && value.exists() && !startingUp) {
                        ArrayList<String> messageStrings = (ArrayList<String>) value.get("messages");
                        if (messageStrings == null) messageStrings = new ArrayList<>();
                        if (messageStrings.size() > 0) {
                            String string = messageStrings.get(messageStrings.size() - 1);
                            Message message = ObjectSerializer.deserialize(string);
                            if (message != null)
                                updateLayout(message);
                        }
                    }
                    startingUp = false;
                }
        );
    }

    public int getPixelDistance(int dp) {
        return dp * getResources().getDisplayMetrics().densityDpi / 160;
    }

    public int getTextViewWidth(Message message) {
        Paint paint = new Paint();
        Rect bounds = new Rect();
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(24);
        paint.getTextBounds(message.text, 0, message.text.length(), bounds);
        int w = bounds.width();
        Log.i("(ChatActivity.java:318)", "Width = " + Math.min(896, getPixelDistance((int) ((w + 24d) * 108d / 105d))));
        return (Math.min(896, getPixelDistance((int) ((w + 24d) * 108d / 105d))));
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }
}
