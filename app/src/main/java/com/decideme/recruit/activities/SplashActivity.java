package com.decideme.recruit.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.decideme.recruit.R;
import com.decideme.recruit.attributes.Constant;
import com.decideme.recruit.attributes.DME_ProgressDilog;
import com.decideme.recruit.attributes.GPSTracker1;
import com.decideme.recruit.attributes.Utility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    GPSTracker1 gpsTracker1;
    Handler handler;
    double latitude;
    double longitude;
    Runnable runnable;
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private TelephonyManager telephonyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        mContext = this;

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkAndRequestPermissions()) {
                initUI();

                if (gpsTracker1.canGetLocation()) {

                    latitude = gpsTracker1.getLatitude();
                    longitude = gpsTracker1.getLongitude();

                    if (!String.valueOf(latitude).matches("0.0")) {
                        try {
                            Log.v("Lat/Long: ", latitude + "\n" + longitude);
                            Utility.writeSharedPreferences(SplashActivity.this, "latitude", latitude + "");
                            Utility.writeSharedPreferences(SplashActivity.this, "longitude", longitude + "");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            handler = new Handler();
                            runnable = new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            latitude = gpsTracker1.getLatitude();
                                            longitude = gpsTracker1.getLongitude();
                                            if (!String.valueOf(latitude).matches("0.0")) {
                                                handler.removeCallbacks(runnable);
                                                onResume();
                                            } else {
                                                handler.postDelayed(runnable, 1000);
                                                gpsTracker1 = new GPSTracker1(SplashActivity.this);
                                            }
                                        }
                                    });
                                }
                            };
                            handler.postDelayed(runnable, 1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    settingAlert();
                }
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Utility.getAppPrefString(mContext, "login").equalsIgnoreCase("true")) {
                            if (Utility.isNetworkAvaliable(mContext)) {
                                try {
                                    getCurrentData getTask = new getCurrentData(mContext);
                                    getTask.execute();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            SplashActivity.this.finish();
                            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    }
                }, 1200);
            }
        } else {
            initUI();

            if (gpsTracker1.canGetLocation()) {

                latitude = gpsTracker1.getLatitude();
                longitude = gpsTracker1.getLongitude();

                if (!String.valueOf(latitude).matches("0.0")) {

                    try {
                        Log.v("Lat/Long: ", latitude + "\n" + longitude);
                        Utility.writeSharedPreferences(SplashActivity.this, "latitude", latitude + "");
                        Utility.writeSharedPreferences(SplashActivity.this, "longitude", longitude + "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {

                    try {

                        handler = new Handler();
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        latitude = gpsTracker1.getLatitude();
                                        longitude = gpsTracker1.getLongitude();
                                        if (!String.valueOf(latitude).matches("0.0")) {
                                            handler.removeCallbacks(runnable);
                                            onResume();
                                        } else {
                                            handler.postDelayed(runnable, 1000);
                                            gpsTracker1 = new GPSTracker1(SplashActivity.this);
                                        }
                                    }
                                });

                            }
                        };
                        handler.postDelayed(runnable, 1000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                settingAlert();
            }
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Utility.getAppPrefString(mContext, "login").equalsIgnoreCase("true")) {
                        if (Utility.isNetworkAvaliable(mContext)) {
                            try {
                                getCurrentData getTask = new getCurrentData(mContext);
                                getTask.execute();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        SplashActivity.this.finish();
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }, 1200);
        }
    }

    private void initUI() {

        gpsTracker1 = new GPSTracker1(SplashActivity.this);
        Utility.writeSharedPreferences(getApplicationContext(), "accept_dialog_flag", "0");

        telephonyManager = (TelephonyManager) getSystemService(mContext.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        Utility.writeSharedPreferences(this.mContext, "device_id", this.telephonyManager.getDeviceId());
        Utility.writeSharedPreferences(getApplicationContext(), "activity_from", "false");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (gpsTracker1 != null)
            gpsTracker1.stopUsingGPS();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (gpsTracker1 != null)
            gpsTracker1.stopUsingGPS();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (gpsTracker1 != null) {
            if (gpsTracker1.canGetLocation()) {

                latitude = gpsTracker1.getLatitude();
                longitude = gpsTracker1.getLongitude();

                if (String.valueOf(latitude).matches("0.0")) {
                    try {
                        handler = new Handler();
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        latitude = gpsTracker1.getLatitude();
                                        longitude = gpsTracker1.getLongitude();
                                        Utility.writeSharedPreferences(SplashActivity.this, "latitude", latitude + "");
                                        Utility.writeSharedPreferences(SplashActivity.this, "longitude", longitude + "");
                                        Log.v("Lat/Long: ", latitude + "\n" + longitude);
                                        if (!String.valueOf(latitude).matches("0.0")) {
                                            handler.removeCallbacks(runnable);
                                            onResume();
                                        } else {
                                            handler.postDelayed(runnable, 1000);
                                            gpsTracker1 = new GPSTracker1(SplashActivity.this);
                                        }
                                    }
                                });

                            }
                        };
                        handler.postDelayed(runnable, 1000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                settingAlert();
            }
        }
    }

    public void settingAlert() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(SplashActivity.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(1 * 1000);
            locationRequest.setFastestInterval(1 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            /*if (mProgressDialog1 != null) {
                                mProgressDialog1.dismiss();
                            }*/
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                status.startResolutionForResult(SplashActivity.this, 1);
                            } catch (IntentSender.SendIntentException e) {
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            });
        }
    }

    private boolean checkAndRequestPermissions() {
        int readPhoneState = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);
        int finePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int crossPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int writePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (readPhoneState != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if (finePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (crossPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (readPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("Permission", "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                perms.put(android.Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    if (perms.get(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    ) {
                        recreate();
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                android.Manifest.permission.READ_PHONE_STATE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this,
                                        android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            Utility.showDialogOK(mContext, "Phone, Location, Storage Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class getCurrentData extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message;
        String user_id, notification_id, current_flag, user_name, user_image, user_latitude,
                user_longitude, rating, total_review, request_type, timer, service_id, rating1,
                total_review1, type, mobile;

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

            try {
                Utility.writeSharedPreferences(mContext, "guide_dialog_flag", "0");
                Utility.writeSharedPreferences(mContext, "back", "0");
                Utility.writeSharedPreferences(mContext, Constant.USER_RATING, rating1);
                Utility.writeSharedPreferences(mContext, Constant.USER_TOTAL_REVIEW, total_review1);
                Utility.writeSharedPreferences(mContext, "authentication_type", type);
                if (response.equalsIgnoreCase("true")) {
                    if (!current_flag.equalsIgnoreCase("") && !current_flag.equalsIgnoreCase("none")) {
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
                        Utility.writeSharedPreferences(getApplicationContext(), "from", "activity");

                        if (timer.equalsIgnoreCase("0")) {
                            Utility.writeSharedPreferences(mContext, "saved_timer", "00:00:00");
                        } else {
                            Utility.writeSharedPreferences(mContext, "saved_timer", timer);
                        }

                        SplashActivity.this.finish();
                        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                        startActivity(intent);
                    } else {
                        Utility.writeSharedPreferences(mContext, "data_flag", "false");
                        Utility.writeSharedPreferences(mContext, "interviewed_flag", "0");
                        Utility.writeSharedPreferences(getApplicationContext(), "from", "activity");
                        SplashActivity.this.finish();
                        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }
                } else {
                    Utility.writeSharedPreferences(mContext, "data_flag", "false");
                    Utility.writeSharedPreferences(mContext, "interviewed_flag", "0");
                    Utility.writeSharedPreferences(getApplicationContext(), "from", "activity");
                    SplashActivity.this.finish();
                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    startActivity(intent);
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
                type = jObject.getString("type");

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
}
