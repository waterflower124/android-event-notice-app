package com.netglue.ngtmobile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.netglue.ngtmobile.adapter.HighLightArrayAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MonitorActivity extends AppCompatActivity {
    public static final String TAG = MonitorActivity.class.getSimpleName();

    private HighLightArrayAdapter intervalAdapter;

    private Date durationStart = new Date();
    private Date durationStop = new Date(durationStart.getTime() + 24 * 3600 * 1000);
    private Date startDate;
    private Date stopDate;

    private TextView tvDuration;

    private DatePicker datePicker;
    private TimePicker timePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        initView();
    }

    private void initView() {
        // Interval
        final Spinner spinner = findViewById(R.id.interval);
        intervalAdapter = new HighLightArrayAdapter(this, R.layout.spinner_interval_item, getResources().getStringArray(R.array.monitor_interval));
        intervalAdapter.setHighlightColor(0xFFEEEEEE);
        intervalAdapter.setDropDownViewResource(R.layout.spinner_dropdown_interval_item);
        spinner.setAdapter(intervalAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                intervalAdapter.setSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Duration
        tvDuration = findViewById(R.id.duration);
        tvDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDurationPicker();
            }
        });
        showDuration();

        // Cancel
        Button btn = findViewById(R.id.cancel);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Done
        btn = findViewById(R.id.done);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] intervalValues = getResources().getStringArray(R.array.monitor_interval_value);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                Intent intent = new Intent();
                intent.putExtra("mon_interval", intervalValues[spinner.getSelectedItemPosition()]);
                intent.putExtra("mon_start_timestamp", sdf.format(durationStart));
                intent.putExtra("mon_stop_timestamp", sdf.format(durationStop));

                setResult(RESULT_OK, intent);

                finish();
            }
        });
    }

    private void showDurationPicker() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_monitor_period);

        startDate = durationStart;
        stopDate = durationStop;

        datePicker = dialog.findViewById(R.id.date_picker);
        datePicker.setMinDate(new Date().getTime());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                Log.d("Date", "Year=" + year + " Month=" + (month + 1) + " day=" + dayOfMonth);

                Calendar cldr = Calendar.getInstance();

                RadioGroup rg = dialog.findViewById(R.id.endpoint_group);
                if (rg.getCheckedRadioButtonId() == R.id.start_radio) {
                    cldr.setTime(startDate);
                    cldr.set(year, month, dayOfMonth);
                    startDate = cldr.getTime();
                } else {
                    cldr.setTime(stopDate);
                    cldr.set(year, month, dayOfMonth);
                    stopDate = cldr.getTime();
                }
            }
        });
        datePicker.findViewById(Resources.getSystem().getIdentifier("date_picker_header", "id", "android"))
                .setVisibility(View.GONE);

        Calendar cldr = Calendar.getInstance();
        cldr.setTime(startDate);

        timePicker = dialog.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(cldr.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(cldr.get(Calendar.MINUTE));
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                Calendar cldr = Calendar.getInstance();
                RadioGroup rg = dialog.findViewById(R.id.endpoint_group);
                if (rg.getCheckedRadioButtonId() == R.id.start_radio) {
                    cldr.setTime(startDate);
                    cldr.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    cldr.set(Calendar.MINUTE, minute);
                    startDate = cldr.getTime();
                } else {
                    cldr.setTime(stopDate);
                    cldr.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    cldr.set(Calendar.MINUTE, minute);
                    stopDate = cldr.getTime();
                }
            }
        });

        // duration start or stop
        RadioGroup rg = dialog.findViewById(R.id.endpoint_group);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // This will get the radiobutton that has changed in its check state
                RadioButton checkedRadioButton = group.findViewById(checkedId);
                // This puts the value (true/false) into the variable
                boolean isChecked = checkedRadioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked) {
                    datePicker.setMinDate(0);
                    Date date = new Date();
                    if (checkedId == R.id.start_radio) {
                        datePicker.setMinDate(date.getTime());
                        date = startDate;
                    } else {
                        datePicker.setMinDate(startDate.getTime());
                        date = stopDate;
                    }

                    Calendar cldr = Calendar.getInstance();
                    cldr.setTime(date);

                    datePicker.updateDate(cldr.get(Calendar.YEAR), cldr.get(Calendar.MONTH), cldr.get(Calendar.DATE));

                    timePicker.setCurrentHour(cldr.get(Calendar.HOUR_OF_DAY));
                    timePicker.setCurrentMinute(cldr.get(Calendar.MINUTE));
                }
            }
        });

        // Cancel
        View v = dialog.findViewById(R.id.cancel);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Done
        v = dialog.findViewById(R.id.done);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                durationStart = startDate;
                durationStop = stopDate;

                showDuration();
            }
        });

        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        dialog.show();
    }

    private void showDuration() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm");
        tvDuration.setText(String.format("%s to %s",
                sdf.format(durationStart), sdf.format(durationStop)));
    }
}
