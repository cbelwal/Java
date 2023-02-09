package com.dasmic.android.tradocly;

import android.net.http.SslError;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {
    private final String  _url="http://www.tradocly.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            WebView webView = findViewById(R.id.webView);
            webView.restoreState(savedInstanceState);
        }
        else
        {
            configureWebView();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState )
    {
        super.onSaveInstanceState(outState);
        WebView webView = findViewById(R.id.webView);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        WebView webView = findViewById(R.id.webView);
        webView.restoreState(savedInstanceState);
    }

    private void configureWebView(){
            WebView webView = findViewById(R.id.webView);

            webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            }

            @Override
            public void onReceivedHttpError(
                    WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                           SslError error) {
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.loadUrl(_url);
    }
}
