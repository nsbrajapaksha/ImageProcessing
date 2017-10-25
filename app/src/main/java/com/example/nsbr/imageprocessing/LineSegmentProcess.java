package com.example.nsbr.imageprocessing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static org.opencv.imgproc.Imgproc.MORPH_ELLIPSE;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.getStructuringElement;

/**
 * Created by nipuna on 10/12/17.
 */

public class LineSegmentProcess extends Activity {

    private static final String TAG = "LineSegmentProcessor";
    private static final Scalar COLOR_WHITE = new Scalar(255, 255, 255);
    private static final Scalar COLOR_BLACK = new Scalar(0, 0, 0);

    private Mat mImg = null;
    private Mat mImgGray, verticalLinesMat, horizontalLinesMat, mask;
    private ArrayList<int[]> verticalLines;
    private ArrayList<int[]> horizontalLines;
    private int mAverageColor;
    private int skewAngle;
    private Bitmap bitmap = null;

    public static void setPostProcessCallBack(PostProcessCallBack postProcessCallBack) {
        mPostProcessCallBack = postProcessCallBack;
    }

    static public PostProcessCallBack mPostProcessCallBack;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public ArrayList<int[]> getVerticalLines() {
        return verticalLines;
    }


    public ArrayList<int[]> getHorizontalLines() {
        return horizontalLines;
    }


    public TreeMap<Integer, TreeMap<Integer, RectEntityData>> segmentLines(final Bitmap bmp) throws IOException {
       /* // Switch text to processing
        Log.i(TAG, "Line segmentation processing....");

        // line segmentation in an async task
        new AsyncTask<Object, Void, TreeMap<Integer, TreeMap<Integer, RectEntityData>>>() {
            @Override
            protected TreeMap<Integer, TreeMap<Integer, RectEntityData>> doInBackground(Object... params) {*/
                mImg = new Mat();
                mImgGray = new Mat(mImg.rows(), mImg.cols(), CvType.CV_8UC1);
                Mat thresholdImg = new Mat();
                Mat imgCanny = new Mat();
                Mat bluredCanny = new Mat();

                Utils.bitmapToMat(bmp, mImg); //convert bitmap to Mat

                Imgproc.cvtColor(mImg, mImgGray, Imgproc.COLOR_RGB2GRAY);
                Imgproc.adaptiveThreshold(mImgGray, thresholdImg, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 75, 15);

                Imgproc.Canny(thresholdImg, imgCanny, 100, 200, 5, false);  //detect edges
                Imgproc.blur(imgCanny, bluredCanny, new Size(3, 3));
                mask = new Mat(mImg.size(), mImg.type(), COLOR_BLACK); //to draw lines on this mask

                verticalLines = new ArrayList<int[]>();

                verticalLinesMat = new Mat();

                Imgproc.HoughLinesP(bluredCanny, verticalLinesMat, 1, Math.PI, 90, 50, 0);
                for (int i = 0; i < verticalLinesMat.rows(); i++) {
                    double[] l = verticalLinesMat.get(i, 0);
                    Imgproc.line(mask, new Point(l[0], l[1]), new Point(l[2], l[3]), COLOR_WHITE, 3); //draw vertical lines on mask
                    int[] verticalLine = {(int) l[0], (int) l[1], (int) l[2], (int) l[3]};
                    verticalLines.add(verticalLine);
                }

                horizontalLines = new ArrayList<int[]>();

                horizontalLinesMat = new Mat();

                Imgproc.HoughLinesP(bluredCanny, horizontalLinesMat, 1, Math.PI / 2, 100, 150, 0);
                for (int i = 0; i < horizontalLinesMat.rows(); i++) {
                    double[] l = horizontalLinesMat.get(i, 0);
                    if (l[1] == l[3]) {
                        Imgproc.line(mask, new Point(l[0], l[1]), new Point(l[2], l[3]), COLOR_WHITE, 3); //draw horizontal lines on mask
                        int[] horizontalLine = {(int) l[0], (int) l[1], (int) l[2], (int) l[3]};
                        horizontalLines.add(horizontalLine);
                    }
                }

                //get clear mask
                int size = 3;
                Mat element = getStructuringElement(MORPH_ELLIPSE, new Size(2 * size + 1, 2 * size + 1), new Point(size, size));
                Imgproc.dilate(mask, mask, element);

                //find contours and populate data
                ArrayList<MatOfPoint> contours = findContours();

                return populateLineSegmentData(contours);
            }


