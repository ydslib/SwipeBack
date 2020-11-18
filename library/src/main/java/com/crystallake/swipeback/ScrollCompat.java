/**
 * Created by : yds
 * Time: 2020-11-15 10:42 PM
 */
package com.crystallake.swipeback;

import android.view.View;

import androidx.core.view.ScrollingView;

public class ScrollCompat {
    public static boolean canScrollHorizontally(View v, int direction) {
        if (v instanceof ScrollingView) {
            return canScrollingViewScrollHorizontally((ScrollingView) v, direction);
        } else {
            return v.canScrollHorizontally(direction);
        }
    }

    public static boolean canScrollVertically(View v, int direction) {
        if (v instanceof ScrollingView) {
            return canScrollingViewScrollVertically((ScrollingView) v,direction);
        }else{
            return v.canScrollVertically(direction);
        }
    }

    private static boolean canScrollingViewScrollHorizontally(ScrollingView view, int direction) {
        //手势位移方向，与滚动条相反，与direction相反
        int offset = view.computeHorizontalScrollOffset();
        int range = view.computeHorizontalScrollRange() - view.computeHorizontalScrollExtent();
        if (range == 0) {
            return false;
        }
        if (direction < 0) {
            return offset > 0;
        } else {
            return offset < range - 1;
        }
    }

    private static boolean canScrollingViewScrollVertically(ScrollingView view, int direction) {
        int offset = view.computeVerticalScrollOffset();
        int range = view.computeVerticalScrollRange() - view.computeVerticalScrollExtent();
        if (range == 0) return false;
        if (direction < 0) {
            return offset > 0;
        }else{
            return offset < range -1;
        }
    }

}
