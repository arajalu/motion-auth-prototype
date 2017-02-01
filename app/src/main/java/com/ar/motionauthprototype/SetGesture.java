package com.ar.motionauthprototype;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class SetGesture extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    final float alpha = 0.8f;
    float[] gravity=new float[3];
    float[] linear_acceleration=new float[3];
    private ArrayList<Pair<Long, float[]>> sensorLog;
    private Button btnHold;
    private TextView acc_debug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Set Gesture");
        setContentView(R.layout.activity_set_gesture);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        btnHold= (Button)findViewById(R.id.btn_hold);
        btnHold.setOnTouchListener(holdListener);
        acc_debug = (TextView)findViewById(R.id.acc_debug);
    }

    private View.OnTouchListener holdListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d("Debug::", "Down");
                    sensorLog = new ArrayList<>();
                    sensorManager.registerListener
                            (SetGesture.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d("Debug::", "Up");
                    sensorManager.unregisterListener(SetGesture.this);
                    break;
            }
            return true;
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometer) {
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];
            System.out.println(String.valueOf(event.timestamp));

            System.out.println("filtered Sensor data : " + Arrays.toString(linear_acceleration));
            acc_debug.setText(Arrays.toString(linear_acceleration));
            sensorLog.add(new Pair<>(event.timestamp,new float[]{linear_acceleration[0], linear_acceleration[1], linear_acceleration[2]}));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
