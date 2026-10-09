package org.soft2u.miband_5_display.utils;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class OnSwipeTouchListener implements View.OnTouchListener {
    private final String TAG = "OnSwipeTouchListener";
    private final GestureDetector gestureDetector;

    public OnSwipeTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void onSwipeLeft() {
    }

    public void onSwipeRight() {
    }

    public void onSwipeUp() {
    }

    public void onSwipeDown() {
    }

    public void onTap() {
    }

    public void onHold() {
    }

    public void onScroll2(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    }

    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
//            Log.d(TAG,"onDown: ");
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
//            Log.i(TAG, "onSingleTapConfirmed: ");
            onTap();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            onHold();
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
//            Log.i(TAG, "onDoubleTap: ");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.i(TAG, "onScroll: " + distanceY);
            onScroll2(e1, e2, distanceX, distanceY);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
//                if (distanceX > 0)
//                    onSwipeRight();
//                else
//                    onSwipeLeft();
                return true;
            } else if (Math.abs(distanceY) > Math.abs(distanceX) && Math.abs(distanceY) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if(distanceY < 0)
                    onSwipeUp();
                else
                    onSwipeDown();
            }
            return false;
        }
    }
}
