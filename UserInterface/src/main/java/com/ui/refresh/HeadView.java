package com.ui.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by wangjiangpeng01 on 2017/3/9.
 */
public class HeadView extends LinearLayout {

    public static final int BEING_DRAGGED = 0;
    public static final int UP_REFRESH = 1;
    public static final int REFRESHING = 2;
    public static final int REFRESH_SUCCESS = 3;
    public static final int REFRESH_FAIL = 4;

    private TextView text;

    public HeadView(Context context) {
        this(context, null);
    }

    public HeadView(Context context, AttributeSet attrs) {
        super(context, attrs);

        text = new TextView(context);
        addView(text);
        setGravity(Gravity.CENTER);
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


}
