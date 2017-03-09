package com.ui.refresh;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by wangjiangpeng01 on 2017/3/8.
 */
public class HeadRefreshView extends LinearLayout{

    private CircleImageView mCircleView;
    private Animation.AnimationListener mListener;
    private TextView text;

    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;
    private static final int CIRCLE_DIAMETER = 40;

    private int mCircleWidth;
    private int mCircleHeight;

    public static final int BEING_DRAGGED = 0;
    public static final int UP_REFRESH = 1;
    public static final int REFRESHING = 2;
    public static final int REFRESH_SUCCESS = 3;
    public static final int REFRESH_FAIL = 4;

    public HeadRefreshView(Context context, MaterialProgressDrawable mProgress) {
        super(context);

        setOrientation(HORIZONTAL);

        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mCircleWidth = (int) (CIRCLE_DIAMETER * metrics.density);
        mCircleHeight = (int) (CIRCLE_DIAMETER * metrics.density);
        createProgressView(mProgress);

        setGravity(Gravity.CENTER);
    }

    private void createProgressView(MaterialProgressDrawable mProgress) {
        mCircleView = new CircleImageView(getContext(), CIRCLE_BG_LIGHT, CIRCLE_DIAMETER / 2);
        mCircleView.setImageDrawable(mProgress);
        addView(mCircleView);
        text = new TextView(getContext());
        text.setText("下拉刷新");
        text.setTextColor(Color.BLACK);
        addView(text);
    }

    public void setState(int state){
        switch(state){
            case BEING_DRAGGED:
                text.setText("下拉刷新");
                break;

            case UP_REFRESH:
                text.setText("释放立即刷新");
                break;

            case REFRESHING:
                text.setText("正在刷新...");
                break;

            case REFRESH_SUCCESS:
                text.setText("刷新成功");
                break;

            case REFRESH_FAIL:
                text.setText("刷新失败");
                break;
        }
    }

    public void setImageDrawable(Drawable drawable){
        mCircleView.setImageDrawable(drawable);
    }

    public void setAnimationListener(Animation.AnimationListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mCircleView.measure(MeasureSpec.makeMeasureSpec(mCircleWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mCircleHeight, MeasureSpec.EXACTLY));
    }

    @Override
    public void onAnimationStart() {
        super.onAnimationStart();
        if (mListener != null) {
            mListener.onAnimationStart(getAnimation());
        }
    }

    @Override
    public void onAnimationEnd() {
        super.onAnimationEnd();
        if (mListener != null) {
            mListener.onAnimationEnd(getAnimation());
        }
    }


}
