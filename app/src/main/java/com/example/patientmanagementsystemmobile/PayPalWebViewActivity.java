package com.example.patientmanagementsystemmobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PayPalWebViewActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    public static final String EXTRA_URL = "paypal_url";
    public static final String EXTRA_PAYMENT_TYPE = "payment_type";
    public static final String EXTRA_ITEM_ID = "item_id";
    public static final String EXTRA_PAYMENT_ID = "payment_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paypal_webview);

        webView = findViewById(R.id.paypal_webview);
        progressBar = findViewById(R.id.webview_progress_bar);

        // Get the PayPal URL from intent
        String paypalUrl = getIntent().getStringExtra(EXTRA_URL);

        if (paypalUrl == null || paypalUrl.isEmpty()) {
            Toast.makeText(this, "Invalid PayPal URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupWebView();
        webView.loadUrl(paypalUrl);
    }

    private void setupWebView() {
        // Enable JavaScript (required for PayPal)
        webView.getSettings().setJavaScriptEnabled(true);

        // Enable DOM storage (required for PayPal)
        webView.getSettings().setDomStorageEnabled(true);

        // Support zoom controls (optional)
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setDisplayZoomControls(false);

        // Enable responsive layout
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        // Set WebViewClient to handle URL loading
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Check if this is a return URL
                if (url.startsWith("patientmanagementsystem://")) {
                    // Parse the URL to get PayerID and paymentId
                    Uri uri = Uri.parse(url);
                    String payerId = uri.getQueryParameter("PayerID");
                    String paymentId = uri.getQueryParameter("paymentId");

                    // Create result intent with payment details
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("payer_id", payerId);
                    resultIntent.putExtra("payment_id", paymentId);
                    resultIntent.putExtra("payment_type", getIntent().getStringExtra(EXTRA_PAYMENT_TYPE));
                    resultIntent.putExtra("item_id", getIntent().getIntExtra(EXTRA_ITEM_ID, 0));

                    setResult(RESULT_OK, resultIntent);
                    finish();
                    return true;
                }

                // Load PayPal URLs in the WebView
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Toast.makeText(PayPalWebViewActivity.this,
                    "Error loading page: " + description,
                    Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });

        // Set WebChromeClient for progress updates
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);

                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        // If there's history, go back in WebView
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            // Otherwise, close the activity
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
