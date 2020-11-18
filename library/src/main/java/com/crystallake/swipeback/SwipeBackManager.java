/**
 * Created by : yds
 * Time: 2020-11-15 9:21 PM
 */
package com.crystallake.swipeback;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Stack;

public class SwipeBackManager implements Application.ActivityLifecycleCallbacks {

    private static SwipeBackManager INSTANCE = null;
    private final Stack<Activity> mActivityStack = new Stack<>();

    private SwipeBackManager(Application application){
        application.registerActivityLifecycleCallbacks(this);
    }

    public static SwipeBackManager getInstance(){
        if (INSTANCE == null) {
            throw new RuntimeException("需要先在Application中调用SwipeBack.init()方法完成初始化");
        }
        return INSTANCE;
    }

    public static void init(Application application){
        if (INSTANCE==null){
            INSTANCE = new SwipeBackManager(application);
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        mActivityStack.push(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        mActivityStack.remove(activity);
    }

    public Stack<Activity> getActivityStack() {
        return mActivityStack;
    }


    /**
     * 获取倒数第二个Activity
     */
    @Nullable
    public Activity getPreviousActivity(){
        return mActivityStack.size()>=2?mActivityStack.get(mActivityStack.size()-2):null;
    }

    /**
     * 获取倒数第二个 Activity
     */
    @Nullable
    public Activity getPreviousActivity(Activity currentActivity) {
        Activity activity = null;
        try {
            if (mActivityStack.size() > 1) {
                activity = mActivityStack.get(mActivityStack.size() - 2);

                if (currentActivity.equals(activity)) {
                    int index = mActivityStack.indexOf(currentActivity);
                    if (index > 0) {
                        // 处理内存泄漏或最后一个 Activity 正在 finishing 的情况
                        activity = mActivityStack.get(index - 1);
                    } else if (mActivityStack.size() == 2) {
                        // 处理屏幕旋转后 mActivityStack 中顺序错乱
                        activity = mActivityStack.lastElement();
                    }
                }
            }
        } catch (Exception e) {
        }
        return activity;
    }
}
