package com.example.nsbr.imageprocessing;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static org.opencv.imgproc.Imgproc.MORPH_ELLIPSE;

public class MainActivity extends AppCompatActivity {
    List<MatOfPoint> contourss = new ArrayList<MatOfPoint>();


    /*private static final String TAG = "LineSegmentProcessor";
    private TextView tvTest;
    private Bitmap mbitmap;
    private ImageView iv;
    private LineSegmentProcess lineSegmentProcess;*/

    private static final String TAG = "LineSegmentProcessor";
    private static final Scalar COLOR_WHITE = new Scalar(255, 255, 255);
    private static final Scalar COLOR_BLACK = new Scalar(0, 0, 0);
    private static final Scalar COLOR_RED = new Scalar(255, 0, 0);

    private Mat mImg, mImgGray, verticalLinesMat, horizontalLinesMat;
    private ArrayList<int[]> verticalLines;
    private ArrayList<int[]> horizontalLines;
    private int mAverageColor;
    private TreeMap<Integer, TreeMap<Integer, RectEntityData>> segmentMapYX;

    public TreeMap<Integer, TreeMap<Integer, RectEntityData>> getSegmentMapYX() {
        return segmentMapYX;
    }

    public ArrayList<int[]> getVerticalLines() {
        return verticalLines;
    }


