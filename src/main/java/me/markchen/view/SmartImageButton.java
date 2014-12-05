package me.markchen.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import me.markchen.memocamera.R;
import me.markchen.util.Log;

/**
 * Created by 振 on 2014/11/26.
 */
public class SmartImageButton extends ImageButton implements View.OnTouchListener{

    private static final int POS_BOTTOM = 0;
    private static final int POS_LEFT = 1;
    private static final int POS_RIGHT = 2;
    boolean isClick;
    int startX;
    int startY;
    int startPos;
    int targetPos;
    int preX;
    int preY;
    int screenWidth;
    int screenHeight;
    Context mContext;
    SharedPreferences sharedPreferences;

    public  SmartImageButton(Context context, AttributeSet attrs){
        super(context, attrs);
        mContext = context;
        DisplayMetrics dm;
        dm = mContext.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        sharedPreferences = mContext.getSharedPreferences("SmartImgPosition",
                mContext.MODE_PRIVATE);
        this.setOnTouchListener(this);
    }

    public SmartImageButton(Context context) {
        super(context);

    }

    private Position getPosition(int pos) {
        Position position = new Position();
        switch (pos) {
            case POS_LEFT:
                position.X = (int) getResources().getDimension(R.dimen.btn_take_photo_margin);
                position.Y = (int) this.screenHeight / 3
                        - (int) this.getHeight() / 2;
                break;
            case POS_RIGHT:
                position.X = this.screenWidth
                        - (int) getResources().getDimension(R.dimen.btn_take_photo_margin)
                        - (int) this.getWidth();
                position.Y = (int) this.screenHeight / 3
                        - (int) this.getHeight() / 2;
                break;
            default:
                position.X = (int) this.screenWidth / 2
                        - (int) this.getWidth() / 2;
                position.Y = this.screenHeight
                        - (int) getResources().getDimension(R.dimen.btn_take_photo_margin)
                        - (int) this.getHeight();
                break;
        }
        return position;
    }

    public boolean onTouch(View v, MotionEvent event) {
//        Log.i("on Touching...");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.startX = this.preX = (int) event.getRawX();
                this.startY = this.preY = (int) event.getRawY();
                this.startPos = positioning(this.startX, this.startY);
                this.isClick = true;
                break;
            case MotionEvent.ACTION_MOVE:
                int newX = (int) event.getRawX();
                int newY = (int) event.getRawY();
                if (Math.abs(newX - this.startX) * Math.abs(newY - this.startY) > 100 ){
                    this.isClick = false;
                }
                this.targetPos = positioning(newX, newY);
                Position p;
                if (this.targetPos != this.startPos) {
                    p = getPosition(this.targetPos);
                    LayoutParams param = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    param.setMargins(p.X, p.Y, 0, 0);
                    this.setLayoutParams(param);
                } else {
                    int dx = newX - this.preX;
                    int dy = newY - this.preY;

                    int l = this.getLeft();
                    int r = this.getRight();
                    int t = this.getTop();
                    int b = this.getBottom();

                    int newt = t + dy;
                    int newb = b + dy;
                    int newl = l + dx;
                    int newr = r + dx;

                    if ((newl < 0) || (newt < 0)
                            || (newr > this.screenWidth)
                            || (newb > this.screenHeight)) {
                        break;
                    }
                    this.layout(newl, newt, newr, newb);

                    this.preX = (int) event.getRawX();
                    this.preY = (int) event.getRawY();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (positioning((int) event.getRawX(), (int)event.getRawY()) == this.startPos) {
                    this.targetPos = this.startPos;
                    p = getPosition(this.targetPos);
                    LayoutParams param = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    param.setMargins(p.X, p.Y, 0, 0);
                    this.setLayoutParams(param);
                }
                p = getPosition(this.targetPos);
                SharedPreferences.Editor editor = this.sharedPreferences.edit();
                editor.putInt("lastX", p.X);
                editor.putInt("lastY", p.Y);
                editor.commit();
                break;
        }
        // return true: no onClick and onLongClick. return false: with onClick and onLongClick.
//        if (this.isClick)
//            Log.i("true");
//        else
//            Log.i("false");
        return !this.isClick;
    }

    private int positioning(int X, int Y) {
        if (Y > this.screenHeight * 2 / 3) {
            return POS_BOTTOM;
        } else if (X < this.screenWidth / 2) {
            return POS_LEFT;
        } else {
            return POS_RIGHT;
        }
    }

    private class Position {
        /*
        The position of SmartImageButton's top left corner
         */
        public int X;
        public int Y;
    }
}