           /* @Override
            protected void onPostExecute(TreeMap<Integer, TreeMap<Integer, RectEntityData>> result) {
                Log.d(TAG, "finished processing");
                //......post processing

                bitmap = Bitmap.createBitmap(mImg.cols(), mImg.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mImg, bitmap);
                *//*for (TreeMap.Entry<Integer, TreeMap<Integer, RectEntityData>> mapY : result.entrySet()) {
                    TreeMap<Integer, RectEntityData> map = mapY.getValue();

                    for (TreeMap.Entry<Integer, RectEntityData> mapX : map.entrySet()) {
                        RectEntityData item = mapX.getValue();
                        if (item != null && item.getBoundingBox() != null) {
                            Canvas canvas = new Canvas(bitmap);
                            Paint paint = new Paint();
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setColor(Color.RED);
                            canvas.drawRect(item.getBoundingBox(), paint);
                        }
                    }

                }*//*
                mPostProcessCallBack.postProcessIsFinished(true);

            }
        }.execute();
    }*/

    private ArrayList<MatOfPoint> findContours() {
        Mat canny = new Mat();
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.Canny(mask, canny, 100, 200, 3, false); // get edges of mask and use it to find contours
        Imgproc.findContours(canny, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        return contours;
    }

    private TreeMap<Integer, TreeMap<Integer, RectEntityData>> populateLineSegmentData(ArrayList<MatOfPoint> contours) {
        int x1, y1, x2, y2;
        TreeMap<Integer, TreeMap<Integer, RectEntityData>> segmentMapYX = new TreeMap<>();

        for (int i = 0; i < contours.size(); i += 2) {
            org.opencv.core.Rect brect = Imgproc.boundingRect(contours.get(i));
            if (brect.area() < 1000 || brect.width < 10 || brect.height < 10)
                continue;
            if (brect.area() > mImg.width() * mImg.height() / 2)
                continue;

            x1 = (int) brect.tl().x;
            y1 = (int) brect.tl().y;
            x2 = (int) brect.br().x;
            y2 = (int) brect.br().y;

            RectEntityData rectEntityData = new RectEntityData();
            Rect segRect = new Rect(x1, y1, x2, y2);

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

        return segmentMapYX;
    }


    public Double distanceToVerticalRight(Point point) {
        double valRight = mImg.size().width;
        for (int i = 0; i < verticalLinesMat.rows(); i++) {
            double[] l = verticalLinesMat.get(i, 0);
            if (l[0] == l[2]) {
                if (Math.abs(l[0] - point.x) < valRight && (l[0] - point.x > 0) && ((point.y < l[3] && point.y > l[1]) || (point.y < l[1] && point.y > l[3])))
                    valRight = Math.abs(l[0] - point.x);
            }
        }
        if (valRight == mImg.size().width) {
            Log.d("TAG", "page end");
            return null;
        }
        return valRight;
    }


    public Double distanceToVerticalLeft(Point point) {
        double valLeft = mImg.size().width;
        for (int i = 0; i < verticalLinesMat.rows(); i++) {
            double[] l = verticalLinesMat.get(i, 0);
            if (l[0] == l[2]) {
                if (Math.abs(l[0] - point.x) < valLeft && (l[0] - point.x < 0) && ((point.y < l[3] && point.y > l[1]) || (point.y < l[1] && point.y > l[3])))
                    valLeft = Math.abs(l[0] - point.x);
            }
        }
        if (valLeft == mImg.size().width) {
            Log.d(TAG, "page end");
            return null;
        }
        return valLeft;
    }


    public Double distanceToHorizontalUp(Point point) {
        double valUp = mImg.size().height;
        for (int i = 0; i < horizontalLinesMat.rows(); i++) {
            double[] l = horizontalLinesMat.get(i, 0);
            if (l[1] == l[3]) {
                if (Math.abs(l[1] - point.y) < valUp && (l[1] - point.y > 0) && ((point.x < l[0] && point.x > l[2]) || (point.x < l[0] && point.x > l[2])))
                    valUp = Math.abs(l[1] - point.y);
            }
        }
        if (valUp == mImg.size().height) {
            Log.d(TAG, "page end");
            return null;
        }
        return valUp;
    }


    public Double distanceToHorizontalDown(Point point) {
        double valDown = mImg.size().height;
        for (int i = 0; i < horizontalLinesMat.rows(); i++) {
            double[] l = horizontalLinesMat.get(i, 0);
            if (l[1] == l[3]) {
                if (Math.abs(l[1] - point.y) < valDown && (l[1] - point.y < 0) && ((point.x < l[0] && point.x > l[2]) || (point.x < l[0] && point.x > l[2])))
                    valDown = Math.abs(l[1] - point.y);
            }
        }
        if (valDown == mImg.size().height) {
            Log.d(TAG, "page end");
            return null;
        }
        return valDown;
    }


    public ArrayList<Bitmap> getCroppedBitmaps(Bitmap originalBmp, ArrayList<Rect> boundingBox) {
        ArrayList<Bitmap> croppedBitmaps = new ArrayList<>();
        int width, height, x, y;
        for (int i = 0; i < boundingBox.size(); i++) {
            width = Math.abs(boundingBox.get(i).width());
            height = Math.abs(boundingBox.get(i).height());
            x = (int) (boundingBox.get(i).exactCenterX() - width / 2.0);
            y = (int) (boundingBox.get(i).exactCenterY() - height / 2.0);
            Bitmap bitmap = Bitmap.createBitmap(originalBmp, x, y, width, height);
            croppedBitmaps.add(bitmap);
        }
        return croppedBitmaps;
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


    public Mat deskew(Mat src, double angle) {
        Point center = new Point(src.width() / 2, src.height() / 2);
        Mat rotImage = Imgproc.getRotationMatrix2D(center, angle, 1.0);
        //1.0 means 100 % scale
        Size size = new Size(src.width(), src.height());
        Imgproc.warpAffine(src, src, rotImage, size, Imgproc.INTER_LINEAR + Imgproc.CV_WARP_FILL_OUTLIERS);
        return src;
    }

    public Bitmap computeSkewCorrection(Bitmap image) {


        Mat img = new Mat();


        Utils.bitmapToMat(image, img); //convert bitmap to Mat
        //Binarize it
        //Use adaptive threshold if necessary
        //Imgproc.adaptiveThreshold(mImg, mImg, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 40);
        Imgproc.threshold(img, img, 200, 255, THRESH_BINARY);

        //Invert the colors (because objects are represented as white pixels, and the background is represented by black pixels)
        Core.bitwise_not(img, img);
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));

        //We can now perform our erosion, we must declare our rectangle-shaped structuring element and call the erode function
        Imgproc.erode(img, img, element);

        //Find all white pixels
        Mat wLocMat = Mat.zeros(img.size(), img.type());
        Core.findNonZero(img, wLocMat);

        //Create an empty Mat and pass it to the function
        MatOfPoint matOfPoint = new MatOfPoint(wLocMat);

        //Translate MatOfPoint to MatOfPoint2f in order to user at a next step
        MatOfPoint2f mat2f = new MatOfPoint2f();
        matOfPoint.convertTo(mat2f, CvType.CV_32FC2);

        //Get rotated rect of white pixels
        RotatedRect rotatedRect = Imgproc.minAreaRect(mat2f);

        Point[] vertices = new Point[4];
        rotatedRect.points(vertices);
        List<MatOfPoint> boxContours = new ArrayList<>();
        boxContours.add(new MatOfPoint(vertices));
        Imgproc.drawContours(img, boxContours, 0, new Scalar(128, 128, 128), -1);

        double resultAngle = rotatedRect.angle;
        if (rotatedRect.size.width > rotatedRect.size.height) {
            rotatedRect.angle += 90.f;
        }

        //Or
        //rotatedRect.angle = rotatedRect.angle < -45 ? rotatedRect.angle + 90.f : rotatedRect.angle;

        Mat result = deskew(img, rotatedRect.angle);


        //Mat tmp = new Mat (height, width, CvType.CV_8U, new Scalar(4));
        Mat resultColor = new Mat();
        Imgproc.cvtColor(result, resultColor, Imgproc.COLOR_RGB2BGRA);
        Bitmap bmp = Bitmap.createBitmap(resultColor.cols(), resultColor.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(resultColor, bmp);

        return bmp;


 /*       Mat source = new Mat();


        Utils.bitmapToMat(image, source); //convert bitmap to Mat
        Size size = source.size();
        Core.bitwise_not(source, source);
        Mat lines = new Mat();
        Imgproc.HoughLinesP(source, lines, 1, Math.PI / 180, 100, size.width / 2.f, 20);
        double angle = 0.;
        for(int i = 0; i<lines.height(); i++){
            for(int j = 0; j<lines.width();j++){
                angle += Math.atan2(lines.get(i, j)[3] - lines.get(i, j)[1], lines.get(i, j)[2] - lines.get(i, j)[0]);
            }
        }
        angle /= lines.size().area();
        angle = angle * 180 / Math.PI;

        Mat resultMat = deskew(source,angle);

        Mat resultColor = new Mat();
        Imgproc.cvtColor(resultMat, resultColor, Imgproc.COLOR_RGB2BGRA);
        Bitmap bmp = Bitmap.createBitmap(resultColor.cols(), resultColor.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(resultColor, bmp);*/


    }


}

