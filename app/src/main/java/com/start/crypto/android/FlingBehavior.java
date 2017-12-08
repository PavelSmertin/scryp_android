package com.start.crypto.android;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public final class FlingBehavior extends AppBarLayout.Behavior {
    private static final int TOP_CHILD_FLING_THRESHOLD = 0;
    private boolean isPositive;
    private int mVerticalOffset = 0;

    public FlingBehavior() {
    }

    public FlingBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, AppBarLayout child, View dependency) {

        child.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            mVerticalOffset = verticalOffset;
            Log.e("Vertical offset", verticalOffset + "");
        });

        return super.layoutDependsOn(parent, child, dependency);
    }

    @Override
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY, boolean consumed) {
//        if (velocityY > 0 && !isPositive || velocityY < 0 && isPositive) {
//            velocityY = velocityY * -1;
//        }
//
//        if (target instanceof SwipeRefreshLayout && velocityY != 0) {
//            target = ((SwipeRefreshLayout) target).getChildAt(0);
//        }
//
//        if (target instanceof RecyclerView && velocityY != 0) {
//            final RecyclerView recyclerView = (RecyclerView) target;
//            final View firstChild = recyclerView.getChildAt(0);
//            final int childAdapterPosition = recyclerView.getChildAdapterPosition(firstChild);
//            consumed = childAdapterPosition > TOP_CHILD_FLING_THRESHOLD;
//
//        }

        Log.e("onNestedFling","Consumed:" + coordinatorLayout + " " + child + " " + " " + target);

        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    }

//    @Override
//    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY) {
//        super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
//
//        if (target instanceof SwipeRefreshLayout) {
//            target = ((SwipeRefreshLayout) target).getChildAt(0);
//        }
//
//        if(target instanceof RecyclerView) {
//
//            if(isPositive && mVerticalOffset > -child.getTotalScrollRange()) { // UP
//
//                onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, true);
//                return true;
//            }
//            else if(!isPositive) { // DOWN
//
//            }
//        }
//
//        return false;
//    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
        isPositive = dy > 0;
    }
}