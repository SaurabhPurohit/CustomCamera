package com.example.sonis.customcamera;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.sonis.customcamera.database.DatabaseOperation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DemoCamera extends AppCompatActivity implements Camera.PictureCallback {


    private static final String TAG = "DEMOCAMERA";
    private static final String STATE_CAMERA_INDEX = "STATE_CAMERA_INDEX";

    private Camera camera;
    private Camera.CameraInfo cameraInfo;
    private int curentCameraIndex,bitmapHeight,bitmapWidth;
    private LayoutCamera layoutCamera;
    private ImageButton takePicture, switchCamera, flashOn, flashOff;
    private File customCameraDirectory,videofileLive,videofile,saveFile;
    private String path,time;
    private ProgressBar progressBar;
    private MediaRecorder mMediaRecorder;
    private boolean isLive=false;
    private AudioManager myAudioManager;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_demo_camera);
        myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        myAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM,true);
        myAudioManager.setStreamMute(AudioManager.STREAM_MUSIC,true);
        flashOn = (ImageButton) findViewById(R.id.button_flash_on);
        flashOff = (ImageButton) findViewById(R.id.button_flash_off);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        setFlashLightIfAvailable();
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AudSnap/";
        customCameraDirectory = new File(path);
        if (!customCameraDirectory.exists()) {
            customCameraDirectory.mkdirs();
        }
        if (savedInstanceState != null) {
            curentCameraIndex = savedInstanceState.getInt(STATE_CAMERA_INDEX);
        } else {
            curentCameraIndex = 0;
        }

        layoutCamera = new LayoutCamera(this);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_layout_camera);
        frameLayout.addView(layoutCamera, 0);

        int width = frameLayout.getWidth();
        int height = frameLayout.getHeight();
        frameLayout.setOnTouchListener(new OnSwipeTouchListener(width, height, DemoCamera.this));

        takePicture = (ImageButton) findViewById(R.id.button_take_piture);
        switchCamera = (ImageButton) findViewById(R.id.button_switch_camera);

        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture.setEnabled(false);
                takePiture();
            }
        });

        takePicture.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isLive=true;
                //initRecorder(isLive);
                return true;
            }
        });

        flashOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseOperation databaseOperation = new DatabaseOperation(DemoCamera.this);
                databaseOperation.updateFlashState(databaseOperation, 0);
                flashOn.setVisibility(View.INVISIBLE);
                flashOff.setVisibility(View.VISIBLE);
            }
        });

        flashOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseOperation databaseOperation = new DatabaseOperation(DemoCamera.this);
                databaseOperation.updateFlashState(databaseOperation, 1);
                flashOff.setVisibility(View.INVISIBLE);
                flashOn.setVisibility(View.VISIBLE);
            }
        });
    }


    private boolean getFlashInfoAndSetFlash(Context context) {

        DatabaseOperation databaseOperation = new DatabaseOperation(context);
        Cursor cursor = databaseOperation.getFlashState(databaseOperation);
        if (cursor != null) {
            cursor.moveToFirst();
            int getState = cursor.getInt(0);
            if (getState == 1) {
                return true;
            } else {
                return false;
            }

        } else {
            return true;
        }

    }


    private void initRecorder(final boolean isLive,Camera camera) {
        try {
            releaseMediaRecorder();
            camera.stopPreview();
            camera.unlock();

            videofileLive = new File("/sdcard/videocapture2.mp4");
            videofile=new File("/sdcard/videocapture1.mp4");

            if(mMediaRecorder==null)
                mMediaRecorder=new MediaRecorder();

            mMediaRecorder.setCamera(camera);

            // Step 2: Set sources
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
            mMediaRecorder.setProfile(CamcorderProfile
                    .get(curentCameraIndex,CamcorderProfile.QUALITY_HIGH));

            // Step 4: Set output file
            if(isLive)
                mMediaRecorder.setOutputFile(videofileLive.getAbsolutePath());
            else
                mMediaRecorder.setOutputFile(videofile.getAbsolutePath());
            // Step 5: Set the preview output
            mMediaRecorder.setPreviewDisplay(layoutCamera.getHolder().getSurface());
            // Step 6: Prepare configured MediaRecorder
            mMediaRecorder.setMaxDuration(3 * 1000);


            mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {

                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {

                        //camera.stopPreview();
                        //releaseMediaRecorder();

                        if(isLive)
                        {
                            Toast.makeText(DemoCamera.this,"Live Completed",Toast.LENGTH_SHORT).show();
                            releaseMediaRecorder();
                            Intent intent = new Intent(DemoCamera.this,PreviewImage.class);
                            intent.putExtra("URIOFIMAGE", Uri.fromFile(saveFile));
                            //intent.putExtra("URIOFVIDEO",Uri.fromFile(videofileLive));
                            intent.putExtra("height",bitmapHeight);
                            intent.putExtra("width",bitmapWidth);
                            intent.putExtra("index",curentCameraIndex);
                            intent.putExtra("TIMEFORMAT",time);
                            startActivity(intent);
                        }
                        else{
                           /* if(videofile.exists()){
                                boolean result = videofile.delete();
                                if(result){
                                    releaseMediaRecorder();
                                    initRecorder(isLive);
                                }

                            }*/

                        }
                       /* Intent intent = new Intent();
                        intent.putExtra(CuxtomIntent.FILE_PATH,
                                videofile.getPath());
                        intent.putExtra(CuxtomIntent.FILE_TYPE, FILE_TYPE.VIDEO);
                        setResult(RESULT_OK, intent);
                        finish();*/
                    }

                }
            });
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset(); // clear mMediaRecorder configuration
            mMediaRecorder.release(); // release the mMediaRecorder object
            mMediaRecorder = null;
        }
    }


    private void setFlashLightIfAvailable() {

        boolean isAvailable = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if(!isAvailable)
        {
            flashOn.setVisibility(View.GONE);
            flashOff.setVisibility(View.GONE);
        }
    }

    private void switchCamera() {

        if(curentCameraIndex==0)
            curentCameraIndex=1;
        else if(curentCameraIndex==1)
            curentCameraIndex=0;

        establishCamera();
    }

    private void takePiture()
    {
        Camera.Parameters parameters = LayoutCamera.camera.getParameters();

        if(getFlashInfoAndSetFlash(DemoCamera.this) && cameraInfo.facing== Camera.CameraInfo.CAMERA_FACING_BACK)
        {
            //parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        }
        else if(!getFlashInfoAndSetFlash(DemoCamera.this) && cameraInfo.facing== Camera.CameraInfo.CAMERA_FACING_BACK)
        {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        else if(cameraInfo.facing==Camera.CameraInfo.CAMERA_FACING_FRONT)
        {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        progressBar.setVisibility(View.VISIBLE);
        takePicture.setVisibility(View.GONE);
        switchCamera.setVisibility(View.GONE);
        LayoutCamera.camera.setParameters(parameters);

        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                camera.takePicture(null,null,DemoCamera.this);
            }
        });

        takePicture.setEnabled(true);

    }

    @Override
    public void onPictureTaken(final byte[] data, final Camera camera) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                initRecorder(true,camera);
            }
        });

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                Bitmap bitmap = processBitmap(data);
                ByteArrayOutputStream byteArrayOutputStream =  new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.JPEG,90,byteArrayOutputStream);
                saveFile = new File(path,"/"+time+".jpg");

                try {

                    FileOutputStream fileOutputStream1 = new FileOutputStream(saveFile);
                    fileOutputStream1.write(byteArrayOutputStream.toByteArray());
                    fileOutputStream1.close();
                    Intent intent = new Intent(DemoCamera.this,PreviewImage.class);
                    intent.putExtra("URIOFIMAGE", Uri.fromFile(saveFile));
                    //intent.putExtra("URIOFVIDEO",Uri.fromFile(videofileLive));
                    intent.putExtra("height",bitmapHeight);
                    intent.putExtra("width",bitmapWidth);
                    intent.putExtra("index",curentCameraIndex);
                    intent.putExtra("TIMEFORMAT",time);
                    startActivity(intent);


                } catch (final FileNotFoundException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            e.printStackTrace();
                            Toast.makeText(getBaseContext(),"Image not Saved",Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            e.printStackTrace();
                            Toast.makeText(getBaseContext(),"Image not Saved",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                bitmap.recycle();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });

            }
        });

        try {
            /*t.start();
            t.join();*/
            thread.start();
        }catch (Exception e)
        {

        }
    }

    private Bitmap processBitmap(byte[] data)
    {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
        if(bitmap.getWidth()>bitmap.getHeight())
        {
            float scale =  (float) bitmap.getHeight()/bitmap.getWidth();
            int finalWidth = (int)(bitmap.getHeight()*scale);
            Bitmap bitmaResize = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth(),bitmap.getHeight(),false);
            if(bitmaResize!=bitmap)
            {
                bitmap.recycle();
                bitmap=bitmaResize;
            }

            //if front camera is active make a mirror image and then rotate to 90 degree (Transformation followed by Rotation)
            //if back camera is active rotate it to 90 degree(Rotation only)

            Matrix matrix = new Matrix();
            if(cameraInfo.facing==Camera.CameraInfo.CAMERA_FACING_FRONT)
            {
                Matrix matrixInfo = new Matrix();
                matrixInfo.setValues(new float[]{
                        -1,0,0,
                         0,1,0,
                         0,0,1

                });
                matrix.postConcat(matrixInfo);

            }

            matrix.postRotate(90);
            Bitmap proceedBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
            bitmapHeight=proceedBitmap.getHeight();
            bitmapWidth=proceedBitmap.getWidth();
            if(bitmap != proceedBitmap)
            {
                bitmap.recycle();
            }
            return proceedBitmap;
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        establishCamera();
    }

    @Override
    protected void onStart() {
        super.onStart();
        takePicture.setVisibility(View.VISIBLE);
        switchCamera.setVisibility(View.VISIBLE);
    }

    private void establishCamera()
    {
   //     releaseMediaRecorder();
        if(camera!=null)
        {
            layoutCamera.setCamera(null,null);
            camera.release();
            camera=null;
        }

        try
        {
            camera=Camera.open(curentCameraIndex);
            cameraInfo=new Camera.CameraInfo();
            Camera.getCameraInfo(curentCameraIndex,cameraInfo);
            layoutCamera.setCamera(camera,cameraInfo);

            if(cameraInfo!=null)
            {
                if(curentCameraIndex==1)
                {
                    flashOff.setVisibility(View.GONE);
                    flashOn.setVisibility(View.GONE);
                }
                else
                {
                    if(getFlashInfoAndSetFlash(DemoCamera.this))
                    {
                        flashOn.setVisibility(View.VISIBLE);
                    } else
                    {
                        flashOff.setVisibility(View.VISIBLE);
                    }
                }
            }

           /* if(!isLive)
                initRecorder(isLive);*/
        }catch (Exception e)
        {
            Log.e(TAG,"Could not open camera"+curentCameraIndex,e);
            Toast.makeText(this,"Error establishing camera",Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        if(camera!=null)
        {
            layoutCamera.setCamera(null,null);
            camera.release();
            camera=null;
        }
        if(mMediaRecorder!=null)
        {
            try {
                mMediaRecorder.stop();
                releaseMediaRecorder();
            }catch (Exception e)
            {

            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CAMERA_INDEX,curentCameraIndex);
    }

}