    public ArrayList<int[]> getHorizontalLines() {
        return horizontalLines;
    }
    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    processing();
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
        /*tvTest = (TextView) findViewById(R.id.tvTest);
        iv = (ImageView) findViewById(R.id.imageView);
        lineSegmentProcess = new LineSegmentProcess();
        lineSegmentProcess.setPostProcessCallBack(mPostProcessCallBack);*/
    }

    /*PostProcessCallBack mPostProcessCallBack = new PostProcessCallBack() {

        @Override
        public void postProcessIsFinished(boolean successfully) {
            iv.setImageBitmap(lineSegmentProcess.getBitmap());
        }
    };*/

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


    /*public void process() {
        mbitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.img).copy(Bitmap.Config.ARGB_8888, true);

        *//*try {
            TreeMap<Integer, TreeMap<Integer, RectEntityData>> treeMap = lineSegmentProcess.segmentLines(mbitmap);
            for (TreeMap.Entry<Integer, TreeMap<Integer, RectEntityData>> mapY : treeMap.entrySet()) {
                TreeMap<Integer, RectEntityData> map = mapY.getValue();

                for (TreeMap.Entry<Integer, RectEntityData> mapX : map.entrySet()) {
                    RectEntityData item = mapX.getValue();
                    if (item != null && item.getBoundingBox() != null) {
                        Canvas canvas = new Canvas(mbitmap);
                        Paint paint = new Paint();
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setColor(Color.RED);
                        canvas.drawRect(item.getBoundingBox(), paint);
                    }
                }
                iv.setImageBitmap(mbitmap);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }*//*

    }*/


    private void foo(Mat canny) {
        Mat hierar = new Mat();
        MatOfInt hulll = new MatOfInt();
        Imgproc.findContours(canny, contourss, hierar, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < contourss.size(); i++) {
            Imgproc.convexHull(contourss.get(i), hulll);
            MatOfPoint hullContour = hull2Points(hulll, contourss.get(i));
            Rect box = Imgproc.boundingRect(hullContour);
            Mat hullMat = new Mat(canny, box);
            int x1 = (int) box.tl().x;
            int y1 = (int) box.tl().y;
            int x2 = (int) box.br().x;
            int y2 = (int) box.br().y;
            Rect segRect = new Rect(x1, y1, x2, y2);
            Imgproc.rectangle(mImg, new Point(x1, y1), new Point(x2, y2), new Scalar(255, 0, 0), 3);
        }
        //convert to bitmap:
        Bitmap bm = Bitmap.createBitmap(mImg.cols(), mImg.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mImg, bm);
        //find the imageview and draw it
        ImageView iv = (ImageView) findViewById(R.id.imageView);
        iv.setImageBitmap(bm);
    }

    private MatOfPoint hull2Points(MatOfInt hull, MatOfPoint contour) {
        List<Integer> indexes = hull.toList();
        List<Point> points = new ArrayList<>();
        MatOfPoint point = new MatOfPoint();
        for (Integer index : indexes) {
            points.add(contour.toList().get(index));
        }
        point.fromList(points);
        return point;
    }

    public void processing() {
        mImg = null;

        Mat thresholdImg = new Mat();
        Mat imgCanny = new Mat();
        Mat bluredCanny = new Mat();

        try {
            mImg = Utils.loadResource(this, R.drawable.img1png, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mImgGray = new Mat(mImg.rows(), mImg.cols(), CvType.CV_8UC1);
        Imgproc.cvtColor(mImg, mImgGray, Imgproc.COLOR_RGB2GRAY);
        //Mat imm = new Mat();
        //double thresh = Imgproc.threshold(mImgGray, imm,0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        //Imgproc.adaptiveThreshold(mImgGray,thresholdImg,255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 75, 15);
        thresholdImg = mImgGray;
        Imgproc.Canny(thresholdImg, imgCanny, 5, 210, 3, true);  //detect edges

        //mImg = getHoughTransform(imgCanny, 1, Math.PI/2, 200, 100, 10, 0, 360);

        Imgproc.blur(imgCanny, bluredCanny, new Size(3, 3));
        Mat mask = new Mat(mImg.size(), mImg.type(), COLOR_BLACK); //to draw lines on this mask

        verticalLines = new ArrayList<int[]>();

        //verticalLinesMat = new Mat();
        //mImg = dekewImage(mImg, getSkewAngle(bluredCanny));
        //foo(imgCanny);

        Imgproc.HoughLinesP(bluredCanny, verticalLinesMat, 1, Math.PI, 110, 70, 0);
        for (int i = 0; i < verticalLinesMat.rows(); i++) {
            double[] l = verticalLinesMat.get(i, 0);
            Imgproc.line(mask, new Point(l[0], l[1]), new Point(l[2], l[3]), COLOR_WHITE, 2); //draw vertical lines on mask
            int[] verticalLine = {(int) l[0], (int) l[1], (int) l[2], (int) l[3]};
            verticalLines.add(verticalLine);
        }

        horizontalLines = new ArrayList<int[]>();

        horizontalLinesMat = new Mat();

        Imgproc.HoughLinesP(bluredCanny, horizontalLinesMat, 1, Math.PI / 2, 60, 65, 0);
        for (int i = 0; i < horizontalLinesMat.rows(); i++) {
            double[] l = horizontalLinesMat.get(i, 0);
            double angle = Math.atan2(l[3] - l[1], l[2] - l[0]) * 180.0 / Math.PI;
            Log.d(TAG, String.valueOf(angle));
            //if (l[1]==l[3]){
            Imgproc.line(mask, new Point(l[0], l[1]), new Point(l[2], l[3]), COLOR_WHITE, 2); //draw horizontal lines on mask
            int[] horizontalLine = {(int) l[0], (int) l[1], (int) l[2], (int) l[3]};
            horizontalLines.add(horizontalLine);
            //}
        }

        //get clear mask
        int size = 3;
        Mat element = Imgproc.getStructuringElement(MORPH_ELLIPSE, new Size(2 * size + 1, 2 * size + 1), new Point(size, size));
        Imgproc.dilate(mask, mask, element);

        Mat canny = new Mat();
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.Canny(mask, canny, 10, 200, 3, true); // get edges of mask and use it to find contours
        Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        int x1, y1, x2, y2;
        segmentMapYX = new TreeMap<>();

        for (int i = 0; i < contours.size(); i += 2) {

           /* MatOfPoint maxMatOfPoint = contours.get(i);
            MatOfPoint2f maxMatOfPoint2f = new MatOfPoint2f(maxMatOfPoint.toArray());
            RotatedRect rect = Imgproc.minAreaRect(maxMatOfPoint2f);

            Point points[] = new Point[4];
            rect.points(points);
            for (int j = 0; j < 4; ++j)
            {
                Imgproc.line(mImg, points[j], points[(j + 1) % 4], new Scalar(0, 0, 255), 2);
            }*/

            org.opencv.core.Rect brect = Imgproc.boundingRect(contours.get(i));
            if (brect.area() < 1000 || brect.width < 20 || brect.height < 20)
                continue;
            if (brect.area() > mImg.width() * mImg.height() / 2)
                continue;
            Imgproc.rectangle(mImg, brect.tl(), brect.br(), new Scalar(255, 0, 0), 3);

            x1 = (int) brect.tl().x;
            y1 = (int) brect.tl().y;
            x2 = (int) brect.br().x;
            y2 = (int) brect.br().y;

            RectEntityData rectEntityData = new RectEntityData();
            android.graphics.Rect segRect = new android.graphics.Rect(x1, y1, x2, y2);

            //calculate average color of rect
            Mat grayRect = new Mat(mImgGray, brect);
            mAverageColor = calculateAverageColor(grayRect);

            rectEntityData.setBoundingBox(segRect);
            rectEntityData.setAverageColor(mAverageColor);

            TreeMap<Integer, RectEntityData> mapYX = new TreeMap<>();
            if (segmentMapYX.containsKey(y1))
                segmentMapYX.get(y1).put(x1, rectEntityData);
            else {
                mapYX.put(x1, rectEntityData);
                segmentMapYX.put(y1, mapYX);
            }

        }

        //convert to bitmap:
        Bitmap bm = Bitmap.createBitmap(mImg.cols(), mImg.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mImg, bm);
        //find the imageview and draw it
        ImageView iv = (ImageView) findViewById(R.id.imageView);
        iv.setImageBitmap(bm);
    }


    private int calculateAverageColor(Mat image) {
        double value = 0;
        int height = image.rows();
        int width = image.cols();
        int pixels = height * width;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                value += image.get(i, j)[0];
            }
        }
        return (int) value / pixels;
    }

    public Mat getHoughTransform(Mat image, double rho, double theta, int threshold, double srn, double stn, double min_theta, double max_theta) {
        Mat lines = new Mat();
        Imgproc.HoughLines(image, lines, rho, theta, threshold, srn, stn, min_theta, max_theta);

        for (int i = 0; i < lines.rows(); i++) {
            double data[] = lines.get(i, 0);
            double rho1 = data[0];
            double theta1 = data[1];
            double cosTheta = Math.cos(theta1);
            double sinTheta = Math.sin(theta1);
            double x0 = cosTheta * rho1;
            double y0 = sinTheta * rho1;
            Point pt1 = new Point(x0 + 10000 * (-sinTheta), y0 + 10000 * cosTheta);
            Point pt2 = new Point(x0 - 10000 * (-sinTheta), y0 - 10000 * cosTheta);
            Imgproc.line(mImg, pt1, pt2, new Scalar(255, 0, 0), 2);
        }
        return mImg;
    }

    public double getSkewAngle(Mat cannyImage) {
        Mat LinesMat = new Mat();
        double length = 0;
        double skewAngle = 0;
        Imgproc.HoughLinesP(cannyImage, LinesMat, 1, Math.PI / 180, 60, 65, 0);
        for (int i = 0; i < LinesMat.rows(); i++) {
            double[] l = LinesMat.get(i, 0);
            double angle = Math.atan2(l[3] - l[1], l[2] - l[0]) * 180.0 / Math.PI;
            if (angle < 10 && angle > -10) {
                double lineLength = (l[3] - l[1]) * (l[3] - l[1]) + (l[2] - l[0]) * (l[2] - l[0]);
                if (lineLength > length) {
                    length = lineLength;
                    skewAngle = angle;
                    //Imgproc.line( mImg, new Point(l[0], l[1]), new Point(l[2], l[3]), COLOR_RED, 2);
                }
            }
        }
        //Log.d(TAG, String.valueOf(skewAngle));

        return skewAngle;
    }

    public Mat dekewImage(Mat image, double skewAngle) {
        Mat warp_dst = new Mat();
        Point pt = new Point(image.cols() / 2, image.rows() / 2);
        Mat M = Imgproc.getRotationMatrix2D(pt, skewAngle, 1.0);
        Imgproc.warpAffine(image, warp_dst, M, image.size());

        return warp_dst;
    }

}
