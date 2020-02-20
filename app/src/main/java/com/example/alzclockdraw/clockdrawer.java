/*Created by Nisanth Mathew James for Hochschule Anhalt - 2020
* This class takes care of drawing.*/

package com.example.alzclockdraw;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

public class clockdrawer extends View {
    private static final float MINP = 0.25f;
    private static final float MAXP = 0.75f;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint mPaint;
    private Rect mRectangle;
    private Paint mGridPaint;
    private Paint mGuidelinePaint;
    private Paint mlayout;
    private Paint textpaint;
    private Paint textpaint2;

    public clockdrawer(drawingspace c) {
        super(c);

        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFF000000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(7);

        textpaint = new Paint();
        textpaint.setColor(Color.MAGENTA);
        textpaint.setTextSize(40);

        textpaint2 = new Paint();
        textpaint2.setColor(Color.MAGENTA);
        textpaint2.setTextSize(30);

        mGridPaint = new Paint();
        mGridPaint.setStyle(Paint.Style.STROKE);
        mGridPaint.setColor(Color.LTGRAY);
        mGridPaint.setStrokeWidth(2);

        mlayout = new Paint();
        mlayout.setStyle(Paint.Style.STROKE);
        mlayout.setColor(Color.BLACK);
        mlayout.setStrokeWidth(3);

        mGuidelinePaint = new Paint();
        mGuidelinePaint.setStyle(Paint.Style.STROKE);
        mGuidelinePaint.setColor(Color.LTGRAY);
        mGuidelinePaint.setStrokeWidth(2);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap( w, h, Bitmap.Config.ARGB_8888);
        mRectangle = new Rect(10,10, (int) (w*0.69), (int) (h*0.98));

        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        /**************************drawing axis and layouts***************************************/
        final int height = getHeight();
        final int width = getWidth();
        final float gridLeft = (float) (width*0.7);
        final float gridBottom_histogramx = height*0.7f;
        final float gridBottom_histogramy = height*0.3f;
        final float gridTop_histogramy = 0;
        final float gridTop_histogramx = height*0.4f;
        final float gridRight = width;


        // Draw Grid Lines


        float x;
        for (int i = 0; i < 5; i++) {
            x = gridBottom_histogramx - i * 100;
            canvas.drawLine(gridLeft, x, gridRight, x, mGuidelinePaint);
        }

        for (int i = 0; i < 8; i++) {
            x = gridLeft + i * 100;
            canvas.drawLine(x, gridTop_histogramx, x, gridBottom_histogramx, mGuidelinePaint);
        }

        float y;
        for (int i = 0; i < 5; i++) {
            y = gridBottom_histogramy - i * 100;
            canvas.drawLine(gridLeft, y, gridRight, y, mGuidelinePaint);
        }

        for (int i = 0; i < 8; i++) {
            y = gridLeft + i * 100;
            canvas.drawLine(y, gridTop_histogramy, y, gridBottom_histogramy, mGuidelinePaint);
        }

        canvas.drawLine( (float) (width*0.7), 0, (float) (width*0.7), (float) (height),mlayout);
        canvas.drawLine((float) (width * 0.7), (float) (height * 0.7), (float) (width), (float) (height * 0.7), mlayout);
        canvas.drawLine((float) (width * 0.7), (float) (height * 0.4), (float) (width), (float) (height * 0.4), mlayout);
        canvas.drawLine( (float) (width*0.7), (float) (height*0.3), (float) (width), (float) (height*0.3),mlayout);

        canvas.drawText("Histogram X axis",(float) (width*0.85), (float) (height*0.45), textpaint);
        canvas.drawText("Column number",(float) (width*0.79), (float) (height*0.75), textpaint2);
        canvas.save();
        canvas.rotate(-90f, (float) (width*0.69), (float) (height*0.60));
        canvas.drawText("pixel count",(float) (width*0.69), (float) (height*0.6), textpaint2);
        canvas.restore();

        canvas.drawText("Histogram Y axis", (float) (width * 0.85), (float) (height * 0.05), textpaint);
        canvas.drawText("row number",(float) (width*0.79), (float) (height*0.35), textpaint2);
        canvas.save();
        canvas.rotate(-90f, (float) (width*0.69), (float) (height*0.25));
        canvas.drawText("pixel count",(float) (width*0.69), (float) (height*0.25), textpaint2);
        canvas.restore();

        canvas.drawBitmap(mBitmap,mRectangle,mRectangle,mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate(); //to force a view to draw
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }
}


