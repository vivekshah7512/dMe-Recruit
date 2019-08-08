package com.decideme.recruit.attributes;

/**
 * Created by vivek_shah on 23/10/17.
 */
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.List;

public class MapAnimator {
    static final int GREY = Color.parseColor("#FFA7A6A6");
    private static MapAnimator mapAnimator;
    private Polyline backgroundPolyline;
    private AnimatorSet firstRunAnimSet;
    private Polyline foregroundPolyline;
    private PolylineOptions optionsForeground;
    private AnimatorSet secondLoopRunAnimSet;

    class C05031 implements AnimatorUpdateListener {
        C05031() {
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            List<LatLng> foregroundPoints = MapAnimator.this.backgroundPolyline.getPoints();
            foregroundPoints.subList(0, (int) (((float) foregroundPoints.size()) * (((float) ((Integer) animation.getAnimatedValue()).intValue()) / 100.0f))).clear();
            MapAnimator.this.foregroundPolyline.setPoints(foregroundPoints);
        }
    }

    class C05042 implements AnimatorListener {
        C05042() {
        }

        public void onAnimationStart(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            MapAnimator.this.foregroundPolyline.setColor(MapAnimator.GREY);
            MapAnimator.this.foregroundPolyline.setPoints(MapAnimator.this.backgroundPolyline.getPoints());
        }

        public void onAnimationCancel(Animator animation) {
        }

        public void onAnimationRepeat(Animator animation) {
        }
    }

    class C05053 implements AnimatorUpdateListener {
        C05053() {
        }

        public void onAnimationUpdate(ValueAnimator animator) {
            MapAnimator.this.foregroundPolyline.setColor(((Integer) animator.getAnimatedValue()).intValue());
        }
    }

    private MapAnimator() {
    }

    public static MapAnimator getInstance() {
        if (mapAnimator == null) {
            mapAnimator = new MapAnimator();
        }
        return mapAnimator;
    }

    public void animateRoute(GoogleMap googleMap, List<LatLng> bangaloreRoute) {
        if (this.firstRunAnimSet == null) {
            this.firstRunAnimSet = new AnimatorSet();
        } else {
            this.firstRunAnimSet.removeAllListeners();
            this.firstRunAnimSet.end();
            this.firstRunAnimSet.cancel();
            this.firstRunAnimSet = new AnimatorSet();
        }
        if (this.secondLoopRunAnimSet == null) {
            this.secondLoopRunAnimSet = new AnimatorSet();
        } else {
            this.secondLoopRunAnimSet.removeAllListeners();
            this.secondLoopRunAnimSet.end();
            this.secondLoopRunAnimSet.cancel();
            this.secondLoopRunAnimSet = new AnimatorSet();
        }
        if (this.foregroundPolyline != null) {
            this.foregroundPolyline.remove();
        }
        if (this.backgroundPolyline != null) {
            this.backgroundPolyline.remove();
        }
        this.backgroundPolyline = googleMap.addPolyline(new PolylineOptions().add((LatLng) bangaloreRoute.get(0)).color(GREY).width(5.0f));
        this.optionsForeground = new PolylineOptions().add((LatLng) bangaloreRoute.get(0)).color(ViewCompat.MEASURED_STATE_MASK).width(5.0f);
        this.foregroundPolyline = googleMap.addPolyline(this.optionsForeground);
        ValueAnimator percentageCompletion = ValueAnimator.ofInt(new int[]{0, 100});
        percentageCompletion.setDuration(2000);
        percentageCompletion.setInterpolator(new DecelerateInterpolator());
        percentageCompletion.addUpdateListener(new C05031());
        percentageCompletion.addListener(new C05042());
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), new Object[]{Integer.valueOf(GREY), Integer.valueOf(ViewCompat.MEASURED_STATE_MASK)});
        colorAnimation.setInterpolator(new AccelerateInterpolator());
        colorAnimation.setDuration(1200);
        colorAnimation.addUpdateListener(new C05053());
    }

    public void setRouteIncreaseForward(LatLng endLatLng) {
        List<LatLng> foregroundPoints = this.foregroundPolyline.getPoints();
        foregroundPoints.add(endLatLng);
        this.foregroundPolyline.setPoints(foregroundPoints);
    }
}


