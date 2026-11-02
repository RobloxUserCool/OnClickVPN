package com.onclickvpn.app;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int VPN_REQ = 42;
    private WebView webView;
    private String vpnConfig = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/index.html");
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void startVpn(String config) {
            vpnConfig = config;
            Intent intent = VpnService.prepare(MainActivity.this);
            if (intent != null) {
                startActivityForResult(intent, VPN_REQ);
            } else {
                onActivityResult(VPN_REQ, RESULT_OK, null);
            }
        }

        @JavascriptInterface
        public void stopVpn() {
            stopService(new Intent(MainActivity.this, VpnTunnelService.class));
            runOnUiThread(() -> webView.evaluateJavascript("onVpnStatusChanged('disconnected');", null));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQ && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, VpnTunnelService.class);
            intent.putExtra("config", vpnConfig);
            startService(intent);
            webView.evaluateJavascript("onVpnStatusChanged('connected');", null);
        }
    }
                        }
