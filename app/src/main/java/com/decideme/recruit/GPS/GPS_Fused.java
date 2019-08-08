package com.decideme.recruit.GPS;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.decideme.recruit.attributes.GPSTracker1;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.text.DateFormat;
import java.util.Date;

public class GPS_Fused implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener {
    private static final long FASTEST_INTERVAL = 3000;
    private static final long INTERVAL = 8000;
    private static final String TAG = "LocationActivity";
    GPSTracker1 gpsTracker1;
    Location mCurrentLocation;
    GoogleApiClient mGoogleApiClient;
    String mLastUpdateTime;
    LocationRequest mLocationRequest;
    private Context context;
    private IGPSActivity igpsActivity;
    private boolean isRunning;
    private Fragment main;

    public GPS_Fused(Fragment main, Context c) {
        this.main = main;
        context = c;
        igpsActivity = (IGPSActivity) main;
        gpsTracker1 = new GPSTracker1(context);
        if (!isGooglePlayServicesAvailable()) {
            return;
        }
        if (gpsTracker1.canGetLocation()) {
            createLocationRequest();
            mGoogleApiClient = new GoogleApiClient.Builder(context).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
            isRunning = true;
            Log.d(TAG, "onStart fired ..............");
            mGoogleApiClient.connect();
            return;
        }
        settingAlert();
    }

    public interface IGPSActivity {
        void locationChanged(double d, double d2);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(5.0f);
        mLocationRequest.setPriority(100);
    }

    public void stopGPS() {
        if (gpsTracker1.canGetLocation() && isRunning) {
            isRunning = false;
            Log.d(TAG, "onStop fired ..............");
            mGoogleApiClient.disconnect();
        }
    }

    public void resumeGPS() {
        isRunning = true;
        if (!gpsTracker1.canGetLocation()) {
            settingAlert();
        } else if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        if (!gpsTracker1.canGetLocation()) {
            settingAlert();
        } else if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    public void onConnectionSuspended(int i) {
    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    public void onLocationChanged(Location location) {
        if (gpsTracker1.canGetLocation()) {
            mCurrentLocation = location;
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
            return;
        }
        settingAlert();
    }

    private void updateUI() {
        Log.d(TAG, "UI update initiated .............");
        if (mCurrentLocation != null) {
            igpsActivity.locationChanged(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude());
        } else {
            Log.d(TAG, "location is null ...............");
        }
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener) this);
        Log.d(TAG, "Location update started ..............: ");
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (status == 0) {
            return true;
        }
        GooglePlayServicesUtil.getErrorDialog(status, (Activity) context, 0).show();
        return false;
    }

    public void settingAlert() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
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
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                status.startResolutionForResult((Activity) context, 1);
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
}