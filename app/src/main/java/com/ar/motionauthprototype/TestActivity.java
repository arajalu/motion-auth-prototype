package com.ar.motionauthprototype;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.opencv.android.OpenCVLoader;
import org.opencv.ml.SVM;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class TestActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    final float alpha = 0.8f;
    float[] gravity=new float[3];
    float[] linear_acceleration=new float[3];
    private ArrayList<Pair<Long, float[]>> sensorData;
    private Button btnHold;
    private TextView[] acc_tv =new TextView[3];
    private float[] tmpacc=new float[]{0f,0f,0f};
    private final Handler mHandler = new Handler();
    private Runnable mTimer;
    private double graphLastXValue = 5d;
    private LineGraphSeries<DataPoint> mSeriesx,mSeriesy,mSeriesz;
    private int count = 0,first=1;
    private String allcsvData="class,time,x,y,z\n";

    private Object mPauseLock;
    private boolean mPaused;
    private static SVM svm;

    private static final int REQUEST_CODE = 0x11;

    String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPauseLock = new Object();
        mPaused = false;
        GraphView graph = (GraphView) findViewById(R.id.graph);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        btnHold= (Button)findViewById(R.id.btn_hold);
        btnHold.setOnTouchListener(holdListener);

//        acc_tv[0] = (TextView)findViewById(R.id.acc_x);
//        acc_tv[1] = (TextView)findViewById(R.id.acc_y);
//        acc_tv[2] = (TextView)findViewById(R.id.acc_z);

        initGraph(graph);
        Thread t = new Thread();
        mTimer = new Runnable() {
            @Override
            public void run() {
                graphLastXValue += 0.05d;
                mSeriesx.appendData(new DataPoint(graphLastXValue, tmpacc[0]), true, 80);
                mSeriesy.appendData(new DataPoint(graphLastXValue, tmpacc[1]), true, 80);
                mSeriesz.appendData(new DataPoint(graphLastXValue, tmpacc[2]), true, 80);
                mHandler.postDelayed(mTimer, 50);
            }
        };
        mHandler.postDelayed(mTimer, 100);

        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }

        svm= SVM.create();
        svm.setType(SVM.ONE_CLASS);
    }

    public void initGraph(GraphView graph) {
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(4);

        graph.getViewport().setMinY(-10);
        graph.getViewport().setMaxY(10);
        graph.getGridLabelRenderer().setLabelVerticalWidth(100);

        // x
        mSeriesx = new LineGraphSeries<>();
        mSeriesx.setTitle("x");
        //y
        mSeriesy = new LineGraphSeries<>();
        mSeriesy.setColor(Color.argb(255, 255, 60, 60));
        mSeriesy.setTitle("y");
        //y
        mSeriesz = new LineGraphSeries<>();
        mSeriesz.setColor(Color.argb(255, 150, 120, 60));
        mSeriesz.setTitle("z");


        graph.addSeries(mSeriesx);
        graph.addSeries(mSeriesy);
        graph.addSeries(mSeriesz);

        // legend
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    private View.OnTouchListener holdListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d("Debug::", "Down");
                    sensorData = new ArrayList<>();
                    sensorManager.registerListener(TestActivity.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d("Debug::", "Up");
                    Long starting=0l;
                    sensorManager.unregisterListener(TestActivity.this);
                    if (sensorData.size() > 150) {
                        Toast.makeText(TestActivity.this, "OK", Toast.LENGTH_SHORT).show();
                        first=1;
                        for(Pair<Long,float[]> pair : sensorData){
                            if(first==1){
                                first=0;
                                starting=pair.first;
                            }
                            allcsvData+="a,";
                            allcsvData+=(pair.first-starting)+",";
                            allcsvData+=pair.second[0]+","+pair.second[1]+","+pair.second[2];
                            allcsvData+="\n";
                        }

                        count++;
                        btnHold.setText(Integer.toString(3-count));
                        Arrays.fill(gravity,0f);
                        if (count== 1){
                            SaveDataToFile(sensorData, TestActivity.this, count);
                            startActivity(new Intent(getApplicationContext(),Menu.class));
                        }
                    }
                    else {
                        Toast.makeText(TestActivity.this, "Try holding the button longer", Toast.LENGTH_SHORT).show();
                    }

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

            //Log.d("acc log","filtered Sensor data : " + Arrays.toString(linear_acceleration));
            int i=0;
            //Make it red if there is change in acceleration beyond threshold
//            for (float val:tmpacc) {
//                if((linear_acceleration[i]-val)>0.1){
//                    //red
//                    acc_tv[i].setTextColor(Color.parseColor("#d42e2e"));
//                }else{
//                    acc_tv[i++].setTextColor(Color.parseColor("#0d9839"));
//                }
//            }
            i=0;
            for (float val:linear_acceleration) {
                tmpacc[i++]=val;
            }

//            acc_tv[0].setText("x:"+String.valueOf(linear_acceleration[0]));
//            acc_tv[1].setText("y:"+String.valueOf(linear_acceleration[1]));
//            acc_tv[2].setText("z:"+String.valueOf(linear_acceleration[2]));
//            Log.d("acc log","DATA:\t"+event.timestamp+"\t"+ event.values[0]+"\t"+event.values[1]+"\t"+event.values[2]+"\t"+ linear_acceleration[0]+"\t"+linear_acceleration[1]+"\t"+linear_acceleration[2]);
            sensorData.add(new Pair<>(System.nanoTime(),new float[]{linear_acceleration[0],linear_acceleration[1],linear_acceleration[2]}));


        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void SaveDataToFile(ArrayList<Pair<Long, float[]>> sensorData, Activity activity, int curveCount) {
        Long starting=0l;
        File Dir = new File(Environment.getExternalStorageDirectory()+"/accData");
        Dir.mkdirs();

        String fileName = "test" + Integer.toString(curveCount)+".csv";
        File file = new File(Dir, fileName);
        if (file.exists()){
            file.delete();
            Log.d("FILEDELETE","DELETED");
        }
        try{
            file.createNewFile();
        }catch(Exception e){
            e.getMessage();
        }


        FileOutputStream fos ;
        try {
            fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            Log.d("csv",allcsvData);
            osw.write(allcsvData);
            osw.flush();
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, "Save file failed", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // save file
            } else {
                Toast.makeText(getApplicationContext(), "PERMISSION_DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
