package com.example.classx.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.classx.R;

public class SocialMediaActivity extends AppCompatActivity {

    WebView webView;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media);

        intent = getIntent();

        webView = findViewById(R.id.socialMediaWebView);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        try {
            webView.loadUrl("https://www.twitter.com/" + intent.getStringExtra("handle"));
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}