package com.netglue.ngtmobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {

    private static final String TAG = WebViewActivity.class.getSimpleName();

    WebView mWebView;
    TextView emptyTextView, urlTextView;
    ImageView cancelImageView, lockImageView, menuImageView;

    String web_url, extbrowswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        web_url = getIntent().getStringExtra("url");
        extbrowswer = getIntent().getStringExtra("extbrowswer");

        mWebView = findViewById(R.id.webview);
        urlTextView = findViewById(R.id.urlTextView);
        emptyTextView = findViewById(R.id.emptyTextView);
        cancelImageView = findViewById(R.id.cancelImageView);
        lockImageView = findViewById(R.id.lockImageView);
        menuImageView = findViewById(R.id.menuImageView);

        if(web_url != null && !web_url.isEmpty()) {
            WebSettings webSettings = mWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            mWebView.loadUrl(web_url);
            emptyTextView.setVisibility(View.GONE);
            urlTextView.setText(web_url);
        } else {
            urlTextView.setText("");
            emptyTextView.setVisibility(View.VISIBLE);
        }

        invalidateOptionsMenu();

        cancelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        menuImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context wrapper = new ContextThemeWrapper(getBaseContext(), R.style.popup_style);
                PopupMenu popupMenu = new PopupMenu(wrapper, v);
                popupMenu.inflate(R.menu.webview_popup_menu);
                if(extbrowswer != null && extbrowswer.equals("YES")) {
                    popupMenu.getMenu().findItem(R.id.webview_popup_item_browser).setVisible(true);
                } else {
                    popupMenu.getMenu().findItem(R.id.webview_popup_item_browser).setVisible(false);
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.webview_popup_item_refresh:
                                mWebView.reload();
                                return true;
                            case R.id.webview_popup_item_browser:
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(web_url));
                                getBaseContext().startActivity(i);
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                popupMenu.show();
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == 0) { // refresh

        } else if(item.getItemId() == 1) { // open browser

        }
        return true;
    }
}
