package com.netglue.ngtmobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.netglue.ngtmobile.common.Utils;
import com.netglue.ngtmobile.model.SessionManager;

public class IntroActivity extends AppCompatActivity {
    private static final String TAG = IntroActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sm = SessionManager.getInstance();
        sm.load(this);

        if (sm.isSignIn()) {
            Utils.gotoMain(IntroActivity.this);
            finish();
            return;
        }

        setContentView(R.layout.activity_intro);

        initView();
    }

    private void initView() {
        TextView tv = findViewById(R.id.signIn);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.gotoSignIn(IntroActivity.this);
                finish();
            }
        });
    }

}
