package com.example.nsbr.imageprocessing;

import android.graphics.Rect;

/**
 * Created by nipuna on 10/3/17.
 */

public class RectEntityData {
    private Rect mBoundingBox;
    private int mAverageColor;

    public RectEntityData() {

    }

    public int getAverageColor() {
        return mAverageColor;
    }

    public void setAverageColor(int mAverageColor) {
        this.mAverageColor = mAverageColor;
    }

    public Rect getBoundingBox() {
        return mBoundingBox;
    }

    public void setBoundingBox(Rect mBoundingBox) {
        this.mBoundingBox = mBoundingBox;
    }

}
