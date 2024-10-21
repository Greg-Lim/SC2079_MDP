package com.example.controllerapp.customlayouts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;

public class ZoomableLayout extends FrameLayout {
    // private ScaleGestureDetector scaleDetector;
    private Paint borderPaint;
    private Matrix matrix = new Matrix();
    private float[] lastEvent;
    private float scale = 1f;
    private float startX = 0f, startY = 0f;
    private PointF startPoint = new PointF();
    private PointF midPoint = new PointF();
    private boolean isDragging = false;

    private final float MIN_SCALE = 1.0f;
    private final float MAX_SCALE = 2.0f;

    public ZoomableLayout(Context context) {
        super(context);
        init(context);
    }

    public ZoomableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        //scaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        // Initialize the Paint object for the border
        borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);  // Set the border color
        borderPaint.setStrokeWidth(10);     // Set the border width
        borderPaint.setStyle(Paint.Style.STROKE);  // Set the style to draw only the border (stroke)
    }


    public boolean translationEvent(MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startPoint.set(event.getX() - startX, event.getY() - startY);
                isDragging = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    float x = event.getX() - startPoint.x;
                    float y = event.getY() - startPoint.y;

                    matrix.setTranslate(x, y);
                    applyMatrix();
                    startX = x;
                    startY = y;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                break;
        }

        return true;
    }

    private void applyMatrix() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.setScaleX(scale);
            child.setScaleY(scale);
            child.setTranslationX(startX);
            child.setTranslationY(startY);
        }
    }

    public void zoomIn(){
        float zoomStep = 0.2f;
        scale = Math.max(MIN_SCALE, Math.min(scale + zoomStep, MAX_SCALE));
        applyMatrix();
    }

    public void zoomOut(){
        float zoomStep = 0.2f;
        scale = Math.max(MIN_SCALE, Math.min(scale - zoomStep, MAX_SCALE));
        applyMatrix();
    }

    public float getScale(){
        return scale;
    }

    /**
     * Used to show the border of the grid interaction area
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        // Draw the border around the layout
        float left = 0f;
        float top = 0f;
        float right = getWidth();
        float bottom = getHeight();
        canvas.drawRect(left, top, right, bottom, borderPaint);
    }

//    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
//        @Override
//        public boolean onScale(ScaleGestureDetector detector) {
//            scale *= detector.getScaleFactor();
//            scale = Math.max(MIN_SCALE, Math.min(scale, MAX_SCALE));
//
//            applyMatrix();
//            return true;
//        }
//    }
}