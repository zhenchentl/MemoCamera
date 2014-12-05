package me.markchen.memocamera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collection;

import me.markchen.util.Log;


public class PicEditActivity extends Activity {

    boolean isTextEditing;
    private float Text_X;
    private float Text_Y;
    private ImageView mImageView;
    private Bitmap mBitmap;
    private LinearLayout mLin_edit;
    private EditText mEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_edit);
        mImageView = (ImageView)findViewById(R.id.img_edit);
        mLin_edit = (LinearLayout)findViewById(R.id.Lin_edit);
        mEditText = (EditText)findViewById(R.id.edit_text);
        isTextEditing = false;
        mImageView.setOnClickListener(onClickListener);
        mImageView.setOnTouchListener(onTouchListener);
        findViewById(R.id.btn_clip).setOnClickListener(onClickListener);
        findViewById(R.id.btn_pen).setOnClickListener(onClickListener);
        mEditText.setOnEditorActionListener(onEditorActionListener);
        Bundle bundle = getIntent().getExtras();
        byte[] data = bundle.getByteArray("photo");
        mBitmap = Bytes2Bimap(data);
        mImageView.setImageBitmap(Bytes2Bimap(data));
        drawNewBitmap("Hello world!");
    }
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()){
                case R.id.img_edit:
                    if (isTextEditing) {
                        Log.i("show text editing...");
                        Text_X = event.getRawX();
                        Text_Y = event.getRawY();
                        Log.i(Text_X + ":" + Text_Y);
                        mEditText.setVisibility(View.VISIBLE);
                    }
                    break;
            }
            return false;
        }
    };
    private TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE){
                Log.i("drawing...");
                drawNewBitmap(v.getText().toString().trim());
                mEditText.setVisibility(View.GONE);
                isTextEditing = false;
            }
            return false;
        }
    };
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.img_edit:
                    Log.i("img_edit clicked...");

                    break;
                case R.id.btn_clip:
                    Log.i("btn_clip clicked...");
                    break;
                case R.id.btn_pen:
                    Log.i("btn_pen clicked...");
                    isTextEditing = true;
                    break;
            }
        }
    };
    @Override
    public void onBackPressed(){
        if (isTextEditing){
            mEditText.setVisibility(View.GONE);
        }else{
            super.onBackPressed();
        }

    }
    public Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    private void drawNewBitmap(String str){
        int width = mBitmap.getWidth();
        int hight = mBitmap.getHeight();
        Bitmap icon = Bitmap.createBitmap(width, hight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(icon);

        Paint photoPaint = new Paint();
        photoPaint.setDither(true);
        photoPaint.setFilterBitmap(true);

        Rect src = new Rect(0,0,mBitmap.getWidth(), mBitmap.getHeight());
        Rect dst = new Rect(0,0,width,hight);
        canvas.drawBitmap(mBitmap,src,dst,photoPaint);
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
        textPaint.setTextSize(100.0f);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setColor(Color.RED);
        canvas.drawText(str, Text_X,Text_Y,textPaint);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        mImageView.setImageBitmap(icon);

    }

}
