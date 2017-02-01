package com.ar.motionauthprototype;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class Menu extends AppCompatActivity {
    private Button btnSetGesture;
    private Button btnLockScreen;
    private Button btnGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Menu");
        setContentView(R.layout.activity_menu);
        btnSetGesture =(Button)findViewById(R.id.btn_set_gesture);
        btnLockScreen = (Button)findViewById(R.id.btn_lock_screen);
        btnGraph =(Button)findViewById(R.id.btn_graph);

        btnSetGesture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Menu.this,SetGesture.class));
            }
        });
        btnLockScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Menu.this,LockScreen.class));
            }
        });
        btnGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Menu.this,Graph.class));
            }
        });
    }
}
