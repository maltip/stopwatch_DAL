package com.example.puri_malti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    //properties
    private Handler timeHandler;
    private ArrayAdapter<String> itemAdapter;
    private TextView txtTimer;
    private Button btnStartPause, btnLapReset;

    //used to keep track of time
    private long millisecondTime, startTime, pausedTime, updateTime = 0;

    //used to display time
    private int seconds, minutes, milliseconds;

    //used to handle the state of the stop watch
    private boolean stopWatchStarted, stopWatchPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //only used in one place, so shouldn't be a global variable
        ListView lvLaps;

        //timerHandler is bound to a thread
        // used to schedule our Runnable to be executed after particular actions
        timeHandler = new Handler();

        //sets the layout for each item of the list view
        itemAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        txtTimer = findViewById(R.id.txt_timer);
        btnStartPause = findViewById(R.id.btn_start_pause);
        btnLapReset = findViewById(R.id.bnt_lap_reset);
        lvLaps = findViewById(R.id.lv_laps);
        lvLaps.setAdapter(itemAdapter);

        btnStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if the action is to start or pause
                if (!stopWatchStarted || stopWatchPaused) {
                    stopWatchStarted = true;
                    stopWatchPaused = false;

                    startTime = SystemClock.uptimeMillis();

                    //enqueue the Runnable to be called by the message queue after the specified amount of time elapses.
                    //message queues live on the main thread of processes.
                    timeHandler.postDelayed(timerRunnable, 0);

                    //switch label settings
                    btnStartPause.setText(R.string.lblPause);
                    btnLapReset.setText(R.string.btnLap);
                } else {
                    pausedTime += millisecondTime;
                    stopWatchPaused = true;

                    //remove pending posts of timerRunnable in message queue
                    timeHandler.removeCallbacks(timerRunnable);

                    //switch label strings
                    btnStartPause.setText(R.string.lblStart);
                    btnLapReset.setText(R.string.lblReset);
                }
            }
        });
        btnLapReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if the action is to create a new lap or reset the stopwatch
                if (stopWatchStarted && !stopWatchPaused) {
                    String lapTime = minutes + ":"
                            + String.format("%02d", seconds) + ":"
                            + String.format("%03d", milliseconds);

                    itemAdapter.add(lapTime);

                } else if (stopWatchStarted) {
                    stopWatchStarted = false;
                    stopWatchPaused = false;

                    //remove pending posts of timeRunnable in message queue
                    timeHandler.removeCallbacks(timerRunnable);

                    //reset all values
                    millisecondTime = 0;
                    startTime = 0;
                    pausedTime = 0;
                    updateTime = 0;
                    seconds = 0;
                    minutes = 0;
                    milliseconds = 0;

                    //switch label strings
                    txtTimer.setText(R.string.lblTimer);
                    btnLapReset.setText(R.string.btnLap);

                    //wipe resource
                    itemAdapter.clear();

                } else {

                    Toast.makeText(getApplicationContext(), "Timer hasn't started yet", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            millisecondTime = SystemClock.uptimeMillis() - startTime;

            //values used to keep track of where the stopwatch time left off
            updateTime = pausedTime + millisecondTime;
            milliseconds = (int) (updateTime % 1000);
            seconds = (int) (updateTime / 1000);

            //convert values to display
            minutes = seconds / 60;
            seconds = seconds % 60;
            String updatedTime = minutes + ":"
                    + String.format("%02d", seconds) + ":"
                    + String.format("%03d", milliseconds);

            txtTimer.setText(updatedTime);

            //enqueues the runnable to be called by the message queue after the specified amount of time elapses
            timeHandler.postDelayed(this, 0);
        }
    };
}