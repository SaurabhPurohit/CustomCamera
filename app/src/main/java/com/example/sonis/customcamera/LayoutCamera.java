package com.example.sonis.customcamera;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.example.sonis.customcamera.database.DatabaseOperation;

import java.util.List;

/**
 * Created by SONI's on 7/8/2016.
 */
public class LayoutCamera extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG="LayoutCamera";
    private final SurfaceHolder surfaceHolder;
    public static Camera camera;
    private Camera.CameraInfo cameraInfo;
    private boolean isSurfaceCreated,isFlashActive;
    private MediaRecorder mediaRecorder;
    Context ctx;

    public LayoutCamera(Context context)
    {
        super(context);
        ctx=context;
        isSurfaceCreated=false;
        surfaceHolder=getHolder();
        surfaceHolder.addCallback(this);

    }

    public LayoutCamera(Context context, MediaRecorder mediaRecorder)
    {
        super(context);
        ctx=context;
        this.mediaRecorder=mediaRecorder;
        isSurfaceCreated=false;
        surfaceHolder=getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera, Camera.CameraInfo cameraInfo)
    {
        if(LayoutCamera.camera!=null)
        {
            try {
                LayoutCamera.camera.stopPreview();
            }catch (Exception e)
            {
                Log.e(TAG,"Error in stop preview",e);
            }
        }
        LayoutCamera.camera=camera;
        this.cameraInfo=cameraInfo;

        if(!isSurfaceCreated)
        {
            return;
        }

        if(camera!=null)
        {
            try
            {
                camera.setPreviewDisplay(surfaceHolder);
                configureCamera();
                camera.startPreview();
            }catch (Exception e)
            {
                Log.e(TAG,"could not start camera preview",e);
            }
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        isSurfaceCreated=true;

        if(camera!=null)
        {
            setCamera(camera,cameraInfo);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {


    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        if(camera==null || surfaceHolder.getSurface()==null)
        {
            return;
        }

        try {
            camera.stopPreview();
        }catch (Exception e)
        {
            Log.e(TAG,"Error in stop preview",e);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        widthMeasureSpec=resolveSize(getSuggestedMinimumWidth(),widthMeasureSpec);
        heightMeasureSpec=resolveSize(getSuggestedMinimumHeight(),heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
    }

    private void configureCamera()
    {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size targetPreviewSize = getClosetSize(getWidth(),getHeight(),parameters.getSupportedPreviewSizes());


        parameters.setPreviewSize(targetPreviewSize.width,targetPreviewSize.height);

        Camera.Size targetImageSize = getClosetSize(getWidth(),getHeight(),parameters.getSupportedPictureSizes());
        parameters.setPictureSize(targetImageSize.width,targetImageSize.height);

        camera.setDisplayOrientation(90);
        camera.setParameters(parameters);
    }


    private Camera.Size getClosetSize(int width, int height, List<Camera.Size> supportedPictureSizes)
    {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio =(double) height/width; //should be some problem here

        Camera.Size targetSize = null;
        double minDifference = Double.MAX_VALUE;

        for(Camera.Size size : supportedPictureSizes)
        {
            double ratio = (double) size.width/size.height;

            if(Math.abs(ratio-targetRatio)>ASPECT_TOLERANCE)
            {
                continue;
            }

            int heightDifference = Math.abs(size.height-height);
            if(heightDifference<minDifference)
            {
                targetSize=size;
                minDifference=heightDifference;
            }
        }

        if(targetSize==null)
        {
            minDifference=Double.MAX_VALUE;
            for(Camera.Size size : supportedPictureSizes)
            {
                int heightDifference = Math.abs(size.height-height);
                if(heightDifference<minDifference)
                {
                    targetSize=size;
                    minDifference=heightDifference;
                }
            }
        }

        return targetSize;

    }
}
