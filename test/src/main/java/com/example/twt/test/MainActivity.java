package com.example.twt.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void Click(View view){
        Intent intent = new Intent();
        intent.setDataAndType(Uri.parse("http://192.168.1.102:8080/vivo11.rmvb"),"video/*");
        startActivity(intent);

    }
}
