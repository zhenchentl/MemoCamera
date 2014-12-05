package me.markchen.memocamera;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import me.markchen.util.Log;
import me.markchen.view.CameraSurfaceView;
import me.markchen.view.SmartImageButton;


public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraSurfaceView mPreview;
    boolean isBackCamera = true;
    SmartImageButton btn_take_picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initTakePhotoBtnForDrag();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean cameraOpened = safeCameraOpenInView(Camera.CameraInfo.CAMERA_FACING_BACK);
        if (!cameraOpened) {
            Toast.makeText(this, R.string.open_camera_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();
//        releaseCamera();
    }
    private void initTakePhotoBtnForDrag(){
        btn_take_picture = (SmartImageButton)findViewById(R.id.btnTakePhoto);
        SharedPreferences sharedPreferences = this.getSharedPreferences("SmartImgPosition",
                this.MODE_PRIVATE);
        int X = sharedPreferences.getInt("lastX", -100);
        int Y = sharedPreferences.getInt("lastY", -100);
        if (X > 0){
            LayoutParams param = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            param.setMargins(X, Y, 0, 0);
            btn_take_picture.setLayoutParams(param);
        }
//        findViewById(R.id.btn_camera_exchange).setOnClickListener(click_camera_exchange);
        btn_take_picture.setOnClickListener(click_take_picture);
    }
    private View.OnClickListener click_take_picture = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPreview.takePicture();
        }
    };
    private View.OnClickListener click_camera_exchange = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            releaseCamera();
            boolean cameraOpened;
            if(isBackCamera){
                isBackCamera = false;
                cameraOpened = safeCameraOpenInView(Camera.CameraInfo.CAMERA_FACING_FRONT);
            }else{
                isBackCamera = true;
                cameraOpened = safeCameraOpenInView(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
            if (!cameraOpened) {
                Toast.makeText(CameraActivity.this, R.string.open_camera_failed,
                        Toast.LENGTH_SHORT).show();
            }
        }
    };
    /**
     * Check if this device has a camera
     *
     * @param context
     * @return boolean
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get a camera instance
     *
     * @return Camera
     */
    private Camera getCameraInstance(int cameraID) {
        Camera c = null;
        try {
            c = Camera.open(cameraID);
        } catch (Exception e) {
            Log.e("open camera failed", e);
            e.printStackTrace();
        }
        return c;
    }

    /**
     * Open camera in CamerasurfaceView safely.
     *
     * @return boolean
     */
    private boolean safeCameraOpenInView(int CAMERA_FACING_INDEX) {
        boolean qOpened;
        if (checkCameraHardware(this)) {
            int cameraCount = Camera.getNumberOfCameras();
            int camIdx = 0;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for(; camIdx < cameraCount; camIdx ++){
                if(cameraInfo.facing == CAMERA_FACING_INDEX){
                    break;
                }
            }
            Log.i("cameaID:" + camIdx);
            mCamera = getCameraInstance(camIdx);
            qOpened = (mCamera != null);
            if (qOpened) {
                mPreview = new CameraSurfaceView(this, mCamera);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_view);
                preview.addView(mPreview);
            }
            return qOpened;
        } else {
            return false;
        }
    }


    private void releaseMediaRecorder() {

    }

    /**
     * release camera resource.
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

}
