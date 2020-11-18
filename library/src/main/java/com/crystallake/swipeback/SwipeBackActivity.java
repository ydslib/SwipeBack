/**
 * Created by : yds
 * Time: 2020-11-16 10:09 PM
 */
package com.crystallake.swipeback;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SwipeBackActivity extends AppCompatActivity {

    private SwipeBackHelper mSwipeBackHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwipeBackHelper = SwipeBackHelper.inject(this);
        mSwipeBackHelper.setSwipeBackEnable(swipeBackEnable());
        mSwipeBackHelper.setSwipeBackOnlyEdge(swipeBackOnlyEdge());
        mSwipeBackHelper.setSwipeBackForceEdge(swipeBackForceEdge());
        mSwipeBackHelper.setSwipeBackDirection(swipeBackDirection());
        mSwipeBackHelper.getSwipeBackLayout().setShadowStartColor(0);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mSwipeBackHelper.onPostCreate();
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        mSwipeBackHelper.onEnterAnimationComplete();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSwipeBackHelper.onDestroy();
    }

    @Override
    public void finish() {
        if (mSwipeBackHelper.finish()) {
            super.finish();
        }
    }


    protected boolean swipeBackEnable(){
        return true;
    }

    protected boolean swipeBackOnlyEdge() {
        return false;
    }

    protected boolean swipeBackForceEdge() {
        return true;
    }

    @SwipeBackLayout.DragEdge
    protected int swipeBackDirection() {
        return SwipeBackLayout.DragEdge.FROM_LEFT;
    }
}
