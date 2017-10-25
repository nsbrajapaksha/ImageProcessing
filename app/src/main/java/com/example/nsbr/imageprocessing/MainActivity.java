package com.example.nsbr.imageprocessing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LineSegmentProcessor";
    private TextView tvTest;
    private ImageView iv;
    private LineSegmentProcess lineSegmentProcess;
    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    process();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTest = (TextView) findViewById(R.id.tvTest);
        iv = (ImageView) findViewById(R.id.imageView);
        lineSegmentProcess = new LineSegmentProcess();
        lineSegmentProcess.setPostProcessCallBack(mPostProcessCallBack);
    }

    PostProcessCallBack mPostProcessCallBack = new PostProcessCallBack() {

        @Override
        public void postProcessIsFinished(boolean successfully) {
            iv.setImageBitmap(lineSegmentProcess.getBitmap());
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "opencv succesfully loaded");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.d(TAG, "opencv not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, baseLoaderCallback);
        }
    }


    public void process() {
        Bitmap bmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.img3);
        try {
            lineSegmentProcess.segmentLines(bmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
