package com.decideme.recruit.activities;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.decideme.recruit.R;
import com.decideme.recruit.attributes.Constant;
import com.decideme.recruit.attributes.DME_ProgressDilog;
import com.decideme.recruit.attributes.Utility;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private EditText et_username, et_password;
    private Button btn_login;
    private TextView tv_forgot_pass, tv_register;
    private Dialog dialog;
    private EditText et_email;
    private CheckBox cb_remember;
    private String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);

        initUI();

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(LoginActivity.this,
                new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        String newToken = instanceIdResult.getToken();
                        Log.e("newToken", newToken);
                        Utility.writeSharedPreferences(mContext, "device_token", newToken);
                    }
                });

    }

    public void initUI() {
        mContext = LoginActivity.this;

        cb_remember = (CheckBox) findViewById(R.id.check_remember);
        et_username = (EditText) findViewById(R.id.et_login_username);
        et_password = (EditText) findViewById(R.id.et_login_password);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);
        tv_forgot_pass = (TextView) findViewById(R.id.tv_forgot_password);
        tv_forgot_pass.setOnClickListener(this);
        tv_register = (TextView) findViewById(R.id.tv_login_register);
        tv_register.setOnClickListener(this);

        tv_register.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        if (Utility.getAppPrefString(mContext, "check_flag").equalsIgnoreCase("true")) {
            et_username.setText(Utility.getAppPrefString(mContext, "username"));
            et_password.setText(Utility.getAppPrefString(mContext, "password"));
            cb_remember.setChecked(true);
        } else {
            cb_remember.setChecked(false);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                username = et_username.getText().toString();
                password = et_password.getText().toString();

                if (et_username.getText().toString().equalsIgnoreCase("") || et_password.getText().toString().equalsIgnoreCase("")) {
                    if (et_username.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(mContext, "Please enter your email id", Toast.LENGTH_SHORT).show();
                    } else if (et_password.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(mContext, "Please enter your password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (cb_remember.isChecked()) {
                        Utility.writeSharedPreferences(mContext, "username", username);
                        Utility.writeSharedPreferences(mContext, "password", password);
                        Utility.writeSharedPreferences(mContext, "check_flag", "true");
                    } else {
                        Utility.writeSharedPreferences(mContext, "username", "");
                        Utility.writeSharedPreferences(mContext, "password", "");
                        Utility.writeSharedPreferences(mContext, "check_flag", "false");
                    }
                    Utility.writeSharedPreferences(mContext, "login_type", "normal");
                    if (Utility.isNetworkAvaliable(mContext)) {
                        try {
                            createLogin getTask = new createLogin(mContext);
                            getTask.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case R.id.tv_forgot_password:
                dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(Color.TRANSPARENT));
                dialog.setContentView(R.layout.dialog_forgot_password);

                Button btn_send = (Button) dialog.findViewById(R.id.btn_forgot_send);
                ImageView img_close = (ImageView) dialog.findViewById(R.id.img_forgot_close);
                et_email = (EditText) dialog.findViewById(R.id.et_forgot_email);

                img_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                btn_send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!et_email.getText().toString().equalsIgnoreCase("")) {
                            if (Utility.isValidMail(et_email.getText().toString()) == true) {

                                if (Utility.isNetworkAvaliable(mContext)) {
                                    try {
                                        sendForgotPasswordLink getTask = new sendForgotPasswordLink(mContext);
                                        getTask.execute();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Email id is not valid", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(mContext, "Please enter email id", Toast.LENGTH_SHORT).show();
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
                break;
            case R.id.tv_login_register:
                finish();
                Intent intent_register = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent_register);
                break;
            default:
                break;
        }
    }

    public class sendForgotPasswordLink extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message;

        public sendForgotPasswordLink(final Context mContext) {
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
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_FORGOT_PASSWORD;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("email", et_email.getText().toString().trim()));

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

    public class createLogin extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response, message;
        String userid, username, userprofile, email, mobile, cat_name, worker_rating, worker_total_review;

        public createLogin(final Context mContext) {
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

                    Utility.writeSharedPreferences(mContext, "login_type", "normal");
                    Utility.writeSharedPreferences(mContext, Constant.USER_ID, userid);
                    Utility.writeSharedPreferences(mContext, "login", "true");
                    Utility.writeSharedPreferences(mContext, Constant.USER_NAME, username);
                    Utility.writeSharedPreferences(mContext, Constant.USER_IMAGE, userprofile);
                    Utility.writeSharedPreferences(mContext, Constant.USER_EMAIL, email);
                    Utility.writeSharedPreferences(mContext, Constant.USER_MOBILE, mobile);
                    Utility.writeSharedPreferences(mContext, Constant.USER_RATING, worker_rating);
                    Utility.writeSharedPreferences(mContext, Constant.USER_TOTAL_REVIEW, worker_total_review);
                    Utility.writeSharedPreferences(mContext, "cat_name", cat_name);
                    Utility.writeSharedPreferences(mContext, "switch_mode", "on");
                    if (Utility.isNetworkAvaliable(mContext)) {
                        try {
                            getCurrentData getTask = new getCurrentData(mContext);
                            getTask.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_LOGIN;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("username", et_username.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("password", et_password.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("fguid", ""));
                nameValuePairs.add(new BasicNameValuePair("full_name", ""));
                nameValuePairs.add(new BasicNameValuePair("userprofile", ""));
                nameValuePairs.add(new BasicNameValuePair("email", ""));
                nameValuePairs.add(new BasicNameValuePair("type", "normal"));
                nameValuePairs.add(new BasicNameValuePair("deviceID", Utility.getAppPrefString(mContext, "device_id")));
                nameValuePairs.add(new BasicNameValuePair("deviceToken", Utility.getAppPrefString(mContext, "device_token")));
                nameValuePairs.add(new BasicNameValuePair("device", "android"));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);
                JSONObject jObject = new JSONObject(response1);
                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

                if (response.equalsIgnoreCase("true")) {
                    userid = jObject.getString("user_id");
                    username = jObject.getString("username");
                    userprofile = jObject.getString("userprofile");
                    email = jObject.getString("email");
                    mobile = jObject.getString("mobile");
                    cat_name = jObject.getString("category");
                    worker_rating = jObject.getString("worker_rating");
                    worker_total_review = jObject.getString("worker_total_review");
                }
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
                rating1,
                total_review1,
                type,
                mobile;

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
                Utility.writeSharedPreferences(mContext, "authentication_type", type);
                if (type.equalsIgnoreCase("authorized")) {
                    Utility.writeSharedPreferences(mContext, Constant.USER_RATING, rating1);
                    Utility.writeSharedPreferences(mContext, Constant.USER_TOTAL_REVIEW, total_review1);
                    if (response.equalsIgnoreCase("true")) {
                        if (!current_flag.equalsIgnoreCase("") && !current_flag.equalsIgnoreCase("none")) {
                            Utility.writeSharedPreferences(mContext, "data_flag", "true");
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
                            Utility.writeSharedPreferences(mContext, "saved_timer", timer);
                            Utility.writeSharedPreferences(mContext, "saved_mobile", mobile);

                            finish();
                            Intent intent_login = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent_login);
                        } else {
                            Utility.writeSharedPreferences(mContext, "data_flag", "false");
                            finish();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        Utility.writeSharedPreferences(mContext, "data_flag", "false");
                        finish();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
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

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}