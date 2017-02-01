package com.ar.motionauthprototype;

import android.graphics.Color;
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
    private TextView[] acc_tv =new TextView[3];
    private float[] tmpacc=new float[]{0f,0f,0f};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Set Gesture");
        setContentView(R.layout.activity_set_gesture);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        btnHold= (Button)findViewById(R.id.btn_hold);
        btnHold.setOnTouchListener(holdListener);
        acc_tv[0] = (TextView)findViewById(R.id.acc_x);
        acc_tv[1] = (TextView)findViewById(R.id.acc_y);
        acc_tv[2] = (TextView)findViewById(R.id.acc_z);
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
            int i=0;
            //Make it red if there is change in acceleration beyond threshold
            for (float val:tmpacc) {
                if((linear_acceleration[i]-val)>0.1){
                    //red
                    acc_tv[i].setTextColor(Color.parseColor("#d42e2e"));
                }else{
                    acc_tv[i++].setTextColor(Color.parseColor("#0d9839"));
                }
            }
            i=0;
            for (float val:linear_acceleration) {
                tmpacc[i++]=val;
            }

            acc_tv[0].setText("x:"+String.valueOf(linear_acceleration[0]));
            acc_tv[1].setText("y:"+String.valueOf(linear_acceleration[1]));
            acc_tv[2].setText("z:"+String.valueOf(linear_acceleration[2]));
            sensorLog.add(new Pair<>(event.timestamp,new float[]{linear_acceleration[0], linear_acceleration[1], linear_acceleration[2]}));


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
