

package com.sprd.validationtools.itemstest.tp;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * @author guoss
 */
public class SingleTouchPointTestEx extends BaseActivity {
    private static final int VERTICAL_COUNT = 13;
    private static final int HORIZONTAL_COUNT = 9;

    private int mBoxWidth;
    private int mBoxHeight;
    private Box[] mBoxes;
    private int mTotalPassCount;
    private int mCurPassCount;
    private boolean[] mHasPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        FrameLayout rootLayout = (FrameLayout) findViewById(R.id.root);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        //mBoxWidth = screenWidth / HORIZONTAL_COUNT;	//screenWidth / (HORIZONTAL_COUNT + 1);
        //mBoxHeight = screenHeight / VERTICAL_COUNT;	//(VERTICAL_COUNT + 1);
        //int preBoxSpace = 4;//mBoxWidth / 20;
        //int boxLeft = 4;	//(mBoxWidth - (HORIZONTAL_COUNT - 1) * preBoxSpace) / 2;
        //int boxTop = 4;	//(mBoxHeight - (VERTICAL_COUNT - 1) * preBoxSpace) / 2;
		
		
		int preBoxSpace = 4;//mBoxWidth / 20;
        int boxLeft = 4;	//(mBoxWidth - (HORIZONTAL_COUNT - 1) * preBoxSpace) / 2;
        int boxTop = 4;	//(mBoxHeight - (VERTICAL_COUNT - 1) * preBoxSpace) / 2;
		mBoxWidth = (screenWidth - preBoxSpace * (HORIZONTAL_COUNT + 1)) / HORIZONTAL_COUNT;
        mBoxHeight = (screenHeight - preBoxSpace * (VERTICAL_COUNT + 1)) / VERTICAL_COUNT;

		
        Log.d("huangcx", "ScreenWidth = " + screenWidth + ", ScreenHeight = " + screenHeight
                + ", mBoxWidth = " + mBoxWidth + ", mBoxHeight = " + mBoxHeight + ", mPreBoxSpace = " + preBoxSpace
                + ", mBoxLeft = " + boxLeft + ", mBoxTop = " + boxTop);

        mTotalPassCount = VERTICAL_COUNT * 5 + (HORIZONTAL_COUNT - 5) * 7;
        mBoxes = new Box[mTotalPassCount];
        mHasPass = new boolean[mTotalPassCount];

        int nLoop = 0;

        for (int h = 0; h < HORIZONTAL_COUNT; h++) {
            for (int v = 0; v < VERTICAL_COUNT; v++) {
                int left = boxLeft + h * (mBoxWidth + preBoxSpace);
                int right = boxLeft + h * (mBoxWidth + preBoxSpace) + mBoxWidth;
				if(h == HORIZONTAL_COUNT - 1){
					right = screenWidth - preBoxSpace;	//reset the last grid's right
				}
                int top = boxTop + v * (mBoxHeight + preBoxSpace);
                int button = boxTop + v * (mBoxHeight + preBoxSpace) + mBoxHeight;
				if(v == VERTICAL_COUNT - 1){
					button = screenHeight - preBoxSpace;	//reset the last grid's right
				}
                if ((0 == h) || (2 == h) || (4 == h) || (6 == h) || (8 == h)
                        || (0 == v) || (2 == v) || (4 == v) || (6 == v) || (8 == v) || (10 == v) || (12 == v)) {
                    mBoxes[nLoop] = new Box(this, left, top, right, button);
                    rootLayout.addView(mBoxes[nLoop]);
                    nLoop++;
                }

            }
        }

        super.removeButton();
    }
    @Override
    public void onBackPressed() {
        finish();
    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        for (int nLoop = 0; nLoop < mTotalPassCount; nLoop++) {
            if (mHasPass[nLoop]) {
                mBoxes[nLoop].setPassColor();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (MotionEvent.ACTION_MOVE == event.getAction()) {
            int curX = (int) event.getX();
            int curY = (int) event.getY();

            for (int nLoop = 0; nLoop < mBoxes.length; nLoop++) {
                int boxLeftPos = mBoxes[nLoop].getLeftPos();
                int boxTopPos = mBoxes[nLoop].getTopPos();
                int boxRightPos = boxLeftPos + mBoxWidth;
                int boxButtonPos = boxTopPos + mBoxHeight;

                if ((boxLeftPos < curX) && (boxRightPos > curX) && (boxTopPos < curY) && (boxButtonPos > curY)) {
                    if (!mHasPass[nLoop]) {
                        mCurPassCount++;
                        mHasPass[nLoop] = true;
                        mBoxes[nLoop].setPassColor();
                        if (mCurPassCount == mTotalPassCount) {
                            Toast.makeText(this, R.string.text_pass, Toast.LENGTH_SHORT).show();
                            storeRusult(true);
                            this.finish();
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    class Box extends View {
        private Paint mPaint;
        private int mLeft;
        private int mRight;
        private int mTop;
        private int mButtom;
        private boolean mIsPass;

        public Box(Context context, int left, int top, int right, int buttom) {
            super(context);
            // TODO Auto-generated constructor stub
            mPaint = new Paint();
            mLeft = left;
            mRight = right;
            mTop = top;
            mButtom = buttom;
            mIsPass = false;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);

            if (mIsPass) {
                mPaint.setColor(Color.GREEN);
            } else {
                mPaint.setColor(Color.RED);
            }
            //	mIsPass = false;
            canvas.drawRect(mLeft, mTop, mRight, mButtom, mPaint);
        }

        public void setPassColor() {
            mIsPass = true;
            invalidate();
        }

        public int getLeftPos() {
            return mLeft;
        }

        public int getTopPos() {
            return mTop;
        }
    }
}
