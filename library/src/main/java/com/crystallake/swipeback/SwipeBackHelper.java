/**
 * Created by : yds
 * Time: 2020-11-16 10:06 PM
 */
package com.crystallake.swipeback;

import android.app.Activity;

public class SwipeBackHelper {
    private Activity mActivity;
    private SwipeBackLayout mSwipeBackLayout;

    private SwipeBackHelper(Activity activity){
        mActivity = activity;
        mSwipeBackLayout = new SwipeBackLayout(activity);
    }

    public static SwipeBackHelper inject(Activity activity){
        return new SwipeBackHelper(activity);
    }

    public void onPostCreate(){
        mSwipeBackLayout.attachTo(mActivity);
    }

    public void onDestroy(){
        mActivity = null;
        mSwipeBackLayout = null;
    }

    public void onEnterAnimationComplete(){
        if (!mSwipeBackLayout.isTakeOverActivityEnterExitAnim()) {
            if (!mSwipeBackLayout.isActivitySwiping()) {
                mSwipeBackLayout.setActivityTranslucent(false);
            }
        }
    }

    public boolean finish(){
        if (mSwipeBackLayout.isTakeOverActivityEnterExitAnim()) {
            if (!mSwipeBackLayout.isTakeOverActivityExitAnimRunning()) {
                mSwipeBackLayout.startExitAnim();
                return false;
            }
            return true;
        } else {
            return !mSwipeBackLayout.isActivitySwiping();
        }
    }

    public SwipeBackLayout getSwipeBackLayout() {
        return mSwipeBackLayout;
    }

    public void setActivityIsAlreadyTranslucent(boolean activityIsAlreadyTranslucent) {
        mSwipeBackLayout.setActivityIsAlreadyTranslucent(activityIsAlreadyTranslucent);
    }

    public boolean isActivityIsAlreadyTranslucent() {
        return mSwipeBackLayout.isActivityIsAlreadyTranslucent();
    }

    public void setTakeOverActivityEnterExitAnim(boolean enable) {
        mSwipeBackLayout.setTakeOverActivityEnterExitAnim(enable);
    }

    public boolean isTakeOverActivityEnterExitAnim() {
        return mSwipeBackLayout.isTakeOverActivityEnterExitAnim();
    }

    public void setSwipeBackEnable(boolean enable) {
        mSwipeBackLayout.setSwipeBackEnable(enable);
    }

    public boolean isSwipeBackEnable() {
        return mSwipeBackLayout.isSwipeBackEnable();
    }

    public void setSwipeBackOnlyEdge(boolean enable) {
        mSwipeBackLayout.setSwipeBackOnlyEdge(enable);
    }

    public boolean isSwipeBackOnlyEdge() {
        return mSwipeBackLayout.isSwipeBackOnlyEdge();
    }


    public void setSwipeBackForceEdge(boolean enable) {
        mSwipeBackLayout.setSwipeBackForceEdge(enable);
    }

    public boolean isSwipeBackForceEdge() {
        return mSwipeBackLayout.isSwipeBackForceEdge();
    }

    public void setSwipeBackDirection(@SwipeBackLayout.DragEdge int direction) {
        mSwipeBackLayout.setSwipeBackDirection(direction);
    }

    @SwipeBackLayout.DragEdge
    public int getSwipeBackDirection() {
        return mSwipeBackLayout.getSwipeBackDirection();
    }


}
