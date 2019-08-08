package com.decideme.recruit.activities;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decideme.recruit.R;
import com.decideme.recruit.attributes.Constant;
import com.decideme.recruit.attributes.DME_ProgressDilog;
import com.decideme.recruit.attributes.GPSTracker1;
import com.decideme.recruit.attributes.Utility;
import com.decideme.recruit.fragment.Fragment_Earning;
import com.decideme.recruit.fragment.Fragment_Help;
import com.decideme.recruit.fragment.Fragment_Home;
import com.decideme.recruit.fragment.Fragment_Profile;
import com.decideme.recruit.fragment.Fragment_Service_History;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.plus.Plus;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String BACK_STACK_ROOT_TAG = "main_fragment";
    public static ImageView img_profile;
    public static TextView tv_name;
    public static TextView tv_city;
    private static int PDF = 123;
    GPSTracker1 gpsTracker1;
    double latitude;
    double longitude;
    Handler handler;
    Runnable runnable;
    FrameLayout fl_fragment;
    Fragment fr = null;
    private Context mContext;
    private RatingBar ratingBar;
    private TextView tv_total_review;
    private GoogleApiClient mGoogleApiClient;
    private NavigationView navigationView;
    private String base64 = "";
    private String fileName = "";
    private String filePath = "";
    private TextView tv_pdf_name;
    private Dialog dialog;
    private String type_on_off = "on";

    public static String getStringFile(File f) {
        FileNotFoundException e1;
        String lastVal;
        IOException e;
        String encodedFile = "";
        try {
            InputStream inputStream = new FileInputStream(f.getAbsolutePath());
            InputStream inputStream2;
            byte[] buffer = new byte[Task.EXTRAS_LIMIT_BYTES];
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Base64OutputStream output64 = new Base64OutputStream(output, 0);
            while (true) {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                output64.write(buffer, 0, bytesRead);
            }
            output64.close();
            encodedFile = output.toString();
            inputStream2 = inputStream;
        } catch (FileNotFoundException e4) {
            e1 = e4;
            e1.printStackTrace();
            lastVal = encodedFile;
            Log.d("lastv", lastVal);
            return lastVal;
        } catch (IOException e5) {
            e = e5;
            e.printStackTrace();
            lastVal = encodedFile;
            Log.d("lastv", lastVal);
            return lastVal;
        }
        lastVal = encodedFile;
        Log.d("lastv", lastVal);
        return lastVal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.post(new Runnable() {
            @Override
            public void run() {
                Drawable d = ResourcesCompat.getDrawable(getResources(), R.mipmap.menu, null);
                toolbar.setNavigationIcon(d);
            }
        });
        mContext = this;
        gpsTracker1 = new GPSTracker1(HomeActivity.this);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        initUI();
    }

    private void initUI() {
        fl_fragment = (FrameLayout) findViewById(R.id.frame);

        if (Utility.getAppPrefString(mContext, "authentication_type").equalsIgnoreCase("authorized")) {
            if (Utility.getAppPrefString(mContext, "request").equalsIgnoreCase("schedule")) {
                fr = new Fragment_Service_History();
                Utility.writeSharedPreferences(mContext, "sc_back", "1");
                if (fr != null) {
                    FragmentManager fm = getFragmentManager();
                    fm.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
                    fragmentTransaction.replace(R.id.frame, fr);
                    fragmentTransaction.addToBackStack(BACK_STACK_ROOT_TAG);
                    fragmentTransaction.commit();
                }
            } else {
                Utility.writeSharedPreferences(mContext, "rule_flag", "1");
                Utility.writeSharedPreferences(mContext, "sc_back", "0");
                fr = new Fragment_Home();
                if (fr != null) {
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
                    fragmentTransaction.replace(R.id.frame, fr);
                    fragmentTransaction.commit();
                }
            }

            navigationView = (NavigationView) findViewById(R.id.nav_view);
            View headerView = navigationView.getHeaderView(0);
            img_profile = (ImageView) headerView.findViewById(R.id.img_menu_profile);
            img_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fr = new Fragment_Profile();
                    if (fr != null) {
                        FragmentManager fm = getFragmentManager();
                        fm.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        FragmentTransaction fragmentTransaction = fm.beginTransaction();
                        fragmentTransaction.replace(R.id.frame, fr);
                        fragmentTransaction.addToBackStack(BACK_STACK_ROOT_TAG);
                        fragmentTransaction.commit();
                    }
                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                }
            });

            tv_name = (TextView) headerView.findViewById(R.id.tv_menu_user_name);
            tv_name.setText(Utility.getAppPrefString(mContext, Constant.USER_NAME) + " (" +
                    Utility.getAppPrefString(mContext, "cat_name").replace(" /", ",") + ")");
            tv_city = (TextView) headerView.findViewById(R.id.tv_menu_user_city);
            tv_city.setText(Utility.getAppPrefString(mContext, Constant.USER_EMAIL));

            ratingBar = (RatingBar) headerView.findViewById(R.id.rating_worker);
            tv_total_review = (TextView) headerView.findViewById(R.id.tv_rating_worker);
            if (Utility.getAppPrefString(mContext, Constant.USER_RATING).equalsIgnoreCase("")) {
                ratingBar.setRating(0.0f);
            } else {
                ratingBar.setRating(Float.parseFloat(Utility.getAppPrefString(mContext, Constant.USER_RATING)));
            }

            LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
            stars.getDrawable(2).setColorFilter((Color.parseColor("#FE9338")),
                    PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(1).setColorFilter((Color.parseColor("#808080")),
                    PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(0).setColorFilter((Color.parseColor("#808080")),
                    PorterDuff.Mode.SRC_ATOP);

            if (Utility.getAppPrefString(mContext, Constant.USER_TOTAL_REVIEW).equalsIgnoreCase("")) {
                tv_total_review.setText("(0)");
            } else {
                tv_total_review.setText("(" + Utility.getAppPrefString(mContext, Constant.USER_TOTAL_REVIEW) + ")");
            }

            if (gpsTracker1.canGetLocation()) {
                latitude = gpsTracker1.getLatitude();
                longitude = gpsTracker1.getLongitude();
                if (!String.valueOf(latitude).matches("0.0")) {
                    try {
                        Log.v("Lat/Long: ", latitude + "\n" + longitude);
                        Utility.writeSharedPreferences(HomeActivity.this, "latitude", latitude + "");
                        Utility.writeSharedPreferences(HomeActivity.this, "longitude", longitude + "");
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
                                            gpsTracker1 = new GPSTracker1(HomeActivity.this);
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

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                    .requestEmail()
                    .build();

            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .enableAutoManage(HomeActivity.this  /* FragmentActivity */, HomeActivity.this  /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .addApi(Plus.API)
                    .build();

            navigationView.setNavigationItemSelectedListener(this);

            try {
                if (!Utility.getAppPrefString(mContext, Constant.USER_IMAGE).equalsIgnoreCase("")) {
                    Glide.with(mContext).load(Utility.getAppPrefString(mContext, Constant.USER_IMAGE))
                            .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)
                                    .error(R.mipmap.user))
                            .thumbnail(0.5f)
                            .into(img_profile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Utility.writeSharedPreferences(mContext, "login", "false");
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setTitle("Authentication Error");
            alertDialog.setMessage("Authentication error occur. Please login again.");
            alertDialog.setCancelable(false);

            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    Intent intent = new Intent(mContext,
                            LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
            alertDialog.show();
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
            if (backStackCount == 0) {
                if (Utility.getAppPrefString(mContext, "sc_back").equalsIgnoreCase("1")) {
                    Utility.writeSharedPreferences(mContext, "sc_back", "0");
                    fr = new Fragment_Home();
                    if (fr != null) {
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fm.beginTransaction();
                        fragmentTransaction.replace(R.id.frame, fr);
                        fragmentTransaction.commit();
                    }
                } else if (Utility.getAppPrefString(mContext, "back").equalsIgnoreCase("1")) {
                    Utility.writeSharedPreferences(mContext, "back", "0");
                    fr = new Fragment_Home();
                    if (fr != null) {
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fm.beginTransaction();
                        fragmentTransaction.replace(R.id.frame, fr);
                        fragmentTransaction.commit();
                    }
                } else {
                    finishAffinity();
                    System.exit(0);
                    int pid = android.os.Process.myPid();
                    android.os.Process.killProcess(pid);
                }
            } else {
                if (backStackCount >= 1) {
                    getSupportFragmentManager().popBackStack();
                    // Change to hamburger icon if at bottom of stack
                    if (backStackCount == 1) {
                        finishAffinity();
                        System.exit(0);
                        int pid = android.os.Process.myPid();
                        android.os.Process.killProcess(pid);
                    }
                } else {
                    super.onBackPressed();
                    navigationView.getMenu().getItem(0).setChecked(true);
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        gpsTracker1.stopUsingGPS();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gpsTracker1.stopUsingGPS();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to logout?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            type_on_off = "off";
                            if (Utility.isNetworkAvaliable(HomeActivity.this)) {
                                try {
                                    logoutUser getTask = new logoutUser(HomeActivity.this);
                                    getTask.execute();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else if (id == R.id.nav_upload) {

            dialog = new Dialog(mContext);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_upload_document);
            dialog.setCanceledOnTouchOutside(false);

            RelativeLayout rl_upload_pdf = (RelativeLayout) dialog.findViewById(R.id.rl_upload_pdf);
            tv_pdf_name = (TextView) dialog.findViewById(R.id.tv_pdf);

            rl_upload_pdf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setType("application/pdf");
                    intent.setAction("android.intent.action.GET_CONTENT");
                    startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF);
                }
            });

            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();

            wlp.gravity = Gravity.CENTER;
            wlp.width = ActionBar.LayoutParams.MATCH_PARENT;
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setAttributes(wlp);

            dialog.show();
        } else if (id == R.id.nav_history) {
            Utility.writeSharedPreferences(mContext, "rule_flag", "0");
            Utility.writeSharedPreferences(mContext, "back", "1");
            fr = new Fragment_Service_History();
            if (fr != null) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.frame, fr);
                fragmentTransaction.addToBackStack(BACK_STACK_ROOT_TAG);
                fragmentTransaction.commit();
            }
        } else if (id == R.id.nav_earning) {
            Utility.writeSharedPreferences(mContext, "rule_flag", "0");
            Utility.writeSharedPreferences(mContext, "back", "1");
            fr = new Fragment_Earning();
            if (fr != null) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.frame, fr);
                fragmentTransaction.addToBackStack(BACK_STACK_ROOT_TAG);
                fragmentTransaction.commit();
            }
        } else if (id == R.id.nav_home) {
            Utility.writeSharedPreferences(mContext, "rule_flag", "0");
            getFragmentManager().popBackStack();
        } else if (id == R.id.nav_help) {
            fr = new Fragment_Help();
            if (fr != null) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.frame, fr);
                fragmentTransaction.addToBackStack(BACK_STACK_ROOT_TAG);
                fragmentTransaction.commit();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Utility.getAppPrefString(mContext, "from").equalsIgnoreCase("noti")) {
            if (Utility.getAppPrefString(mContext, "request").equalsIgnoreCase("schedule")) {
                fr = new Fragment_Service_History();
                if (fr != null) {
                    FragmentManager fm = getFragmentManager();
                    fm.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
                    fragmentTransaction.replace(R.id.frame, fr);
                    fragmentTransaction.addToBackStack(BACK_STACK_ROOT_TAG);
                    fragmentTransaction.commit();
                }
            } else {
                Utility.writeSharedPreferences(mContext, "rule_flag", "1");
                fr = new Fragment_Home();
                if (fr != null) {
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
                    fragmentTransaction.replace(R.id.frame, fr);
                    fragmentTransaction.commit();
                }
            }
        }
        if (gpsTracker1.canGetLocation()) {
            latitude = gpsTracker1.getLatitude();
            longitude = gpsTracker1.getLongitude();
            if (!String.valueOf(latitude).matches("0.0")) {

                try {
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
                                    Utility.writeSharedPreferences(HomeActivity.this, "latitude", latitude + "");
                                    Utility.writeSharedPreferences(HomeActivity.this, "longitude", longitude + "");
                                    Log.v("Lat/Long: ", latitude + "\n" + longitude);
                                    if (!String.valueOf(latitude).matches("0.0")) {
                                        handler.removeCallbacks(runnable);
                                        onResume();
                                    } else {
                                        handler.postDelayed(runnable, 1000);
                                        gpsTracker1 = new GPSTracker1(HomeActivity.this);
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

    public void settingAlert() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(HomeActivity.this)
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
                                status.startResolutionForResult(HomeActivity.this, 1);
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PDF) {
            try {
                filePath = Utility.getFilePath(mContext, data.getData());
                fileName = getFileName(data.getData());
                tv_pdf_name.setVisibility(View.VISIBLE);
                tv_pdf_name.setText(this.fileName);
                base64 = getStringFile(new File(this.filePath));

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Utility.isNetworkAvaliable(mContext)) {
                            try {
                                saveDocumentDetails getTask = new saveDocumentDetails(mContext);
                                getTask.execute();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, 1000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Fragment fragment = getFragmentManager().findFragmentById(R.id.frame);
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();

            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
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

    public class saveDocumentDetails extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message;

        public saveDocumentDetails(final Context mContext) {
            this.mContext = mContext;
            mProgressDialog = new DME_ProgressDilog(mContext);
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
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
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_UPLOAD_DOC;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("worker_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("document", base64));

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

    public class logoutUser extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;

        public logoutUser(final Context mContext) {
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

            try {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                Utility.writeSharedPreferences(mContext, "login", "false");
                Utility.writeSharedPreferences(mContext, Constant.USER_ID, "");
                Utility.writeSharedPreferences(mContext, Constant.USER_NAME, "");
                Utility.writeSharedPreferences(mContext, Constant.USER_IMAGE, "");
                if (Utility.getAppPrefString(mContext, "login_type").equalsIgnoreCase("google")) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                            new ResultCallback<com.google.android.gms.common.api.Status>() {
                                @Override
                                public void onResult(com.google.android.gms.common.api.Status status) {
                                    HomeActivity.this.finish();
                                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                }
                            });

                } else if (Utility.getAppPrefString(mContext, "login_type").equalsIgnoreCase("facebook")) {
                    HomeActivity.this.finish();
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    HomeActivity.this.finish();
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_LOGOUT_USER;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);
                JSONObject jObject = new JSONObject(response1);
                Log.v("response", jObject.toString() + "");
                response = jObject.getString("response");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}