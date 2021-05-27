package com.netglue.ngtmobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.netglue.ngtmobile.common.Utils;

public class PermissionActivity extends AppCompatActivity {
    private static final String TAG = PermissionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        initView();
    }

    private void initView() {
        Button btn  = findViewById(R.id.next);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.gotoMain(PermissionActivity.this);
                finish();
            }
        });
    }

}
