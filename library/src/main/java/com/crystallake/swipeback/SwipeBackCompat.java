/**
 * Created by : yds
 * Time: 2020-11-15 9:43 PM
 */
package com.crystallake.swipeback;

import android.app.Activity;
import android.app.ActivityOptions;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import androidx.core.view.ScrollingView;
import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SwipeBackCompat {




    public static List<View> findAllScrollViews(ViewGroup viewGroup){
        List<View> views = new ArrayList<>();
        for(int i=0;i<viewGroup.getChildCount();i++){
            View view = viewGroup.getChildAt(i);
            if(view.getVisibility()!=View.VISIBLE){
                continue;
            }
            if (isScrollableView(view)) {
                views.add(view);
            }
            if (view instanceof ViewGroup){
                views.addAll(findAllScrollViews((ViewGroup) view));
            }
        }
        return views;
    }

    public static boolean isScrollableView(View view) {
        return view instanceof ScrollView
                || view instanceof HorizontalScrollView
                || view instanceof AbsListView
                || view instanceof ViewPager
                || view instanceof WebView
                || view instanceof ScrollingView;
    }
    /**
     * 视图是否能继续左滑
     *
     * @param views
     * @param x
     * @param y
     * @param defaultValueForNull
     * @return
     */
    public static boolean canViewScrollLeft(List<View> views, float x, float y, boolean defaultValueForNull) {
        if (views == null) {
            return defaultValueForNull;
        }
        List<View> contains = contains(views, x, y);
        if (contains == null) {
            return defaultValueForNull;
        }
        boolean canViewScroll = false;
        for (int i = contains.size() - 1; i >= 0; i--) {
            canViewScroll = ScrollCompat.canScrollHorizontally(contains.get(i),-1);
            if (canViewScroll){
                break;
            }
        }
        return canViewScroll;
    }

    public static boolean canViewScrollRight(List<View> views,float x,float y,boolean defaultValueForNull){
        if (views == null) {
            return defaultValueForNull;
        }
        List<View> contains = contains(views,x,y);
        if (contains == null) {
            return defaultValueForNull;
        }
        boolean canViewScroll = false;
        for (int i=contains.size()-1;i>=0;i--){
            canViewScroll = ScrollCompat.canScrollHorizontally(contains.get(i),1);
            if (canViewScroll) {
                break;
            }
        }
        return canViewScroll;
    }

    public static boolean canViewScrollUp(List<View> views, float x, float y, boolean defaultValueForNull) {
        if (views == null) {
            return defaultValueForNull;
        }
        List<View> contains = contains(views,x,y);
        if (contains == null) {
            return defaultValueForNull;
        }
        boolean canViewScroll = false;
        for (int i = contains.size()-1; i >=0; i--) {
            canViewScroll = ScrollCompat.canScrollVertically(contains.get(i),-1);
            if (canViewScroll) {
                break;
            }
        }
        return canViewScroll;
    }

    public static boolean canViewScrollDown(List<View> views, float x, float y, boolean defaultValueForNull) {
        if (views == null) {
            return defaultValueForNull;
        }
        List<View> contains = contains(views,x,y);
        if (contains == null) {
            return defaultValueForNull;
        }
        boolean canViewScroll = false;
        for(int i=contains.size()-1;i>=0;i--){
            canViewScroll = ScrollCompat.canScrollVertically(contains.get(i),1);
            if (canViewScroll) {
                break;
            }
        }
        return canViewScroll;

    }

    public static List<View> contains(List<View> views, float x, float y) {
        if (views == null) {
            return null;
        }
        List<View> contains = new ArrayList<>(views.size());
        for (int i = views.size() - 1; i >= 0; i--) {
            View v = views.get(i);
            Rect localRect = new Rect();
            int[] l = new int[2];
            //计算屏幕上此视图的坐标
            v.getLocationOnScreen(l);
            //视图矩形大小
            localRect.set(l[0], l[1], l[0] + v.getWidth(), l[1] + v.getHeight());
            if (localRect.contains((int) x, (int) y)) {
                //如果x，y在矩形内，则加入到列表中
                contains.add(v);
            }
        }
        return contains;
    }

    /**
     * 将activity转化为透明的
     *
     * @param activity 要转化的activity
     */
    public static void convertActivityToTranslucent(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            convertActivityToTranslucentAfterL(activity);
        } else {
            convertActivityToTranslucentBeforeL(activity);
        }
    }

    /**
     * Convert a translucent themed Activity
     */
    public static void convertActivityFromTranslucent(Activity activity) {
        try {
            Method method = Activity.class.getDeclaredMethod("convertFromTranslucent");
            method.setAccessible(true);
            method.invoke(activity);
        } catch (Throwable t) {
        }
    }

    /**
     * Calling the convertToTranslucent method on platforms before Android 5.0
     */
    private static void convertActivityToTranslucentBeforeL(Activity activity) {
        try {
            Class<?>[] classes = Activity.class.getDeclaredClasses();
            Class<?> translucentConversionListenerClazz = null;
            for (Class clazz : classes) {
                if (clazz.getSimpleName().contains("TranslucentConversionListener")) {
                    translucentConversionListenerClazz = clazz;
                }
            }
            Method method = Activity.class.getDeclaredMethod("convertToTranslucent", translucentConversionListenerClazz);
            method.setAccessible(true);
            method.invoke(activity, new Object[]{null});
        } catch (Throwable t) {
        }
    }

    /**
     * Calling the convertToTranslucent method on platforms after Android 5.0
     */
    private static void convertActivityToTranslucentAfterL(Activity activity) {
        try {
            Method getActivityOptions = Activity.class.getDeclaredMethod("getActivityOptions");
            getActivityOptions.setAccessible(true);
            Object options = getActivityOptions.invoke(activity);

            Class<?>[] classes = Activity.class.getDeclaredClasses();
            Class<?> translucentConversionListenerClazz = null;
            for (Class clazz : classes) {
                if (clazz.getSimpleName().contains("TranslucentConversionListener")) {
                    translucentConversionListenerClazz = clazz;
                }
            }
            Method convertToTranslucent = Activity.class.getDeclaredMethod("convertToTranslucent", translucentConversionListenerClazz, ActivityOptions.class);
            convertToTranslucent.setAccessible(true);
            convertToTranslucent.invoke(activity, null, options);
        } catch (Throwable t) {
        }
    }

}
