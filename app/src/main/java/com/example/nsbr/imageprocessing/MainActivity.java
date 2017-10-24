package com.example.nsbr.imageprocessing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        process();
    }

    public void process() {
        Bitmap bmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.img1);
        LineSegmentProcess lineSegmentProcess = new LineSegmentProcess();
        try {
            lineSegmentProcess.segmentLines(bmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
