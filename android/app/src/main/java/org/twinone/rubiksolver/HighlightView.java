package org.twinone.rubiksolver;/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

class HighlightView extends View implements View.OnTouchListener {

    private static final int TOP_LEFT = 0;
    private static final int TOP_RIGHT = 1;
    private static final int BOTTOM_RIGHT = 2;
    private static final int BOTTOM_LEFT = 3;
    private static final int VERTEX_TOUCH_RADIUS = 60; // px

    int mParentW;
    int mParentH;
    private Paint mBorderPaint;
    private Paint mThirdsPaint;
    private Paint mCornerHandlePaint;

    private int mHitVertex = -1;


    // Clockwise, starting at top left
    Point[] mCoords = new Point[4];


    public HighlightView(Context context) {
        super(context);
    }

    public HighlightView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HighlightView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init() {
        setWillNotDraw(false);

        Log.d("HLView", "Parent: " + mParentW + "x" + mParentH);
        float scale = 0.8f;
        int s = (int) (scale * Math.min(mParentW, mParentH));
        int marginLeft = (mParentW - s) / 2;
        int marginTop = (mParentH - s) / 2;

        mCoords[0] = new Point(marginLeft + 0, marginTop + 0);
        mCoords[1] = new Point(marginLeft + s, marginTop + 0);
        mCoords[2] = new Point(marginLeft + s, marginTop + s);
        mCoords[3] = new Point(marginLeft + 0, marginTop + s);

        mBorderPaint = new Paint();
        mBorderPaint.setColor(0xffffffff);
        mBorderPaint.setStrokeWidth(10.0f);

        mThirdsPaint = new Paint();
        mThirdsPaint.setColor(0xffffffff);
        mThirdsPaint.setStrokeWidth(2.0f);

        mCornerHandlePaint = new Paint();
        mCornerHandlePaint.setColor(Color.WHITE);

        setOnTouchListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw contour
        for (int i = 0; i < 4; i++) {
            canvas.drawLine(
                    mCoords[i].x, mCoords[i].y,
                    mCoords[(i + 1) % 4].x, mCoords[(i + 1) % 4].y,
                    mBorderPaint);
        }

        // Draw thirds
        for (int i = 1; i < MainActivity.SIZE; i++) {
            canvas.drawLine(
                    mCoords[TOP_LEFT].x + i * (mCoords[TOP_RIGHT].x - mCoords[TOP_LEFT].x) / MainActivity.SIZE,
                    mCoords[TOP_LEFT].y + i * (mCoords[TOP_RIGHT].y - mCoords[TOP_LEFT].y) / MainActivity.SIZE,
                    mCoords[BOTTOM_LEFT].x + i * (mCoords[BOTTOM_RIGHT].x - mCoords[BOTTOM_LEFT].x) / MainActivity.SIZE,
                    mCoords[BOTTOM_LEFT].y + i * (mCoords[BOTTOM_RIGHT].y - mCoords[BOTTOM_LEFT].y) / MainActivity.SIZE,
                    mThirdsPaint);
            canvas.drawLine(
                    mCoords[TOP_LEFT].x + i * (mCoords[BOTTOM_LEFT].x - mCoords[TOP_LEFT].x) / MainActivity.SIZE,
                    mCoords[TOP_LEFT].y + i * (mCoords[BOTTOM_LEFT].y - mCoords[TOP_LEFT].y) / MainActivity.SIZE,
                    mCoords[TOP_RIGHT].x + i * (mCoords[BOTTOM_RIGHT].x - mCoords[TOP_RIGHT].x) / MainActivity.SIZE,
                    mCoords[TOP_RIGHT].y + i * (mCoords[BOTTOM_RIGHT].y - mCoords[TOP_RIGHT].y) / MainActivity.SIZE,
                    mThirdsPaint);
        }
        // Draw handles
        for (int i = 0; i < 4; ++i) {
            canvas.drawCircle(mCoords[i].x, mCoords[i].y, VERTEX_TOUCH_RADIUS, mCornerHandlePaint);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mParentW = ((View) getParent()).getWidth();
        mParentH = ((View) getParent()).getHeight();
        init();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && mHitVertex == -1) {
            mHitVertex = getHit((int) event.getX(), (int) event.getY());
            if (mHitVertex != -1) return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (mHitVertex != -1) {
                mCoords[mHitVertex].x = (int) event.getX();
                mCoords[mHitVertex].y = (int) event.getY();
                invalidate();
                return true;
            }

        } else if (event.getAction() == MotionEvent.ACTION_UP && mHitVertex != -1) {
            mHitVertex = -1;
            return true;
        }
        return false;
    }

    /**
     * Returns the vertex that was hit or -1
     */
    private int getHit(int x, int y) {
        Log.d("HLView", "getHit():" + x + " " + y);
        for (int i = 0; i < 4; i++) {
            Log.d("HLView", "getHit() Coords[" + i + "]: " + mCoords[i].x + " " + mCoords[i].y);
            if (dist(mCoords[i].x, mCoords[i].y, x, y) < VERTEX_TOUCH_RADIUS) {
                Log.d("HLView", "Returning coordinate" + i);
                return i;
            }
        }
        return -1;
    }

    private float dist(float x1, float y1, float x2, float y2) {
        float dx = Math.abs(x1 - x2);
        float dy = Math.abs(y1 - y2);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

}