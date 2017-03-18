package com.example.sonis.customcamera;

/**
 * Created by SONI's on 7/10/2016.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class OnSwipeTouchListener implements OnTouchListener {

    //private final GestureDetector gestureDetector;
    static final int NONE = 0;
    static final int ZOOM = 2;
    int mode = NONE;
    int upOrDown=0;
    private static final float FOCUS_AREA_SIZE = 75f;
    float oldDist = 1f;
    Context ctx;
    int width,height;
    public static RectF rectF;
    public OnSwipeTouchListener (int width,int height,Context ctx){
    //    gestureDetector = new GestureDetector(ctx, new GestureListener());
        this.ctx=ctx;
        this.width=width;
        this.height=height;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {

            case MotionEvent.ACTION_UP: // second finger lifted
            //    mode = ZOOM;
                handelFocus(event);
                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down
                oldDist = spacing(event);
                break;

            case MotionEvent.ACTION_MOVE:

                //if (mode == ZOOM)
                {
                    float newDist = spacing(event);
                    Camera.Parameters parameters = LayoutCamera.camera.getParameters();
                    final int maxZoomLevel = parameters.getMaxZoom();
                    int currentZoomLevel=parameters.getZoom();
                    if (newDist > oldDist)
                    {

                        if(currentZoomLevel < maxZoomLevel){
                            currentZoomLevel++;
                            parameters.setZoom(currentZoomLevel);
                            LayoutCamera.camera.setParameters(parameters);
                        }
                    }
                    else if(newDist<oldDist)
                    {
                        if(currentZoomLevel>0)
                        {
                            currentZoomLevel--;
                            parameters.setZoom(currentZoomLevel);
                            LayoutCamera.camera.setParameters(parameters);
                        }
                    }
                }
                break;
        }

        return true;
    }

    private void handelFocus(MotionEvent event) {

        if (LayoutCamera.camera != null) {

            Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f);

            Camera.Parameters parameters = LayoutCamera.camera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
            if (parameters.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> mylist = new ArrayList<Camera.Area>();
                mylist.add(new Camera.Area(focusRect, 1000));
                parameters.setFocusAreas(mylist);
            }

            LayoutCamera.camera.setParameters(parameters);
            LayoutCamera.camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    camera.cancelAutoFocus();
                    Camera.Parameters params = camera.getParameters();
                    if(params.getFocusMode() != Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE){
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                        camera.setParameters(params);
                    }
                }
            });
        }
    }

    private Rect calculateTapArea(float x, float y, float coefficient) {

        int areaSize = Float.valueOf(FOCUS_AREA_SIZE * coefficient).intValue();

        int left = clamp((int) x - areaSize / 2, 0, width - areaSize);
        int top = clamp((int) y - areaSize / 2, 0, height - areaSize);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);

        return round(rectF);
    }

    private Rect round(RectF rect) {
        return new Rect(Math.round(rect.left), Math.round(rect.top), Math.round(rect.right), Math.round(rect.bottom));
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    private float spacing(MotionEvent event)
    {
        if(event.getPointerCount()>=2)
        {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        }
        else
        {
            return 0;
        }
    }

  /*  private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                    result = true;
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                }
                result = true;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }*/

}
