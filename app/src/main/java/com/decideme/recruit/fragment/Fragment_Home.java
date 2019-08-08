package com.decideme.recruit.fragment;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decideme.recruit.GPS.GPS_Fused;
import com.decideme.recruit.R;
import com.decideme.recruit.activities.ChatActivity;
import com.decideme.recruit.activities.LoginActivity;
import com.decideme.recruit.adapter.GiudeListAdapter;
import com.decideme.recruit.attributes.Constant;
import com.decideme.recruit.attributes.DME_ProgressDilog;
import com.decideme.recruit.attributes.DataParser;
import com.decideme.recruit.attributes.GPSTracker1;
import com.decideme.recruit.attributes.MyApplication;
import com.decideme.recruit.attributes.Utility;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by vivek.shah on 12-Jan-16.
 */
public class Fragment_Home extends Fragment implements GPS_Fused.IGPSActivity,
        OnMapReadyCallback, View.OnClickListener {

    public static boolean active = false;
    private static LatLng POINT_A;
    private static LatLng POINT_B;
    View mView;
    MapView mapView;
    NotificationManager notificationManager;
    private ArrayList<LatLng> MarkerPoints;
    private LatLng POINT_USER, POINT_WORKER;
    private Thread backgroundData;
    private Button btn_accept, btn_interviewed, btn_pause_service, btn_reject, btn_start;
    private Chronometer cm_timer;
    private count count;
    private Dialog dialog;
    private EditText et_comments;
    private GoogleMap googleMap;
    private GPS_Fused gps;
    private GPSTracker1 gpsTracker;
    private ImageView img_ar_profile, img_cancel_req, img_pause_service, img_profile_interviewed,
            img_profile_start;
    private boolean isRunning = false;
    private double latitude = 0.0, longitude = 0.0;
    private LinearLayout ll_accept_reject, ll_interviewed, ll_start_service, ll_time_consumed, ll_timer;
    private Bundle mBundle;
    private Marker mk = null;
    private String notification_id = "";
    private boolean pause_service_flag = false;
    private Polyline polylinePath;
    private RatingBar ratingBar, ratingBar1, ratingBar_inter, ratingBar_start;
    private String request_type = "";
    private RelativeLayout rl_ar_time, rl_inter_call, rl_inter_chat, rl_my_location,
            rl_start_call, rl_start_chat;
    private String[] rules_image, rules_name;
    private String service_id = "";
    private Chronometer simpleChronometer;
    private ToggleButton tbutton;
    private long timeWhenStopped = 0;
    private TextView tv_ar_name, tv_ar_time, tv_inter_job_location, tv_inter_job_type,
            tv_inter_name, tv_inter_total_review, tv_job_type, tv_location, tv_rating,
            tv_start_job_type, tv_start_name, tv_start_total_review, tv_time;
    private String type = "", type_on_off = "on", us_id = "", user_id = "", user_latitude,
            user_longitude, user_mobile = "", user_pic = "";
    private View view, view_ar_time;
    // Notification Broadcast
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Utility.getAppPrefString(getActivity(), "activity_from").equalsIgnoreCase("cancel")) {
                Toast.makeText(getActivity(), "Job Cancel", Toast.LENGTH_SHORT).show();
                Utility.writeSharedPreferences(getActivity(), "data_flag", "false");
                if (googleMap != null) {
                    polylinePath.remove();
                    googleMap.clear();
                    LatLng gps = new LatLng(latitude, longitude);
                    googleMap.addMarker(new MarkerOptions()
                            .position(gps)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.blue_pin)));
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(gps).zoom(14.0f).build()));
                }
                ll_timer.setVisibility(View.GONE);
                ll_interviewed.setVisibility(View.GONE);
                ll_accept_reject.setVisibility(View.GONE);
                img_cancel_req.setVisibility(View.GONE);
                ll_start_service.setVisibility(View.GONE);
                tbutton.setVisibility(View.VISIBLE);
                ll_time_consumed.setVisibility(View.GONE);
                btn_pause_service.setVisibility(View.GONE);

                notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
            } else if (Utility.getAppPrefString(getActivity(), "activity_from").equalsIgnoreCase("cancel_request")) {
                if (googleMap != null) {
                    polylinePath.remove();
                    googleMap.clear();
                    if (gpsTracker.canGetLocation()) {
                        LatLng gps = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
                        googleMap.addMarker(new MarkerOptions()
                                .position(gps)
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.blue_pin)));

                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(gps).zoom(14.0f).build()));
                    }
                }
                count.cancel();
                ll_timer.setVisibility(View.GONE);

                ll_interviewed.setVisibility(View.GONE);
                ll_accept_reject.setVisibility(View.GONE);
                img_cancel_req.setVisibility(View.GONE);
                ll_start_service.setVisibility(View.GONE);
                tbutton.setVisibility(View.VISIBLE);
                ll_time_consumed.setVisibility(View.GONE);
                btn_pause_service.setVisibility(View.GONE);

                notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
            }
        }
    };

    public static void animateMarker(Location destination, Marker marker) {
        if (marker != null) {
            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = new LatLng(destination.getLatitude(), destination.getLongitude());
            final float startRotation = marker.getRotation();
            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            valueAnimator.setDuration(1000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            final Marker marker2 = marker;
            final Location location = destination;
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        marker2.setPosition(latLngInterpolator.interpolate(v, startPosition, endPosition));
                        marker2.setRotation(Fragment_Home.computeRotation(v, startRotation, location.getBearing()));
                    } catch (Exception e) {
                    }
                }
            });
            valueAnimator.start();
        }
    }

    private static float computeRotation(float fraction, float start, float end) {
        float rotation;
        float normalizedEndAbs = ((end - start) + 360.0f) % 360.0f;
        if ((normalizedEndAbs > 180.0f ? -1.0f : 1.0f) > 0.0f) {
            rotation = normalizedEndAbs;
        } else {
            rotation = normalizedEndAbs - 360.0f;
        }
        return (((fraction * rotation) + start) + 360.0f) % 360.0f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        view = inflater.inflate(R.layout.content_home,
                container, false);

        mapView = (MapView) view.findViewById(R.id.map1);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        gps = new GPS_Fused(Fragment_Home.this, getActivity());

        init();

        // Before One Hour Schedule
        if (Utility.getAppPrefString(getActivity(), "accept_dialog_flag").equalsIgnoreCase("1")) {
            tbutton.setVisibility(View.GONE);
            ll_accept_reject.setVisibility(View.VISIBLE);
            ll_start_service.setVisibility(View.GONE);
            ll_interviewed.setVisibility(View.GONE);
            img_cancel_req.setVisibility(View.GONE);
            ll_time_consumed.setVisibility(View.GONE);
            btn_pause_service.setVisibility(View.GONE);

            try {
                tv_ar_name.setText(Utility.getAppPrefString(getActivity(), "noti_user_name"));
                tv_rating.setText(Utility.getAppPrefString(getActivity(), "noti_total_review"));
                tv_job_type.setText(Utility.getAppPrefString(getActivity(), "cat_name"));
                notification_id = Utility.getAppPrefString(getActivity(), "noti_notification_id");
                service_id = Utility.getAppPrefString(getActivity(), "noti_service_id");
                user_id = Utility.getAppPrefString(getActivity(), "noti_user_id");
                request_type = Utility.getAppPrefString(getActivity(), "noti_type");
                user_pic = Utility.getAppPrefString(getActivity(), "noti_user_pic");
                user_mobile = Utility.getAppPrefString(getActivity(), "noti_user_mobile");

                if (request_type.equalsIgnoreCase("schedule")) {
                    view_ar_time.setVisibility(View.VISIBLE);
                    rl_ar_time.setVisibility(View.VISIBLE);
                    tv_ar_time.setText(Utility.getAppPrefString(getActivity(), "noti_date") + " at " + Utility.getAppPrefString(getActivity(), "noti_time"));
                } else if (request_type.equalsIgnoreCase("schedule1")) {
                    view_ar_time.setVisibility(View.VISIBLE);
                    rl_ar_time.setVisibility(View.VISIBLE);
                    tv_ar_time.setText(Utility.getAppPrefString(getActivity(), "noti_date_time"));
                } else {
                    view_ar_time.setVisibility(View.GONE);
                    rl_ar_time.setVisibility(View.GONE);
                }
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(getActivity(), Locale.getDefault());
                addresses = geocoder.getFromLocation(Double.parseDouble(Utility.getAppPrefString(getActivity(), "noti_user_latitude")), Double.parseDouble(Utility.getAppPrefString(getActivity(), "noti_user_longitude")), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                tv_location.setText(address);
                ratingBar.setRating(Float.parseFloat(Utility.getAppPrefString(getActivity(), "noti_user_rating")));
                Glide.with(getActivity()).load(Utility.getAppPrefString(getActivity(), "noti_user_pic"))
                        .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)
                                .error(R.mipmap.user))
                        .thumbnail(0.5f)
                        .into(img_ar_profile);

                POINT_A = new LatLng(Double.parseDouble(Utility.getAppPrefString(getActivity(), "latitude")),
                        Double.parseDouble(Utility.getAppPrefString(getActivity(), "longitude")));
                POINT_B = new LatLng(Double.parseDouble(Utility.getAppPrefString(getActivity(), "noti_user_latitude")),
                        Double.parseDouble(Utility.getAppPrefString(getActivity(), "noti_user_longitude")));
                MarkerPoints = new ArrayList();
                MarkerPoints.add(POINT_A);
                MarkerPoints.add(POINT_B);

                if (googleMap != null) {
                    try {
                        if (MarkerPoints.size() >= 2) {
                            POINT_WORKER = (LatLng) MarkerPoints.get(0);
                            POINT_USER = (LatLng) MarkerPoints.get(1);
                            String url = getUrl(POINT_WORKER, POINT_USER);
                            new FetchUrl().execute(new String[]{url});
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            ll_timer.setVisibility(View.VISIBLE);

            if (!isRunning) {
                count = new count(61000, 1000);
                count.start();
            }
        } else if (Utility.getAppPrefString(getActivity(), "back").equalsIgnoreCase("1")) {
            Utility.writeSharedPreferences(getActivity(), "back", "0");
            if (Utility.isNetworkAvaliable(getActivity())) {
                try {
                    getCurrentData getTask = new getCurrentData(getActivity());
                    getTask.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (Utility.getAppPrefString(getActivity(), "data_flag").equalsIgnoreCase("true")) {

            notification_id = Utility.getAppPrefString(getActivity(), "saved_notification_id");
            service_id = Utility.getAppPrefString(getActivity(), "saved_service_id");
            user_id = Utility.getAppPrefString(getActivity(), "saved_user_id");
            request_type = Utility.getAppPrefString(getActivity(), "saved_request_type");
            user_latitude = Utility.getAppPrefString(getActivity(), "saved_user_latitude");
            user_longitude = Utility.getAppPrefString(getActivity(), "saved_user_longitude");
            user_pic = Utility.getAppPrefString(getActivity(), "saved_user_image");
            user_mobile = Utility.getAppPrefString(getActivity(), "saved_mobile");

            if (Utility.getAppPrefString(getActivity(), "saved_current_flag").equalsIgnoreCase("accept")) {
                ll_interviewed.setVisibility(View.VISIBLE);
                img_cancel_req.setVisibility(View.VISIBLE);
                ll_start_service.setVisibility(View.GONE);
                ll_accept_reject.setVisibility(View.GONE);
                ll_time_consumed.setVisibility(View.GONE);
                btn_pause_service.setVisibility(View.GONE);
                tbutton.setVisibility(View.GONE);

                tv_inter_name.setText(Utility.getAppPrefString(getActivity(), "saved_user_name"));
                tv_inter_total_review.setText(Utility.getAppPrefString(getActivity(), "saved_total_review"));
                tv_inter_job_type.setText(Utility.getAppPrefString(getActivity(), "cat_name"));
                ratingBar_inter.setRating(Float.parseFloat(Utility.getAppPrefString(getActivity(), "saved_rating")));
                if (!Utility.getAppPrefString(getActivity(), "saved_user_image").equalsIgnoreCase("")) {
                    Glide.with(getActivity()).load(Utility.getAppPrefString(getActivity(), "saved_user_image"))
                            .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)
                                    .error(R.mipmap.user))
                            .thumbnail(0.5f)
                            .into(img_profile_interviewed);
                }
                try {
                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(getActivity(), Locale.getDefault());
                    addresses = geocoder.getFromLocation(Double.parseDouble(user_latitude), Double.parseDouble(user_longitude), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    tv_inter_job_location.setText(address);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!gps.isRunning()) gps.resumeGPS();
            } else if (Utility.getAppPrefString(getActivity(), "saved_current_flag").equalsIgnoreCase("interview")) {
                ll_start_service.setVisibility(View.VISIBLE);
                ll_interviewed.setVisibility(View.GONE);
                img_cancel_req.setVisibility(View.GONE);
                ll_time_consumed.setVisibility(View.GONE);
                btn_pause_service.setVisibility(View.GONE);
                tbutton.setVisibility(View.GONE);
                tv_start_name.setText(Utility.getAppPrefString(getActivity(), "saved_user_name"));
                tv_start_total_review.setText(Utility.getAppPrefString(getActivity(), "saved_total_review"));
                tv_start_job_type.setText(Utility.getAppPrefString(getActivity(), "cat_name"));
                ratingBar_start.setRating(Float.parseFloat(Utility.getAppPrefString(getActivity(), "saved_rating")));
                if (!Utility.getAppPrefString(getActivity(), "saved_user_image").equalsIgnoreCase("")) {
                    Glide.with(getActivity()).load(Utility.getAppPrefString(getActivity(), "saved_user_image"))
                            .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)
                                    .error(R.mipmap.user))
                            .thumbnail(0.5f)
                            .into(img_profile_start);
                }
            } else if (Utility.getAppPrefString(getActivity(), "saved_current_flag").equalsIgnoreCase("start") ||
                    Utility.getAppPrefString(getActivity(), "saved_current_flag").equalsIgnoreCase("pause")) {
                ll_start_service.setVisibility(View.VISIBLE);
                ll_interviewed.setVisibility(View.GONE);
                img_cancel_req.setVisibility(View.GONE);
                ll_time_consumed.setVisibility(View.GONE);
                btn_pause_service.setVisibility(View.GONE);
                tbutton.setVisibility(View.GONE);
                tv_start_name.setText(Utility.getAppPrefString(getActivity(), "saved_user_name"));
                tv_start_total_review.setText(Utility.getAppPrefString(getActivity(), "saved_total_review"));
                tv_start_job_type.setText(Utility.getAppPrefString(getActivity(), "cat_name"));
                ratingBar_start.setRating(Float.parseFloat(Utility.getAppPrefString(getActivity(), "saved_rating")));
                if (!Utility.getAppPrefString(getActivity(), "saved_user_image").equalsIgnoreCase("")) {
                    Glide.with(getActivity()).load(Utility.getAppPrefString(getActivity(), "saved_user_image"))
                            .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)
                                    .error(R.mipmap.user))
                            .thumbnail(0.5f)
                            .into(img_profile_start);
                }
                btn_start.setText("Finish");
                ll_time_consumed.setVisibility(View.VISIBLE);
                btn_pause_service.setVisibility(View.VISIBLE);
                if (Utility.getAppPrefString(getActivity(), "saved_current_flag").equalsIgnoreCase("start")) {
                    if (Utility.getAppPrefString(getActivity(), "saved_timer").equalsIgnoreCase("0") ||
                            Utility.getAppPrefString(getActivity(), "saved_timer").equalsIgnoreCase("")) {
                        cm_timer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                        cm_timer.start();
                        simpleChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                        simpleChronometer.start();
                    } else {
                        try {
                            cm_timer.setBase(SystemClock.elapsedRealtime() - convert(Utility.getAppPrefString(getActivity(), "saved_timer")));
                            cm_timer.start();
                            simpleChronometer.setBase(SystemClock.elapsedRealtime() - convert(Utility.getAppPrefString(getActivity(), "saved_timer")));
                            simpleChronometer.start();
                            cm_timer.setText(Utility.getAppPrefString(getActivity(), "saved_timer"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    btn_pause_service.setText("Pause");
                    btn_start.setVisibility(View.VISIBLE);
                    pause_service_flag = false;
                } else {
                    btn_pause_service.setText("Resume");
                    btn_start.setVisibility(View.GONE);
                    cm_timer.setText(Utility.getAppPrefString(getActivity(), "saved_timer"));
                    simpleChronometer.setText(Utility.getAppPrefString(getActivity(), "saved_timer"));
                    pause_service_flag = true;
                }
            }
        } else if (Utility.getAppPrefString(getActivity(), "rule_flag").equalsIgnoreCase("1")) {
            if (googleMap != null) {
                googleMap.clear();
                LatLng gps = new LatLng(latitude, longitude);
                googleMap.addMarker(new MarkerOptions()
                        .position(gps)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.blue_pin)));

                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(gps).zoom(14.0f).build()));
            }
            if (Utility.getAppPrefString(getActivity(), "guide_dialog_flag").equalsIgnoreCase("0")) {
                if (Utility.isNetworkAvaliable(getActivity())) {
                    try {
                        getRulesList getTask = new getRulesList(getActivity());
                        getTask.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return view;
    }

    public void init() {

        mapView = (MapView) view.findViewById(R.id.map1);
        mapView.onCreate(mBundle);
        mapView.getMapAsync(this);
        gps = new GPS_Fused(Fragment_Home.this, getActivity());
        gpsTracker = new GPSTracker1(getActivity());

        ll_timer = (LinearLayout) view.findViewById(R.id.rl_timer);
        tv_time = (TextView) view.findViewById(R.id.tv_timer);
        // Interviewed
        ll_interviewed = (LinearLayout) view.findViewById(R.id.ll_interviewed);
        tv_inter_name = (TextView) view.findViewById(R.id.tv_inter_name);
        tv_inter_total_review = (TextView) view.findViewById(R.id.tv_rating_inter_value);
        tv_inter_job_type = (TextView) view.findViewById(R.id.tv_inter_job_type);
        tv_inter_job_location = (TextView) view.findViewById(R.id.tv_inter_job_location);
        rl_inter_call = (RelativeLayout) view.findViewById(R.id.rl_call);
        rl_inter_call.setOnClickListener(this);
        rl_inter_chat = (RelativeLayout) view.findViewById(R.id.rl_chat);
        rl_inter_chat.setOnClickListener(this);
        btn_interviewed = (Button) view.findViewById(R.id.btn_interviewed);
        btn_interviewed.setOnClickListener(this);
        img_profile_interviewed = (ImageView) view.findViewById(R.id.img_inter_profile);
        ratingBar_inter = (RatingBar) view.findViewById(R.id.rating_inter);
        img_cancel_req = (ImageView) view.findViewById(R.id.img_cancel_req);
        img_cancel_req.setOnClickListener(this);

        // Start Service
        ll_start_service = (LinearLayout) view.findViewById(R.id.ll_start_service);
        tv_start_name = (TextView) view.findViewById(R.id.tv_start_name);
        tv_start_total_review = (TextView) view.findViewById(R.id.tv_rating_start_value);
        tv_start_job_type = (TextView) view.findViewById(R.id.tv_start_job_type);
        rl_start_call = (RelativeLayout) view.findViewById(R.id.rl_start_call);
        rl_start_call.setOnClickListener(this);
        rl_start_chat = (RelativeLayout) view.findViewById(R.id.rl_start_chat);
        rl_start_chat.setOnClickListener(this);
        img_pause_service = (ImageView) view.findViewById(R.id.img_pause_service);
        img_pause_service.setOnClickListener(this);
        btn_pause_service = (Button) view.findViewById(R.id.btn_pause_service);
        btn_pause_service.setOnClickListener(this);
        btn_start = (Button) view.findViewById(R.id.btn_start_service);
        btn_start.setOnClickListener(this);
        img_profile_start = (ImageView) view.findViewById(R.id.img_start_profile);
        ratingBar_start = (RatingBar) view.findViewById(R.id.rating_start);
        simpleChronometer = (Chronometer) view.findViewById(R.id.simpleChronometer);
        simpleChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                String t = (h < 10 ? "0" + h : h) + ":" + (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
                chronometer.setText(t);
            }
        });
        simpleChronometer.setBase(SystemClock.elapsedRealtime());
        simpleChronometer.setText("00:00:00");

        cm_timer = (Chronometer) view.findViewById(R.id.cm_timer);
        cm_timer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                String t = (h < 10 ? "0" + h : h) + ":" + (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
                chronometer.setText(t);
            }
        });
        cm_timer.setBase(SystemClock.elapsedRealtime());
        cm_timer.setText("00:00:00");
        ll_time_consumed = (LinearLayout) view.findViewById(R.id.ll_time_consumed);

        // Accept Reject
        ll_accept_reject = (LinearLayout) view.findViewById(R.id.ll_accept_reject);
        btn_accept = (Button) view.findViewById(R.id.btn_ar_accept);
        btn_accept.setOnClickListener(this);
        btn_reject = (Button) view.findViewById(R.id.btn_ar_reject);
        btn_reject.setOnClickListener(this);
        img_ar_profile = (ImageView) view.findViewById(R.id.img_ar_profile);
        tv_ar_name = (TextView) view.findViewById(R.id.tv_ar_name);
        tv_rating = (TextView) view.findViewById(R.id.tv_rating_ar_value);
        tv_job_type = (TextView) view.findViewById(R.id.tv_ar_job_type1);
        tv_location = (TextView) view.findViewById(R.id.tv_ar_location);
        ratingBar = (RatingBar) view.findViewById(R.id.rating_ar);
        view_ar_time = (View) view.findViewById(R.id.view_ar_time);
        rl_ar_time = (RelativeLayout) view.findViewById(R.id.rl_ar_time);
        tv_ar_time = (TextView) view.findViewById(R.id.tv_ar_time);
        // Current Location
        rl_my_location = (RelativeLayout) view.findViewById(R.id.rl_my_location);
        rl_my_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMyLocation();
            }
        });
        // On/Off button
        tbutton = (ToggleButton) view.findViewById(R.id.toggleButton1);
        tbutton.setOnClickListener(this);
        if (Utility.getAppPrefString(getActivity(), "switch_mode").equalsIgnoreCase("off")) {
            tbutton.setChecked(false);
        } else {
            tbutton.setChecked(true);
        }
        Utility.writeSharedPreferences(getActivity(), "from", "activity");
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        gps.stopGPS();
        active = false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toggleButton1:
                if (tbutton.isChecked()) {
                    Log.v("Switch: ", "ON");
                    type_on_off = "on";
                    Utility.writeSharedPreferences(getActivity(), "switch_mode", "on");
                    if (latitude != 0.0) {
                        Location targetLocation = new Location("");//provider name is unnecessary
                        targetLocation.setLatitude(latitude);//your coords of course
                        targetLocation.setLongitude(longitude);

                        sendLocation sendLocation = new sendLocation(getActivity(), targetLocation);
                        sendLocation.execute();

                        LatLng gps = new LatLng(latitude, longitude);
                        googleMap.addMarker(new MarkerOptions()
                                .position(gps)
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.blue_pin)));

                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(gps).zoom(14.0f).build()));
                    }
                } else {
                    Log.v("Switch: ", "OFF");
                    type_on_off = "off";
                    Utility.writeSharedPreferences(getActivity(), "switch_mode", "off");
                    googleMap.clear();
                }
                if (Utility.isNetworkAvaliable(getActivity())) {
                    try {
                        requestONOFF getTask = new requestONOFF(getActivity());
                        getTask.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.img_cancel_req:
                if (Utility.isNetworkAvaliable(getActivity())) {
                    try {
                        cancelRequest getTask = new cancelRequest(getActivity());
                        getTask.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btn_pause_service:
                if (!pause_service_flag) {
                    btn_pause_service.setText("Resume");
                    btn_start.setVisibility(View.GONE);
                    Utility.writeSharedPreferences(getActivity(), "saved_current_flag", "pause");
                    pause_service_flag = true;
                } else {
                    btn_pause_service.setText("Pause");
                    btn_start.setVisibility(View.VISIBLE);
                    Utility.writeSharedPreferences(getActivity(), "saved_current_flag", "start");
                    pause_service_flag = false;
                }
                if (Utility.isNetworkAvaliable(getActivity())) {
                    try {
                        pauseServiceTime getTask = new pauseServiceTime(getActivity());
                        getTask.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btn_start_service:
                if (btn_start.getText().toString().equalsIgnoreCase("Start")) {
                    gps.stopGPS();
                    if (Utility.isNetworkAvaliable(getActivity())) {
                        try {
                            simpleChronometer.stop();
                            startService getTask = new startService(getActivity());
                            getTask.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (Utility.isNetworkAvaliable(getActivity())) {
                        simpleChronometer.stop();
                        try {
                            if (googleMap != null) {
                                if (polylinePath != null)
                                    polylinePath.remove();

                                googleMap.clear();
                                LatLng gps = new LatLng(latitude, longitude);
                                googleMap.addMarker(new MarkerOptions()
                                        .position(gps)
                                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.blue_pin)));

                                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(gps).zoom(14.0f).build()));
                            }

                            notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancelAll();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Utility.writeSharedPreferences(getActivity(), "saved_current_flag", "");
                        Utility.writeSharedPreferences(getActivity(), "data_flag", "false");
                        Fragment frg = getFragmentManager().findFragmentById(R.id.frame);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.detach(frg);
                        ft.attach(frg);
                        ft.commit();

                        finishService getTask = new finishService(getActivity());
                        getTask.execute();
                    }
                }
                break;
            case R.id.rl_call:
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + "+63" + user_mobile));
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callIntent);
                break;
            case R.id.rl_chat:
                Utility.writeSharedPreferences(getActivity(), "chat_from", "activity");
                Intent intent_chat = new Intent(getActivity(), ChatActivity.class);
                intent_chat.putExtra("worker_id", user_id);
                intent_chat.putExtra("notification_id", notification_id);
                intent_chat.putExtra("user_name", tv_inter_name.getText().toString());
                intent_chat.putExtra("user_image", user_pic);
                intent_chat.putExtra("user_mobile", user_mobile);
                intent_chat.putExtra("chatFrom", "activity");
                startActivity(intent_chat);
                break;
            case R.id.rl_start_call:
                Intent callIntent1 = new Intent(Intent.ACTION_CALL);
                callIntent1.setData(Uri.parse("tel:" + "+63" + user_mobile));
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(callIntent1);
                break;
            case R.id.rl_start_chat:
                Utility.writeSharedPreferences(getActivity(), "chat_from", "activity");
                Intent intent_chat1 = new Intent(getActivity(), ChatActivity.class);
                intent_chat1.putExtra("worker_id", user_id);
                intent_chat1.putExtra("notification_id", notification_id);
                intent_chat1.putExtra("user_name", tv_inter_name.getText().toString());
                intent_chat1.putExtra("user_image", user_pic);
                intent_chat1.putExtra("user_mobile", user_mobile);
                intent_chat1.putExtra("chatFrom", "activity");
                startActivity(intent_chat1);
                break;
            case R.id.btn_interviewed:
                if (Utility.isNetworkAvaliable(getActivity())) {
                    try {
                        interviewedService getTask = new interviewedService(getActivity());
                        getTask.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btn_ar_accept:
                count.cancel();
                Utility.writeSharedPreferences(getActivity(), "accept_dialog_flag", "0");
                ll_timer.setVisibility(View.GONE);
                type = "accept";
                if (Utility.isNetworkAvaliable(getActivity())) {
                    try {
                        acceptRejectRequest getTask = new acceptRejectRequest(getActivity());
                        getTask.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btn_ar_reject:
                count.cancel();
                Utility.writeSharedPreferences(getActivity(), "accept_dialog_flag", "0");
                ll_timer.setVisibility(View.GONE);
                ll_interviewed.setVisibility(View.GONE);
                img_cancel_req.setVisibility(View.GONE);
                type = "cancel";
                if (Utility.isNetworkAvaliable(getActivity())) {
                    try {
                        acceptRejectRequest getTask = new acceptRejectRequest(getActivity());
                        getTask.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    public void onResume() {
        mapView.onResume();
        if (!gps.isRunning()) gps.resumeGPS();
        super.onResume();
        active = true;
        getActivity().registerReceiver(mMessageReceiver, new IntentFilter("unique_name"));

        if (Utility.getAppPrefString(getActivity(), "accept_dialog_flag").equalsIgnoreCase("1")) {
            tbutton.setVisibility(View.GONE);
            ll_accept_reject.setVisibility(View.VISIBLE);
            ll_start_service.setVisibility(View.GONE);
            ll_time_consumed.setVisibility(View.GONE);
            btn_pause_service.setVisibility(View.GONE);
            ll_interviewed.setVisibility(View.GONE);
            img_cancel_req.setVisibility(View.GONE);

            try {
                tv_ar_name.setText(Utility.getAppPrefString(getActivity(), "noti_user_name"));
                tv_rating.setText(Utility.getAppPrefString(getActivity(), "noti_total_review"));
                tv_job_type.setText(Utility.getAppPrefString(getActivity(), "cat_name"));
                notification_id = Utility.getAppPrefString(getActivity(), "noti_notification_id");
                service_id = Utility.getAppPrefString(getActivity(), "noti_service_id");
                user_id = Utility.getAppPrefString(getActivity(), "noti_user_id");
                request_type = Utility.getAppPrefString(getActivity(), "noti_type");
                user_pic = Utility.getAppPrefString(getActivity(), "noti_user_pic");
                user_mobile = Utility.getAppPrefString(getActivity(), "noti_user_mobile");
                ratingBar.setRating(Float.parseFloat(Utility.getAppPrefString(getActivity(), "noti_user_rating")));
                Glide.with(getActivity()).load(Utility.getAppPrefString(getActivity(), "noti_user_pic"))
                        .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)
                                .error(R.mipmap.user))
                        .thumbnail(0.5f)
                        .into(img_ar_profile);

                if (request_type.equalsIgnoreCase("schedule")) {
                    view_ar_time.setVisibility(View.VISIBLE);
                    rl_ar_time.setVisibility(View.VISIBLE);
                    tv_ar_time.setText(Utility.getAppPrefString(getActivity(), "noti_date") + " at " + Utility.getAppPrefString(getActivity(), "noti_time"));
                } else if (request_type.equalsIgnoreCase("schedule1")) {
                    view_ar_time.setVisibility(View.VISIBLE);
                    rl_ar_time.setVisibility(View.VISIBLE);
                    tv_ar_time.setText(Utility.getAppPrefString(getActivity(), "noti_date_time"));
                } else {
                    view_ar_time.setVisibility(View.GONE);
                    rl_ar_time.setVisibility(View.GONE);
                }

                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(getActivity(), Locale.getDefault());
                addresses = geocoder.getFromLocation(Double.parseDouble(Utility.getAppPrefString(getActivity(), "noti_user_latitude")), Double.parseDouble(Utility.getAppPrefString(getActivity(), "noti_user_longitude")), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                tv_location.setText(address);

                POINT_A = new LatLng(Double.parseDouble(Utility.getAppPrefString(getActivity(), "latitude")),
                        Double.parseDouble(Utility.getAppPrefString(getActivity(), "longitude")));
                POINT_B = new LatLng(Double.parseDouble(Utility.getAppPrefString(getActivity(), "noti_user_latitude")),
                        Double.parseDouble(Utility.getAppPrefString(getActivity(), "noti_user_longitude")));
                MarkerPoints = new ArrayList();
                MarkerPoints.add(POINT_A);
                MarkerPoints.add(POINT_B);

                if (googleMap != null) {
                    try {
                        if (MarkerPoints.size() >= 2) {
                            POINT_WORKER = (LatLng) MarkerPoints.get(0);
                            POINT_USER = (LatLng) MarkerPoints.get(1);
                            String url = getUrl(POINT_WORKER, POINT_USER);
                            new FetchUrl().execute(new String[]{url});
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            ll_timer.setVisibility(View.VISIBLE);
            if (!isRunning) {
                count = new count(61000, 1000);
                count.start();
            }
        } else if (Utility.getAppPrefString(getActivity(), "activity_from").equalsIgnoreCase("cancel")) {
            Toast.makeText(getActivity(), "Job Cancel", Toast.LENGTH_SHORT).show();
            Utility.writeSharedPreferences(getActivity(), "data_flag", "false");
            if (googleMap != null) {
                polylinePath.remove();
                googleMap.clear();
                if (gpsTracker != null) {
                    LatLng gps = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
                    googleMap.addMarker(new MarkerOptions()
                            .position(gps)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.blue_pin)));
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(gps).zoom(14.0f).build()));
                }
            }
            ll_timer.setVisibility(View.GONE);
            ll_interviewed.setVisibility(View.GONE);
            ll_accept_reject.setVisibility(View.GONE);
            img_cancel_req.setVisibility(View.GONE);
            ll_start_service.setVisibility(View.GONE);
            tbutton.setVisibility(View.VISIBLE);
            ll_time_consumed.setVisibility(View.GONE);
            btn_pause_service.setVisibility(View.GONE);

            notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        } else if (Utility.getAppPrefString(getActivity(), "activity_from").equalsIgnoreCase("cancel_request")) {
            if (googleMap != null) {
                polylinePath.remove();
                googleMap.clear();
                if (gpsTracker.canGetLocation()) {
                    LatLng gps = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
                    googleMap.addMarker(new MarkerOptions()
                            .position(gps)
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.blue_pin)));

                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(gps).zoom(14.0f).build()));
                }
            }
            count.cancel();
            ll_timer.setVisibility(View.GONE);

            ll_interviewed.setVisibility(View.GONE);
            ll_accept_reject.setVisibility(View.GONE);
            img_cancel_req.setVisibility(View.GONE);
            ll_start_service.setVisibility(View.GONE);
            tbutton.setVisibility(View.VISIBLE);
            ll_time_consumed.setVisibility(View.GONE);
            btn_pause_service.setVisibility(View.GONE);

            notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        active = false;
        gps.stopGPS();
        MyApplication.activityPaused();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.map_style);
        this.googleMap.setMapStyle(style);

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (status != 0) {
            GooglePlayServicesUtil.getErrorDialog(status, getActivity(), 10).show();
        } else if (googleMap != null) {

            if (Utility.getAppPrefString(getActivity(), "accept_dialog_flag").equalsIgnoreCase("1")) {
                try {
                    if (MarkerPoints.size() >= 2) {
                        POINT_WORKER = (LatLng) MarkerPoints.get(0);
                        POINT_USER = (LatLng) MarkerPoints.get(1);
                        String url = getUrl(POINT_WORKER, POINT_USER);
                        new FetchUrl().execute(new String[]{url});
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (Utility.getAppPrefString(getActivity(), "data_flag").equalsIgnoreCase("true")) {
                try {
                    latitude = Double.parseDouble(Utility.getAppPrefString(getActivity(), "latitude"));
                    longitude = Double.parseDouble(Utility.getAppPrefString(getActivity(), "longitude"));
                    POINT_WORKER = new LatLng(latitude, longitude);
                    POINT_USER = new LatLng(Double.parseDouble(user_latitude),
                            Double.parseDouble(user_longitude));
                    String url = getUrl(POINT_WORKER, POINT_USER);
                    new FetchUrl().execute(new String[]{url});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mView = mapView.findViewById(Integer.parseInt("1"));
                View locationButton = ((View) mView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
                rlp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                googleMap.setPadding(0, 0, 12, 0);

                try {
                    latitude = Double.parseDouble(Utility.getAppPrefString(getActivity(), "latitude"));
                    longitude = Double.parseDouble(Utility.getAppPrefString(getActivity(), "longitude"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (Utility.getAppPrefString(getActivity(), "switch_mode").equalsIgnoreCase("off")) {
                    if (latitude != 0.0) {
                        Location targetLocation = new Location("");//provider name is unnecessary
                        targetLocation.setLatitude(latitude);//your coords of course
                        targetLocation.setLongitude(longitude);

                        sendLocation sendLocation = new sendLocation(getActivity(), targetLocation);
                        sendLocation.execute();

                        LatLng gps = new LatLng(latitude, longitude);
                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(gps).zoom(14.0f).build()));
                    }
                } else {
                    if (latitude != 0.0) {
                        Location targetLocation = new Location("");//provider name is unnecessary
                        targetLocation.setLatitude(latitude);//your coords of course
                        targetLocation.setLongitude(longitude);

                        sendLocation sendLocation = new sendLocation(getActivity(), targetLocation);
                        sendLocation.execute();

                        LatLng gps = new LatLng(latitude, longitude);
                        googleMap.addMarker(new MarkerOptions()
                                .position(gps)
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.blue_pin)));

                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(gps).zoom(14.0f).build()));
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void locationChanged(double longitude, double latitude) {
        if (ll_interviewed.getVisibility() == View.VISIBLE) {
            double user_lat = Double.parseDouble(Utility.getAppPrefString(getActivity(), "saved_user_latitude"));
            double user_lng = Double.parseDouble(Utility.getAppPrefString(getActivity(), "saved_user_longitude"));
            double distance = getDistance(latitude, longitude, user_lat, user_lng);
            Log.v("Distance:", distance + "");

            Location temp = new Location(LocationManager.GPS_PROVIDER);
            temp.setLatitude(latitude);
            temp.setLongitude(longitude);

            if (distance > 25.00) {
                btn_interviewed.setEnabled(false);
                btn_interviewed.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#696969")));
                sendLocation sendLocation = new sendLocation(getActivity(), temp);
                sendLocation.execute();
            } else {
                btn_interviewed.setEnabled(true);
                btn_interviewed.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#15B8F8")));
            }
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(strUrl).openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String str = "";
            while (true) {
                str = br.readLine();
                if (str == null) {
                    break;
                }
                sb.append(str);
            }
            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private String getUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        return "https://maps.googleapis.com/maps/api/directions/json?" + ("origin=" + origin.latitude + "," + origin.longitude) + "&" + "destination=" + dest.latitude + "," + dest.longitude + "&sensor=false&units=metric&mode=driving&key=AIzaSyC11_uyqNi5jOPnHL7c1PRJLPCKxrYWiTw";
//        return "https://maps.googleapis.com/maps/api/directions/json?" + ("origin=" + origin.latitude + "," + origin.longitude) + "&" + "destination=" + dest.latitude + "," + dest.longitude + "&sensor=false&units=metric&mode=driving&key=AIzaSyAUWhvhUNKcpe9BOIkmwtVYi9Rt2Ysyk1A";
    }

    private long convert(String time) throws ParseException {
        String myDate = time;
        long milliseconds;

        String[] arrayString = myDate.split(":");
        int hours = Integer.parseInt(arrayString[0]);
        int minutes = Integer.parseInt(arrayString[1]);

        char toCheck = ':';
        int count = 0;

        for (char ch : myDate.toCharArray()) {
            if (ch == toCheck) {
                count++;
            }
        }

        if (count > 1) {
            int second = Integer.parseInt(arrayString[2]);
            milliseconds = TimeUnit.SECONDS.toMillis(TimeUnit.HOURS.toSeconds(hours) + TimeUnit.MINUTES.toSeconds(minutes) + TimeUnit.SECONDS.toSeconds(second));
        } else {
            milliseconds = TimeUnit.SECONDS.toMillis(TimeUnit.HOURS.toSeconds(hours) + TimeUnit.MINUTES.toSeconds(minutes));
        }
        return milliseconds;
    }

    // get Current Location
    private void getMyLocation() {
        if (gpsTracker.canGetLocation()) {
            LatLng latLng = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f);
            googleMap.animateCamera(cameraUpdate);
        }
    }

    // After 60 sec API
    public void removeRequest() {
        backgroundData = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String webUrl = Constant.URL_60_SEC_COMPLETED;
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("service_id", service_id));
                    String response1 = Utility.postRequest(getActivity(), webUrl, nameValuePairs);

                    JSONObject jObject = new JSONObject(response1);

                    Log.v("Response:", jObject.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        backgroundData.start();
    }

    public double getDistance(double lat1, double lng1, double lat2, double lng2) {
        Location startPoint = new Location("locationA");
        startPoint.setLatitude(lat1);
        startPoint.setLongitude(lng1);

        Location endPoint = new Location("locationA");
        endPoint.setLatitude(lat2);
        endPoint.setLongitude(lng2);

        return startPoint.distanceTo(endPoint);
    }

    private interface LatLngInterpolator {
        LatLng interpolate(float f, LatLng latLng, LatLng latLng2);

        public static class LinearFixed implements LatLngInterpolator {
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = ((b.latitude - a.latitude) * ((double) fraction)) + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                if (Math.abs(lngDelta) > 180.0d) {
                    lngDelta -= Math.signum(lngDelta) * 360.0d;
                }
                return new LatLng(lat, (((double) fraction) * lngDelta) + a.longitude);
            }
        }
    }

    /*
     * Send Location API
     */
    public class sendLocation extends AsyncTask<String, Integer, Object> {
        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String message;
        String response;
        private Location location;

        public sendLocation(Context mContext, Location location) {
            this.mContext = mContext;
            this.location = location;
            this.mProgressDialog = new DME_ProgressDilog(mContext);
            this.mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            this.mProgressDialog.setCancelable(false);
            this.mProgressDialog.setCanceledOnTouchOutside(false);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            if (this.mProgressDialog != null && !this.mProgressDialog.isShowing()) {
            }
        }

        protected Object doInBackground(String... params) {
            try {
                getAboutMeListItem();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return Integer.valueOf(0);
        }

        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            try {
                if (response.equalsIgnoreCase("true")) {

                    POINT_WORKER = new LatLng(location.getLatitude(), location.getLongitude());

                    if (user_latitude != null) {
                        POINT_USER = new LatLng(Double.parseDouble(user_latitude), Double.parseDouble(user_longitude));
                        String url = getUrl(POINT_WORKER, POINT_USER);
                        if (Utility.getAppPrefString(getActivity(), "data_flag").equalsIgnoreCase("true"))
                            new FetchUrl().execute(new String[]{url});
                    }

                } else {
                    Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), this.message, 1000).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_LOCATION_UPDATE;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("latitude", location.getLatitude() + ""));
                nameValuePairs.add(new BasicNameValuePair("longitude", location.getLongitude() + ""));

                JSONObject jObject = new JSONObject(Utility.postRequest(this.mContext, webUrl, nameValuePairs));

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }
    }

    public class getRulesList extends AsyncTask<String, Integer, Object> {
        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        JSONArray jsonArray;
        JSONObject jsonObjectMessage;
        String message, response, url;

        public getRulesList(Context mContext) {
            this.mContext = mContext;
            this.mProgressDialog = new DME_ProgressDilog(mContext);
            this.mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            this.mProgressDialog.setCancelable(false);
            this.mProgressDialog.setCanceledOnTouchOutside(false);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            if (this.mProgressDialog != null && !this.mProgressDialog.isShowing()) {
                this.mProgressDialog.show();
            }
        }

        protected Object doInBackground(String... params) {
            try {
                getAboutMeListItem();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return Integer.valueOf(0);
        }

        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
                this.mProgressDialog.dismiss();
            }
            try {
                Utility.writeSharedPreferences(mContext, "help_url", url);
                Utility.writeSharedPreferences(mContext, "guide_dialog_flag", "1");
                if (response.equalsIgnoreCase("true")) {
                    final Dialog dialog = new Dialog(this.mContext);
                    dialog.requestWindowFeature(1);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                    dialog.setContentView(R.layout.dialog_guide);

                    ((ListView) dialog.findViewById(R.id.list_guide)).setAdapter(new GiudeListAdapter(getActivity(), rules_image, rules_name));
                    ((Button) dialog.findViewById(R.id.btn_guide_ok)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });

                    Window window = dialog.getWindow();
                    WindowManager.LayoutParams wlp = window.getAttributes();
                    wlp.gravity = Gravity.CENTER;
                    wlp.width = ActionBar.LayoutParams.MATCH_PARENT;
                    window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    window.setAttributes(wlp);
                    dialog.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_GET_RULES;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("deviceID", Utility.getAppPrefString(mContext, "device_id")));

                JSONObject jObject = new JSONObject(Utility.postRequest(this.mContext, webUrl, nameValuePairs));

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");
                url = jObject.getString("help_url");

                if (response.equalsIgnoreCase("true")) {

                    jsonArray = jObject.getJSONArray("rule_data");

                    int lenth = this.jsonArray.length();
                    rules_image = new String[lenth];
                    rules_name = new String[lenth];
                    for (int a = 0; a < lenth; a++) {
                        this.jsonObjectMessage = this.jsonArray.getJSONObject(a);
                        try {
                            rules_image[a] = this.jsonObjectMessage.getString("rule_image");
                            rules_name[a] = this.jsonObjectMessage.getString("rule_name");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Snackbar.make(getActivity().getWindow().getDecorView().getRootView(), this.message, 1000).show();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }
    }

    public class acceptRejectRequest extends AsyncTask<String, Integer, Object> {
        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String message;
        String response;

        public acceptRejectRequest(Context mContext) {
            this.mContext = mContext;
            this.mProgressDialog = new DME_ProgressDilog(mContext);
            this.mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            this.mProgressDialog.setCancelable(false);
            this.mProgressDialog.setCanceledOnTouchOutside(false);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            if (this.mProgressDialog != null && !this.mProgressDialog.isShowing()) {
                this.mProgressDialog.show();
            }
        }

        protected Object doInBackground(String... params) {
            try {
                getAboutMeListItem();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return Integer.valueOf(0);
        }

        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
                this.mProgressDialog.dismiss();
            }
            try {
                if (response.equalsIgnoreCase("true")) {
                    ll_accept_reject.setVisibility(View.GONE);
                    if (type.equalsIgnoreCase("accept")) {
                        Utility.writeSharedPreferences(getActivity(), "saved_current_flag", "interview");
                        ll_interviewed.setVisibility(View.VISIBLE);
                        img_cancel_req.setVisibility(View.VISIBLE);
                        tv_inter_name.setText(tv_ar_name.getText().toString());
                        tv_inter_total_review.setText(tv_rating.getText().toString());
                        tv_inter_job_type.setText(Utility.getAppPrefString(mContext, "cat_name"));
                        tv_inter_job_location.setText(tv_location.getText().toString());
                        float rating = ratingBar.getRating();
                        ratingBar_inter.setRating(rating);
                        if (!user_pic.equalsIgnoreCase("")) {
                            Glide.with(mContext).load(user_pic)
                                    .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)
                                            .error(R.mipmap.user))
                                    .thumbnail(0.5f)
                                    .into(img_profile_interviewed);
                        }

                        ll_start_service.setVisibility(View.GONE);
                        tbutton.setVisibility(View.GONE);
                        ll_time_consumed.setVisibility(View.GONE);
                        btn_pause_service.setVisibility(View.GONE);
//                        updateUI();
                        if (!gps.isRunning()) gps.resumeGPS();
                    } else {
                        if (googleMap != null) {
                            polylinePath.remove();
                            googleMap.clear();
                            LatLng gps = new LatLng(gpsTracker.getLatitude(),
                                    gpsTracker.getLongitude());
                            googleMap.addMarker(new MarkerOptions()
                                    .position(gps)
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.blue_pin)));

                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(gps).zoom(14.0f).build()));
                        }
                        notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancelAll();

                        ll_interviewed.setVisibility(View.GONE);
                        ll_accept_reject.setVisibility(View.GONE);
                        img_cancel_req.setVisibility(View.GONE);
                        ll_start_service.setVisibility(View.GONE);
                        tbutton.setVisibility(View.VISIBLE);
                        ll_time_consumed.setVisibility(View.GONE);
                        btn_pause_service.setVisibility(View.GONE);
                    }
                    Utility.writeSharedPreferences(mContext, "accept_dialog_flag", "0");
                } else {
                    googleMap.clear();
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    ll_accept_reject.setVisibility(View.GONE);
                    ll_interviewed.setVisibility(View.GONE);
                    img_cancel_req.setVisibility(View.GONE);
                    ll_start_service.setVisibility(View.GONE);
                    tbutton.setVisibility(View.VISIBLE);
                    ll_time_consumed.setVisibility(View.GONE);
                    btn_pause_service.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_ACCEPT_REJECT;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList();

                if (Utility.getAppPrefString(getActivity(), "request").equalsIgnoreCase("normal")) {
                    nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                    nameValuePairs.add(new BasicNameValuePair("service_id", service_id));
                    nameValuePairs.add(new BasicNameValuePair("type", type));
                    nameValuePairs.add(new BasicNameValuePair("service_type", "normal"));
                    nameValuePairs.add(new BasicNameValuePair("worker_latitude", latitude + ""));
                    nameValuePairs.add(new BasicNameValuePair("worker_longitude", longitude + ""));
                } else if (Utility.getAppPrefString(getActivity(), "request").equalsIgnoreCase("schedule_normal_req")) {
                    nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                    nameValuePairs.add(new BasicNameValuePair("service_id", service_id));
                    nameValuePairs.add(new BasicNameValuePair("type", type));
                    nameValuePairs.add(new BasicNameValuePair("service_type", "schedual_normal"));
                    nameValuePairs.add(new BasicNameValuePair("worker_latitude", latitude + ""));
                    nameValuePairs.add(new BasicNameValuePair("worker_longitude", longitude + ""));
                } else {
                    nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                    nameValuePairs.add(new BasicNameValuePair("type", type));
                    nameValuePairs.add(new BasicNameValuePair("service_id", service_id));
                    nameValuePairs.add(new BasicNameValuePair("service_type", "cron"));
                    nameValuePairs.add(new BasicNameValuePair("worker_latitude", latitude + ""));
                    nameValuePairs.add(new BasicNameValuePair("worker_longitude", longitude + ""));
                }

                JSONObject jObject = new JSONObject(Utility.postRequest(this.mContext, webUrl, nameValuePairs));

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }
    }

    public class interviewedService extends AsyncTask<String, Integer, Object> {
        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String message;
        String response;

        public interviewedService(Context mContext) {
            this.mContext = mContext;
            this.mProgressDialog = new DME_ProgressDilog(mContext);
            this.mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            this.mProgressDialog.setCancelable(false);
            this.mProgressDialog.setCanceledOnTouchOutside(false);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            if (this.mProgressDialog != null && !this.mProgressDialog.isShowing()) {
                this.mProgressDialog.show();
            }
        }

        protected Object doInBackground(String... params) {
            try {
                getAboutMeListItem();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return Integer.valueOf(0);
        }

        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
                this.mProgressDialog.dismiss();
            }
            try {
                if (response.equalsIgnoreCase("true")) {
                    ll_start_service.setVisibility(View.VISIBLE);
                    ll_interviewed.setVisibility(View.GONE);
                    img_cancel_req.setVisibility(View.GONE);
                    ll_time_consumed.setVisibility(View.GONE);
                    btn_pause_service.setVisibility(View.GONE);
                    tbutton.setVisibility(View.GONE);
                    if (Utility.getAppPrefString(getActivity(), "data_flag").equalsIgnoreCase("true")) {
                        tv_start_name.setText(Utility.getAppPrefString(getActivity(), "saved_user_name"));
                        tv_start_total_review.setText(Utility.getAppPrefString(getActivity(), "saved_total_review"));
                        tv_start_job_type.setText(Utility.getAppPrefString(mContext, "cat_name"));
                        ratingBar_start.setRating(Float.parseFloat(Utility.getAppPrefString(getActivity(), "saved_rating")));
                        if (!user_pic.equalsIgnoreCase("")) {
                            Glide.with(mContext).load(user_pic)
                                    .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)
                                            .error(R.mipmap.user))
                                    .thumbnail(0.5f)
                                    .into(img_profile_start);
                        }
                    } else {
                        tv_start_name.setText(tv_inter_name.getText().toString());
                        tv_start_total_review.setText(tv_inter_total_review.getText().toString());
                        tv_start_job_type.setText(Utility.getAppPrefString(mContext, "cat_name"));
                        ratingBar_start.setRating(ratingBar_inter.getRating());
                        if (!user_pic.equalsIgnoreCase("")) {
                            Glide.with(mContext).load(user_pic)
                                    .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)
                                            .error(R.mipmap.user))
                                    .thumbnail(0.5f)
                                    .into(img_profile_start);
                        }
                    }
                    simpleChronometer.setBase(SystemClock.elapsedRealtime());
                    simpleChronometer.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_INTERVIEWED_SERVICE;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList();

                nameValuePairs.add(new BasicNameValuePair("user_id", user_id));
                nameValuePairs.add(new BasicNameValuePair("service_id", service_id));
                nameValuePairs.add(new BasicNameValuePair("worker_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));

                JSONObject jObject = new JSONObject(Utility.postRequest(this.mContext, webUrl, nameValuePairs));

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }
    }

    public class startService extends AsyncTask<String, Integer, Object> {
        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String message;
        String response;

        public startService(Context mContext) {
            this.mContext = mContext;
            this.mProgressDialog = new DME_ProgressDilog(mContext);
            this.mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            this.mProgressDialog.setCancelable(false);
            this.mProgressDialog.setCanceledOnTouchOutside(false);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            if (this.mProgressDialog != null && !this.mProgressDialog.isShowing()) {
                this.mProgressDialog.show();
            }
        }

        protected Object doInBackground(String... params) {
            try {
                getAboutMeListItem();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return Integer.valueOf(0);
        }

        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
                this.mProgressDialog.dismiss();
            }
            try {
                if (response.equalsIgnoreCase("true")) {
                    btn_start.setText("Finish");
                    ll_time_consumed.setVisibility(View.VISIBLE);
                    btn_pause_service.setVisibility(View.VISIBLE);
                    btn_pause_service.setText("Pause");
                    pause_service_flag = false;
                    cm_timer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                    cm_timer.start();
                    simpleChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                    simpleChronometer.start();
                } else {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_START_SERVICES;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList();

                nameValuePairs.add(new BasicNameValuePair("user_id", user_id));
                nameValuePairs.add(new BasicNameValuePair("service_id", service_id));
                nameValuePairs.add(new BasicNameValuePair("worker_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("hh_mm", simpleChronometer.getText().toString()));

                JSONObject jObject = new JSONObject(Utility.postRequest(this.mContext, webUrl, nameValuePairs));

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

                if (response.equalsIgnoreCase("true")) {
                    us_id = jObject.getString("us_id");
                }

            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }
    }

    public class finishService extends AsyncTask<String, Integer, Object> {
        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String message, response, r_user_id, r_user_name, r_user_rate, r_user_profile, r_user_total_review;

        public finishService(Context mContext) {
            this.mContext = mContext;
            this.mProgressDialog = new DME_ProgressDilog(mContext);
            this.mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            this.mProgressDialog.setCancelable(false);
            this.mProgressDialog.setCanceledOnTouchOutside(false);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            if (this.mProgressDialog != null && !this.mProgressDialog.isShowing()) {
                this.mProgressDialog.show();
            }
        }

        protected Object doInBackground(String... params) {
            try {
                getAboutMeListItem();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return Integer.valueOf(0);
        }

        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
                this.mProgressDialog.dismiss();
            }
            try {
                if (response.equalsIgnoreCase("true")) {
                    Utility.writeSharedPreferences(mContext, "data_flag", "false");
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();

                    dialog = new Dialog(getActivity());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setBackgroundDrawable(
                            new ColorDrawable(Color.TRANSPARENT));
                    dialog.setContentView(R.layout.dialog_worker_rating);
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);

                    ImageView img_profile = (ImageView) dialog.findViewById(R.id.img_ar_profile);
                    TextView tv_name = (TextView) dialog.findViewById(R.id.tv_ar_name);
                    RatingBar ratingBar = (RatingBar) dialog.findViewById(R.id.rating_ar);
                    TextView tv_total_review = (TextView) dialog.findViewById(R.id.tv_rating_ar_value);

                    try {
                        tv_name.setText(r_user_name);
                        ratingBar.setRating(Float.parseFloat(r_user_rate));
                        tv_total_review.setText("(" + r_user_total_review + ")");
                        if (!r_user_profile.equalsIgnoreCase("")) {
                            Glide.with(getActivity()).load(r_user_profile)
                                    .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)
                                            .error(R.mipmap.user))
                                    .thumbnail(0.5f)
                                    .into(img_profile);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    ratingBar1 = (RatingBar) dialog.findViewById(R.id.rate_worker);
                    et_comments = (EditText) dialog.findViewById(R.id.et_comments);
                    Button btn_submit = (Button) dialog.findViewById(R.id.btn_add_rating);

                    btn_submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (ratingBar1.getRating() > 0) {
                                if (Utility.isNetworkAvaliable(getActivity())) {
                                    try {
                                        addRating getTask = new addRating(getActivity());
                                        getTask.execute();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                Toast.makeText(getActivity(), "Please give rating", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                    Window window = dialog.getWindow();
                    WindowManager.LayoutParams wlp = window.getAttributes();

                    wlp.gravity = Gravity.CENTER;
                    wlp.width = ActionBar.LayoutParams.MATCH_PARENT;
                    window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    window.setAttributes(wlp);

                    dialog.show();
                } else {
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_FINISH_SERVICES;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList();

                nameValuePairs.add(new BasicNameValuePair("user_id", user_id));
                nameValuePairs.add(new BasicNameValuePair("service_id", service_id));
                nameValuePairs.add(new BasicNameValuePair("worker_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("hh_mm", simpleChronometer.getText().toString()));

                JSONObject jObject = new JSONObject(Utility.postRequest(this.mContext, webUrl, nameValuePairs));

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

                if (response.equalsIgnoreCase("true")) {
                    r_user_id = jObject.getString("user_id");
                    r_user_name = jObject.getString("user_name");
                    r_user_rate = jObject.getString("user_rate");
                    r_user_profile = jObject.getString("user_profile");
                    r_user_total_review = jObject.getString("user_total_review");
                }

            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }
    }

    public class requestONOFF extends AsyncTask<String, Integer, Object> {
        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String message;
        String response;

        public requestONOFF(Context mContext) {
            this.mContext = mContext;
            this.mProgressDialog = new DME_ProgressDilog(mContext);
            this.mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            this.mProgressDialog.setCancelable(false);
            this.mProgressDialog.setCanceledOnTouchOutside(false);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            if (this.mProgressDialog != null && !this.mProgressDialog.isShowing()) {
                this.mProgressDialog.show();
            }
        }

        protected Object doInBackground(String... params) {
            try {
                getAboutMeListItem();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return Integer.valueOf(0);
        }

        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            try {
                if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
                    this.mProgressDialog.dismiss();
                }

                if (response.equalsIgnoreCase("true")) {
                    ll_accept_reject.setVisibility(View.GONE);
                    ll_start_service.setVisibility(View.GONE);
                    ll_interviewed.setVisibility(View.GONE);
                    img_cancel_req.setVisibility(View.GONE);
                    ll_time_consumed.setVisibility(View.GONE);
                    btn_pause_service.setVisibility(View.GONE);
                    tbutton.setVisibility(View.VISIBLE);
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                } else {

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_ON_OFF;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("type", type_on_off));

                JSONObject jObject = new JSONObject(Utility.postRequest(this.mContext, webUrl, nameValuePairs));

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }
    }

    public class pauseServiceTime extends AsyncTask<String, Integer, Object> {
        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String message;
        String response, time;

        public pauseServiceTime(Context mContext) {
            this.mContext = mContext;
            this.mProgressDialog = new DME_ProgressDilog(mContext);
            this.mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            this.mProgressDialog.setCancelable(false);
            this.mProgressDialog.setCanceledOnTouchOutside(false);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            if (this.mProgressDialog != null && !this.mProgressDialog.isShowing()) {
                this.mProgressDialog.show();
            }
        }

        protected Object doInBackground(String... params) {
            try {
                getAboutMeListItem();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return Integer.valueOf(0);
        }

        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
                this.mProgressDialog.dismiss();
            }
            try {
                if (response.equalsIgnoreCase("true")) {

                    if (!pause_service_flag) {
                        if (time.equalsIgnoreCase("0")) {
                            time = "00:00:00";
                        }
                        cm_timer.setBase(SystemClock.elapsedRealtime() - convert(time));
                        cm_timer.start();
                        simpleChronometer.setBase(SystemClock.elapsedRealtime() - convert(time));
                        Utility.writeSharedPreferences(getActivity(), "saved_timer", time);
                        simpleChronometer.start();
                        cm_timer.setText(time);
                    } else {
                        Utility.writeSharedPreferences(getActivity(), "saved_timer", time);
                        cm_timer.stop();
                        simpleChronometer.stop();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_PAUSE_SERVICE;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList();

                nameValuePairs.add(new BasicNameValuePair("worker_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                if (!pause_service_flag) {
                    nameValuePairs.add(new BasicNameValuePair("type", "start"));
                } else {
                    nameValuePairs.add(new BasicNameValuePair("type", "pause"));
                }

                JSONObject jObject = new JSONObject(Utility.postRequest(this.mContext, webUrl, nameValuePairs));

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");
                if (response.equalsIgnoreCase("true"))
                    time = jObject.getString("time");

            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {
        private FetchUrl() {
        }

        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
                return data;
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
                return data;
            }
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(new String[]{result});
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        private ParserTask() {
        }

        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            List<List<HashMap<String, String>>> routes = null;
            try {
                JSONObject jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());
                return routes;
            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
                return routes;
            }
        }

        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            PolylineOptions lineOptions = null;
            for (int i = 0; i < result.size(); i++) {
                ArrayList<LatLng> points = new ArrayList();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = (List) result.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = (HashMap) path.get(j);
                    points.add(new LatLng(Double.parseDouble((String) point.get("lat")), Double.parseDouble((String) point.get("lng"))));
                }
                lineOptions.addAll(points);
                lineOptions.width(6.0f);
                lineOptions.geodesic(true);
                lineOptions.color(Color.parseColor("#10B4F1"));
                Log.d("onPostExecute", "onPostExecute lineoptions decoded");
            }
            if (lineOptions != null) {
                try {
                    googleMap.clear();

                    polylinePath = googleMap.addPolyline(lineOptions);

                    MarkerOptions options = new MarkerOptions();
                    options.position(POINT_WORKER);
                    options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.blue_pin));
                    googleMap.addMarker(options);

                    MarkerOptions options1 = new MarkerOptions();
                    options1.position(POINT_USER);
                    options1.icon(BitmapDescriptorFactory.fromResource(R.mipmap.orange_pin));
                    googleMap.addMarker(options1);

                    mk = googleMap.addMarker(new MarkerOptions().position(new LatLng(POINT_WORKER.latitude, POINT_WORKER.longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.blue_pin)));

                    Location mLastLocation = new Location("");
                    mLastLocation.setLatitude(POINT_WORKER.latitude);
                    mLastLocation.setLongitude(POINT_WORKER.longitude);

                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(POINT_WORKER).zoom(14.0f).build()));
                    Fragment_Home.animateMarker(mLastLocation, mk);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }

    public class cancelRequest extends AsyncTask<String, Integer, Object> {
        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String message;
        String response;

        public cancelRequest(Context mContext) {
            this.mContext = mContext;
            this.mProgressDialog = new DME_ProgressDilog(mContext);
            this.mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            this.mProgressDialog.setCancelable(false);
            this.mProgressDialog.setCanceledOnTouchOutside(false);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            if (this.mProgressDialog != null && !this.mProgressDialog.isShowing()) {
                this.mProgressDialog.show();
            }
        }

        protected Object doInBackground(String... params) {
            try {
                getAboutMeListItem();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return Integer.valueOf(0);
        }

        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
                this.mProgressDialog.dismiss();
            }
            try {
                if (response.equalsIgnoreCase("true")) {
                    notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();

                    if (googleMap != null) {
                        polylinePath.remove();
                        googleMap.clear();
                        if (gpsTracker != null) {
                            LatLng gps = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
                            googleMap.addMarker(new MarkerOptions()
                                    .position(gps)
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.blue_pin)));
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(gps).zoom(14.0f).build()));
                        }
                    }

                    Utility.writeSharedPreferences(mContext, "data_flag", "false");
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                    ll_timer.setVisibility(View.GONE);
                    ll_interviewed.setVisibility(View.GONE);
                    ll_accept_reject.setVisibility(View.GONE);
                    img_cancel_req.setVisibility(View.GONE);
                    ll_start_service.setVisibility(View.GONE);
                    tbutton.setVisibility(View.VISIBLE);
                    ll_time_consumed.setVisibility(View.GONE);
                    btn_pause_service.setVisibility(View.GONE);
                    notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();

                    Fragment frg;
                    frg = getFragmentManager().findFragmentById(R.id.frame);
                    final FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.detach(frg);
                    ft.attach(frg);
                    ft.commit();
                } else {
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_CANCEL_INTERVIEWED;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList();

                nameValuePairs.add(new BasicNameValuePair("worker_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("service_id", service_id));

                JSONObject jObject = new JSONObject(Utility.postRequest(this.mContext, webUrl, nameValuePairs));

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }
    }

    // Rating
    public class addRating extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message;

        public addRating(final Context mContext) {
            this.mContext = mContext;
            mProgressDialog = new DME_ProgressDilog(mContext);
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mProgressDialog != null && !mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        }

        @Override
        protected Object doInBackground(String... params) {
            try {
                getAboutMeListItem();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Object result) {

            super.onPostExecute(result);

            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            try {
                if (response.equalsIgnoreCase("true")) {
                    dialog.dismiss();
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_ADD_RATING;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("service_id", service_id));
                nameValuePairs.add(new BasicNameValuePair("rating", ratingBar1.getRating() + ""));
                nameValuePairs.add(new BasicNameValuePair("comment", et_comments.getText().toString()));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);

                JSONObject jObject = new JSONObject(response1);

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class getCurrentData extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message;
        String user_id,
                notification_id,
                current_flag,
                user_name,
                user_image,
                user_latitude,
                user_longitude,
                rating,
                total_review,
                request_type,
                timer,
                service_id,
                rating1,
                total_review1, type1, mobile;

        public getCurrentData(final Context mContext) {
            this.mContext = mContext;
            mProgressDialog = new DME_ProgressDilog(mContext);
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mProgressDialog != null && !mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        }

        @Override
        protected Object doInBackground(String... params) {
            try {
                getAboutMeListItem();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            try {
                if (type1.equalsIgnoreCase("authorized")) {
                    Utility.writeSharedPreferences(mContext, "guide_dialog_flag", "0");
                    Utility.writeSharedPreferences(mContext, Constant.USER_RATING, rating1);
                    Utility.writeSharedPreferences(mContext, Constant.USER_TOTAL_REVIEW, total_review1);
                    if (response.equalsIgnoreCase("true")) {
                        Utility.writeSharedPreferences(mContext, "data_flag", "true");
                        Utility.writeSharedPreferences(mContext, "interviewed_flag", "0");
                        Utility.writeSharedPreferences(mContext, "saved_user_id", user_id);
                        Utility.writeSharedPreferences(mContext, "saved_notification_id", notification_id);
                        Utility.writeSharedPreferences(mContext, "saved_current_flag", current_flag);
                        Utility.writeSharedPreferences(mContext, "saved_user_name", user_name);
                        Utility.writeSharedPreferences(mContext, "saved_user_image", user_image);
                        Utility.writeSharedPreferences(mContext, "saved_user_latitude", user_latitude);
                        Utility.writeSharedPreferences(mContext, "saved_user_longitude", user_longitude);
                        Utility.writeSharedPreferences(mContext, "saved_rating", rating);
                        Utility.writeSharedPreferences(mContext, "saved_total_review", total_review);
                        Utility.writeSharedPreferences(mContext, "saved_request_type", request_type);
                        Utility.writeSharedPreferences(mContext, "saved_service_id", service_id);
                        Utility.writeSharedPreferences(mContext, "saved_mobile", mobile);
                        Utility.writeSharedPreferences(mContext, "from", "activity");

                        if (timer.equalsIgnoreCase("0")) {
                            Utility.writeSharedPreferences(mContext, "saved_timer", "00:00:00");
                        } else {
                            Utility.writeSharedPreferences(mContext, "saved_timer", timer);
                        }

                        if (Utility.getAppPrefString(getActivity(), "saved_current_flag").equalsIgnoreCase("accept")) {
                            ll_interviewed.setVisibility(View.VISIBLE);
                            img_cancel_req.setVisibility(View.VISIBLE);
                            ll_start_service.setVisibility(View.GONE);
                            ll_accept_reject.setVisibility(View.GONE);
                            ll_time_consumed.setVisibility(View.GONE);
                            btn_pause_service.setVisibility(View.GONE);
                            tbutton.setVisibility(View.GONE);

                            tv_inter_name.setText(Utility.getAppPrefString(getActivity(), "saved_user_name"));
                            tv_inter_total_review.setText(Utility.getAppPrefString(getActivity(), "saved_total_review"));
                            tv_inter_job_type.setText(Utility.getAppPrefString(getActivity(), "cat_name"));
                            ratingBar_inter.setRating(Float.parseFloat(Utility.getAppPrefString(getActivity(), "saved_rating")));
                            if (!Utility.getAppPrefString(getActivity(), "saved_user_image").equalsIgnoreCase("")) {
                                Glide.with(getActivity()).load(Utility.getAppPrefString(getActivity(), "saved_user_image"))
                                        .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)
                                                .error(R.mipmap.user))
                                        .thumbnail(0.5f)
                                        .into(img_profile_interviewed);
                            }

                            try {
                                Geocoder geocoder;
                                List<Address> addresses;
                                geocoder = new Geocoder(getActivity(), Locale.getDefault());
                                addresses = geocoder.getFromLocation(Double.parseDouble(user_latitude), Double.parseDouble(user_longitude), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                tv_inter_job_location.setText(address);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (!gps.isRunning()) gps.resumeGPS();
                        } else if (Utility.getAppPrefString(getActivity(), "saved_current_flag").equalsIgnoreCase("interview")) {
                            ll_start_service.setVisibility(View.VISIBLE);
                            ll_interviewed.setVisibility(View.GONE);
                            img_cancel_req.setVisibility(View.GONE);
                            ll_time_consumed.setVisibility(View.GONE);
                            btn_pause_service.setVisibility(View.GONE);
                            tbutton.setVisibility(View.GONE);
                            tv_start_name.setText(Utility.getAppPrefString(getActivity(), "saved_user_name"));
                            tv_start_total_review.setText(Utility.getAppPrefString(getActivity(), "saved_total_review"));
                            tv_start_job_type.setText(Utility.getAppPrefString(getActivity(), "cat_name"));
                            ratingBar_start.setRating(Float.parseFloat(Utility.getAppPrefString(getActivity(), "saved_rating")));
                            if (!Utility.getAppPrefString(getActivity(), "saved_user_image").equalsIgnoreCase("")) {
                                Glide.with(getActivity()).load(Utility.getAppPrefString(getActivity(), "saved_user_image"))
                                        .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)
                                                .error(R.mipmap.user))
                                        .thumbnail(0.5f)
                                        .into(img_profile_start);
                            }
                        } else if (Utility.getAppPrefString(getActivity(), "saved_current_flag").equalsIgnoreCase("start") ||
                                Utility.getAppPrefString(getActivity(), "saved_current_flag").equalsIgnoreCase("pause")) {
                            ll_start_service.setVisibility(View.VISIBLE);
                            ll_interviewed.setVisibility(View.GONE);
                            img_cancel_req.setVisibility(View.GONE);
                            ll_time_consumed.setVisibility(View.GONE);
                            btn_pause_service.setVisibility(View.GONE);
                            tbutton.setVisibility(View.GONE);
                            tv_start_name.setText(Utility.getAppPrefString(getActivity(), "saved_user_name"));
                            tv_start_total_review.setText(Utility.getAppPrefString(getActivity(), "saved_total_review"));
                            tv_start_job_type.setText(Utility.getAppPrefString(getActivity(), "cat_name"));
                            ratingBar_start.setRating(Float.parseFloat(Utility.getAppPrefString(getActivity(), "saved_rating")));
                            if (!Utility.getAppPrefString(getActivity(), "saved_user_image").equalsIgnoreCase("")) {
                                Glide.with(getActivity()).load(Utility.getAppPrefString(getActivity(), "saved_user_image"))
                                        .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)
                                                .error(R.mipmap.user))
                                        .thumbnail(0.5f)
                                        .into(img_profile_start);
                            }
                            btn_start.setText("Finish");
                            ll_time_consumed.setVisibility(View.VISIBLE);
                            btn_pause_service.setVisibility(View.VISIBLE);
                            if (Utility.getAppPrefString(getActivity(), "saved_current_flag").equalsIgnoreCase("start")) {
                                if (Utility.getAppPrefString(getActivity(), "saved_timer").equalsIgnoreCase("0") ||
                                        Utility.getAppPrefString(getActivity(), "saved_timer").equalsIgnoreCase("")) {
                                    cm_timer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                                    cm_timer.start();
                                    simpleChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                                    simpleChronometer.start();
                                } else {
                                    try {
                                        cm_timer.setBase(SystemClock.elapsedRealtime() - convert(Utility.getAppPrefString(getActivity(), "saved_timer")));
                                        cm_timer.start();
                                        simpleChronometer.setBase(SystemClock.elapsedRealtime() - convert(Utility.getAppPrefString(getActivity(), "saved_timer")));
                                        simpleChronometer.start();
                                        cm_timer.setText(Utility.getAppPrefString(getActivity(), "saved_timer"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                btn_pause_service.setText("Pause");
                                btn_start.setVisibility(View.VISIBLE);
                                pause_service_flag = false;
                            } else {
                                btn_pause_service.setText("Resume");
                                btn_start.setVisibility(View.GONE);
                                cm_timer.setText(Utility.getAppPrefString(getActivity(), "saved_timer"));
                                simpleChronometer.setText(Utility.getAppPrefString(getActivity(), "saved_timer"));
                                pause_service_flag = true;
                            }
                        }
                    } else {
                        Utility.writeSharedPreferences(mContext, "data_flag", "false");
                        Utility.writeSharedPreferences(mContext, "interviewed_flag", "0");
                        Utility.writeSharedPreferences(mContext, "from", "activity");
                        if (googleMap != null) {
                            googleMap.clear();
                            LatLng gps = new LatLng(latitude, longitude);
                            googleMap.addMarker(new MarkerOptions()
                                    .position(gps)
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.blue_pin)));

                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(gps).zoom(14.0f).build()));
                        }
                    }
                } else {
                    Utility.writeSharedPreferences(mContext, "login", "false");
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                    alertDialog.setTitle("Authentication Error");
                    alertDialog.setMessage("Authentication error occur. Please login again.");
                    alertDialog.setCancelable(false);

                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(mContext,
                                    LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });
                    alertDialog.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_GET_CURRENT_DATA;

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("worker_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("deviceID", Utility.getAppPrefString(mContext, "device_id")));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);
                JSONObject jObject = new JSONObject(response1);
                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");
                rating1 = jObject.getString("rating");
                total_review1 = jObject.getString("total_review");
                type1 = jObject.getString("type");

                if (response.equalsIgnoreCase("true")) {

                    user_id = jObject.getString("user_id");
                    notification_id = jObject.getString("notification_id");
                    current_flag = jObject.getString("current_flag");
                    user_name = jObject.getString("user_name");
                    user_image = jObject.getString("user_image");
                    user_latitude = jObject.getString("user_latitude");
                    user_longitude = jObject.getString("user_longitude");
                    rating = jObject.getString("rating");
                    total_review = jObject.getString("total_review");
                    request_type = jObject.getString("request_type");
                    timer = jObject.getString("timer");
                    mobile = jObject.getString("mobile");
                    service_id = jObject.getString("service_id");

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class count extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public count(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            isRunning = true;
            tv_time.setText(millisUntilFinished / 1000 + "");
        }

        @Override
        public void onFinish() {
            try {
                if (Utility.getAppPrefString(getActivity(), "accept_dialog_flag")
                        .equalsIgnoreCase("1")) {
                    count.cancel();
                    removeRequest();
                    isRunning = false;
                    ll_accept_reject.setVisibility(View.GONE);
                    ll_timer.setVisibility(View.GONE);
                    tbutton.setVisibility(View.VISIBLE);
                    Utility.writeSharedPreferences(getActivity(), "accept_dialog_flag", "0");

                    if (googleMap != null) {
                        polylinePath.remove();
                        googleMap.clear();
                        Location targetLocation = null;

                        if (gpsTracker.canGetLocation()) {
                            targetLocation = new Location("");//provider name is unnecessary
                            targetLocation.setLatitude(gpsTracker.getLatitude());//your coords of course
                            targetLocation.setLongitude(gpsTracker.getLongitude());
                        }

                        sendLocation sendLocation = new sendLocation(getActivity(), targetLocation);
                        sendLocation.execute();

                        LatLng gps = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
                        googleMap.addMarker(new MarkerOptions()
                                .position(gps)
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.blue_pin)));

                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(gps).zoom(14.0f).build()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}