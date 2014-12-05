package me.markchen.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.markchen.memocamera.PicEditActivity;
import me.markchen.util.Log;
import me.markchen.util.MemoCameraConstants;

/**
 * Created by 振 on 2014/11/23.
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Camera mCamera;
    Context mContext;
    int viewWidth;
    int viewHeight;
    ToneGenerator tone;

    private Camera.ShutterCallback shutter = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            if (tone == null){
                tone = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
                tone.startTone(ToneGenerator.TONE_PROP_BEEP2);
            }
        }
    };

    private Camera.PictureCallback raw = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

        }
    };

    private Camera.PictureCallback jpeg = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                Bundle bundle = new Bundle();
                bundle.putByteArray("photo", data);
                Intent intent = new Intent(mContext, PicEditActivity.class);
                intent.putExtras(bundle);
//                saveToSDCard(data);
                mContext.startActivity(intent);
            } catch (Exception e){
                Log.e("PictureCallback",e);
                e.printStackTrace();
            }
//            mCamera.startPreview();
        }
    };

    public CameraSurfaceView(Context context, Camera camera) {
        super(context);
        mContext = context;
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * Save photo to SD card
     * @param data
     * @throws IOException
     */
    public static void saveToSDCard(byte[] data) throws IOException{
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat(MemoCameraConstants.PATH_PHOTO_DATE_FORMATE);
        String filename = MemoCameraConstants.PATH_PHOTO_FILE_PREFIX + format.format(date)
                + MemoCameraConstants.PATH_PHOTO_FILE_TYPE;
        File fileFolder = new File(Environment.getExternalStorageDirectory()
                + MemoCameraConstants.PATH_PHOTO_FILE_FOLDER);
        if(!fileFolder.exists()){
            fileFolder.mkdir();
        }
        File photoFile = new File(fileFolder,filename);
        FileOutputStream outputStream = new FileOutputStream(photoFile);
        outputStream.write(data);
        outputStream.close();

    }
    /**
     * Take a picture. After focus.
     */
    public void takePicture() {
        mCamera.autoFocus(null);
        Log.i("taking photos...");
        mCamera.takePicture(shutter, null, jpeg);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e("surfaceCreated",e);
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null) {
            return;
        }
        try {
            mCamera.stopPreview();
        } catch (Exception e) {

        }
        try {
            mCamera.setPreviewDisplay(mHolder);
            viewWidth = width;
            viewHeight = height;
            updateCameraParameters();
            mCamera.startPreview();
        } catch (Exception e) {
            Log.e("surfaceChanged",e);
            e.printStackTrace();
        }
    }

    /**
     * Update the camera parameters.
     */
    private void updateCameraParameters() {
        if (mCamera != null) {
            Parameters p = mCamera.getParameters();
            long time = new Date().getTime();
            p.setGpsTimestamp(time);
            Size previewSize = findBestPreviewSize(p);
            p.setPreviewSize(previewSize.width, previewSize.height);
            // size of picture, which can be defined any size. but have the same radio with preview.
            p.setPictureSize(previewSize.width * 2, previewSize.height * 2);
            // Set the preview frame aspect ratio according to the picture size.
//            frameLayout.setAspectRatio((double) previewSize.width / previewSize.height);
            if (mContext.getResources().getConfiguration().orientation
                    != Configuration.ORIENTATION_LANDSCAPE) {
                mCamera.setDisplayOrientation(90);
                p.setRotation(90);
            }
            mCamera.setParameters(p);
        }
    }

    /**
     * Find the most suitable ratio. (To avoid deformation of picture)
     *
     * @param parameters
     * @return
     */
    private Size findBestPreviewSize(Camera.Parameters parameters) {
        // Find all ratios that system support.
        String previewSizeValueString = null;
        previewSizeValueString = parameters.get("preview-size-values");
        if (previewSizeValueString == null) {
            previewSizeValueString = parameters.get("preview-size-value");
        }
        if (previewSizeValueString == null) { // 有些手机例如m9获取不到支持的预览大小 就直接返回屏幕大小
            return mCamera.new Size(getScreenWH().widthPixels, getScreenWH().heightPixels);
        }
        float bestX = 0;
        float bestY = 0;

        float tmpRatio = 0;
        float viewRatio = 0;

        if (viewWidth != 0 && viewHeight != 0) {
            viewRatio = Math.min((float) viewWidth, (float) viewHeight)
                    / Math.max((float) viewWidth, (float) viewHeight);
        }
        String[] COMMA_PATTERN = previewSizeValueString.split(",");
        for (String prewsizeString : COMMA_PATTERN) {
            prewsizeString = prewsizeString.trim();
            int dimPosition = prewsizeString.indexOf('x');
            if (dimPosition == -1) {
                continue;
            }

            float newX = 0;
            float newY = 0;

            try {
                newX = Float.parseFloat(prewsizeString.substring(0, dimPosition));
                newY = Float.parseFloat(prewsizeString.substring(dimPosition + 1));
            } catch (NumberFormatException e) {
                continue;
            }

            float radio = Math.min(newX, newY) / Math.max(newX, newY);
            if (tmpRatio == 0) {
                tmpRatio = radio;
                bestX = newX;
                bestY = newY;
            } else if (tmpRatio != 0 && (Math.abs(radio - viewRatio)) < (Math.abs(tmpRatio - viewRatio))) {
                tmpRatio = radio;
                bestX = newX;
                bestY = newY;
            }
        }

        if (bestX > 0 && bestY > 0) {
            return mCamera.new Size((int) bestX, (int) bestY);
        }
        return null;
    }

    protected DisplayMetrics getScreenWH() {
        DisplayMetrics dMetrics = new DisplayMetrics();
        dMetrics = this.getResources().getDisplayMetrics();
        return dMetrics;
    }

}
