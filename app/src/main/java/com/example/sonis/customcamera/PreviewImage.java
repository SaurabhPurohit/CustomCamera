package com.example.sonis.customcamera;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

public class PreviewImage extends AppCompatActivity implements View.OnClickListener {

    private MediaRecorder myAudioRecorder;
    private ImageButton startRecord,stopRecord,viewImage;
    private String time;
    private String path=Environment.getExternalStorageDirectory().getAbsolutePath()+"/AudSnap/";
    private MediaPlayer mediaPlayer,videoMediaPlayer;
    private boolean isAudioRecording,isSoundPlaying;
    private int height,width,index;
    private Uri videoUri;
    private ImageView imageView;
    private TextureView videoView;

    private Surface s;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image);

        Uri uri = getIntent().getParcelableExtra("URIOFIMAGE");
       // videoUri = getIntent().getParcelableExtra("URIOFVIDEO");
        time = getIntent().getStringExtra("TIMEFORMAT");
        height=getIntent().getIntExtra("height",0);
        width=getIntent().getIntExtra("width",0);
        index=getIntent().getIntExtra("index",0);

        if(uri!=null)
        {
            imageView = (ImageView) findViewById(R.id.imageView);
            videoView = (TextureView) findViewById(R.id.videoView);
            videoView.setSurfaceTextureListener(surfaceTextureListener);
            //videoView.setSurfaceTextureListener(this);
            imageView.setImageURI(uri);
            //imageView.setOnTouchListener(new ZoomInZoomOut());
            startRecord = (ImageButton) findViewById(R.id.button_record_audio);
            startRecord.setOnClickListener(this);
            stopRecord = (ImageButton) findViewById(R.id.button_stop_audio);
            stopRecord.setOnClickListener(this);
            myAudioRecorder = new MediaRecorder();
            viewImage = (ImageButton) findViewById(R.id.button_view_image);
            viewImage.setOnClickListener(this);


            /*imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {


                   // startVideo();
                    imageView.setVisibility(View.GONE);
                    //videoView.setVisibility(View.VISIBLE);
                    if(index==0)
                    {
                        videoView.setRotation((float)270);
                    }
                    else{
                        videoView.setRotation((float)90);
                    }

                    if(s!=null)
                    {
                        setUpMediaPlayer(s);
                    }
                    *//*videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                videoView.setVisibility(View.GONE);
                                imageView.setVisibility(View.VISIBLE);
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            }
                    });*//*

                    //videoView.start();

                    return true;
                }
            });*/
        }

    }

    private void setUpMediaPlayer(Surface surface) {

        videoMediaPlayer=new MediaPlayer();
        try {
            videoMediaPlayer.setDataSource(PreviewImage.this,videoUri);
            videoMediaPlayer.setSurface(surface);
            videoMediaPlayer.prepare();
            videoMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        videoMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                videoMediaPlayer.release();
                videoMediaPlayer=null;
                videoView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.button_record_audio:
                try {
                    startRecord.setVisibility(View.GONE);
                    stopRecord.setVisibility(View.VISIBLE);
                    myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                    myAudioRecorder.setOutputFile(path+"/"+time+".3gp");
                    myAudioRecorder.prepare();
                    isAudioRecording=true;
                    myAudioRecorder.start();
                }catch (Exception e)
                {
                    Log.e("PreviewImage",e.getLocalizedMessage(),e);
                }
                break;
            case R.id.button_stop_audio:

                if(myAudioRecorder!=null)
                {
                    myAudioRecorder.stop();
                    isAudioRecording=false;
                    myAudioRecorder.release();
                    myAudioRecorder=null;
                    stopRecord.setVisibility(View.GONE);
                    startRecord.setVisibility(View.GONE);
                    viewImage.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.button_view_image:
                stopRecord.setVisibility(View.GONE);
                startRecord.setVisibility(View.GONE);
                viewImage.setVisibility(View.GONE);
                mediaPlayer=new MediaPlayer();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        viewImage.setVisibility(View.VISIBLE);
                        mediaPlayer.stop();
                        isSoundPlaying=false;
                        mediaPlayer.release();
                        mediaPlayer=null;
                    }
                });
                playSound();
                break;

        }
    }

    private void playSound()
    {
        try {
            isSoundPlaying=true;
            mediaPlayer.setDataSource(path+"/"+time+".3gp");
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(myAudioRecorder!=null && isAudioRecording)
        {
            myAudioRecorder.stop();
            myAudioRecorder.release();
            myAudioRecorder=null;
        }
        if(mediaPlayer!=null && isSoundPlaying)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(myAudioRecorder!=null && isAudioRecording)
        {
            myAudioRecorder.stop();
            myAudioRecorder.release();
            myAudioRecorder=null;
        }
        if(mediaPlayer!=null && isSoundPlaying)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(myAudioRecorder!=null && isAudioRecording)
        {
            myAudioRecorder.stop();
            myAudioRecorder.release();
            myAudioRecorder=null;
        }
        if(mediaPlayer!=null && isSoundPlaying)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
    }

    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

            s=new Surface(surface);

            int rotation = getWindowManager().getDefaultDisplay()
                    .getRotation();
            Matrix matrix = new Matrix();

            switch (rotation){

                case Surface.ROTATION_0:
                   // mCamera.setDisplayOrientation(90);
                    videoView.setLayoutParams(new FrameLayout.LayoutParams(
                            height, width, Gravity.CENTER));
                    if(index==0)
                    {
                        matrix.setScale(-1,1,height/2,0);
                        videoView.setScaleY(-1);
                        videoView.setTransform(matrix);
                    }
                    else {
                        matrix.setScale(-1,1,height/2,0);
                        videoView.setTransform(matrix);
                    }
                    break;

                case Surface.ROTATION_90:
                    videoView.setLayoutParams(new FrameLayout.LayoutParams(
                            height, width, Gravity.CENTER));
                    matrix.setScale(-1,1,width/2,0);
                    videoView.setTransform(matrix);

                    break;

                case Surface.ROTATION_180:
                    videoView.setLayoutParams(new FrameLayout.LayoutParams(
                            height, width, Gravity.CENTER));
                    matrix.setScale(-1,1,height/2,0);
                    videoView.setTransform(matrix);
                    break;

                case Surface.ROTATION_270:
                    videoView.setLayoutParams(new FrameLayout.LayoutParams(
                            height, width, Gravity.CENTER));
                    matrix.setScale(-1,1,width/2,0);
                    videoView.setTransform(matrix);
                    break;


            }
            setUpMediaPlayer(s);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
