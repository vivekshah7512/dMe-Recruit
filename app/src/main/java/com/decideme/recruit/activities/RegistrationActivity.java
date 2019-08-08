package com.decideme.recruit.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Html;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.decideme.recruit.R;
import com.decideme.recruit.attributes.Constant;
import com.decideme.recruit.attributes.DME_ProgressDilog;
import com.decideme.recruit.attributes.PasswordValidator;
import com.decideme.recruit.attributes.Utility;
import com.google.android.gms.gcm.Task;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RegistrationActivity extends Activity implements View.OnClickListener {

    private static int PDF = 123;
    Thread backgroundCat;
    private Context mContext;
    private Button btn_sign_up;
    private RelativeLayout rl_upload_pdf;
    private Spinner sp_category;
    private TextView tv_login, tv_pdf_name, tv_terms;
    private EditText et_confirm_pass, et_email, et_mobile, et_name, et_name1, et_pass;
    private PasswordValidator passwordValidator;
    private String[] cat_id, cat_name;
    private String base64 = "", c_id, fileName = "", filePath = "", response;

    public static String getStringFile(File f) {
        FileNotFoundException e1;
        String lastVal;
        IOException e;
        String encodedFile = "";
        try {
            InputStream inputStream = new FileInputStream(f.getAbsolutePath());
            InputStream inputStream2;
            try {
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
            } catch (FileNotFoundException e2) {
                e1 = e2;
                inputStream2 = inputStream;
                e1.printStackTrace();
                lastVal = encodedFile;
                Log.d("lastv", lastVal);
                return lastVal;
            } catch (IOException e3) {
                e = e3;
                inputStream2 = inputStream;
                e.printStackTrace();
                lastVal = encodedFile;
                Log.d("lastv", lastVal);
                return lastVal;
            }
        } catch (FileNotFoundException e4) {
            e1 = e4;
            e1.printStackTrace();
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
        setContentView(R.layout.activity_registration);

        initUI();

        if (Utility.isNetworkAvaliable(mContext)) {
            try {
                backgroundCat = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jObject1 = new JSONObject(Utility.getRequest(mContext, Constant.URL_GET_CATEGORY, null));
                            response = jObject1.getString("response");
                            if (response.equals("true")) {

                                JSONArray jsonArray = jObject1.getJSONArray("cat_data");

                                int lenth = jsonArray.length();

                                cat_id = new String[lenth];
                                cat_name = new String[lenth];

                                for (int a = 0; a < lenth; a++) {
                                    JSONObject jsonObjectMessage = jsonArray.getJSONObject(a);
                                    try {
                                        cat_id[a] = jsonObjectMessage.getString("cat_id");
                                        cat_name[a] = jsonObjectMessage.getString("cat_name");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        if (response != null && !response.isEmpty()) {
                                            if (response.equals("true")) {

                                                ArrayAdapter<String> aa2 = new ArrayAdapter<String>(
                                                        mContext,
                                                        R.layout.spinner_selected_text, cat_name);
                                                sp_category.setAdapter(aa2);

                                            }
                                        }
                                    }
                                });
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                });
                backgroundCat.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }

        if (sp_category != null) {
            sp_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    c_id = cat_id[i];
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

    }

    private void initUI() {

        mContext = RegistrationActivity.this;
        passwordValidator = new PasswordValidator();

        et_name = (EditText) findViewById(R.id.et_reg_first_name);
        et_name1 = (EditText) findViewById(R.id.et_reg_last_name);
        et_email = (EditText) findViewById(R.id.et_reg_email);
        et_mobile = (EditText) findViewById(R.id.et_reg_mobile);
        et_pass = (EditText) findViewById(R.id.et_reg_pass);
        et_confirm_pass = (EditText) findViewById(R.id.et_reg_confirm_pass);
        btn_sign_up = (Button) findViewById(R.id.btn_register);
        btn_sign_up.setOnClickListener(this);
        tv_login = (TextView) findViewById(R.id.tv_register_login);
        tv_login.setOnClickListener(this);
        sp_category = (Spinner) findViewById(R.id.sp_category);
        rl_upload_pdf = (RelativeLayout) findViewById(R.id.rl_upload_pdf);
        rl_upload_pdf.setOnClickListener(this);
        tv_pdf_name = (TextView) findViewById(R.id.tv_pdf);

        tv_terms = (TextView) findViewById(R.id.tv_terms);
        tv_terms.setOnClickListener(this);

        String styledText = "<u><font color='white'>Terms & Privacy Policy</font></u>.";
        tv_terms.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_upload_pdf:
                Intent intent = new Intent();
                intent.setType("application/pdf");
                intent.setAction("android.intent.action.GET_CONTENT");
                startActivityForResult(Intent.createChooser(intent, "Select Pdf"), PDF);
                return;
            case R.id.btn_register:
                if (et_name.getText().toString().equalsIgnoreCase("") ||
                        et_name1.getText().toString().equalsIgnoreCase("") ||
                        et_email.getText().toString().equalsIgnoreCase("") ||
                        et_mobile.getText().toString().equalsIgnoreCase("") ||
                        et_pass.getText().toString().equalsIgnoreCase("") ||
                        et_confirm_pass.getText().toString().equalsIgnoreCase("")) {
                    if (et_name.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(getApplicationContext(), "Please enter your first name", Toast.LENGTH_SHORT).show();
                    } else if (et_name1.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(getApplicationContext(), "Please enter your last name", Toast.LENGTH_SHORT).show();
                    } else if (et_email.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(getApplicationContext(), "Please enter your email id", Toast.LENGTH_SHORT).show();
                    } else if (et_mobile.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(getApplicationContext(), "Please enter your mobile no.", Toast.LENGTH_SHORT).show();
                    } else if (et_pass.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(getApplicationContext(), "Please enter password", Toast.LENGTH_SHORT).show();
                    } else if (et_confirm_pass.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(getApplicationContext(), "Please enter confirm password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (Utility.isValidMobile(et_mobile.getText().toString()) == true &&
                            et_mobile.getText().toString().length() == 10) {
                        if (Utility.isValidMail(et_email.getText().toString()) == true) {
                            if (passwordValidator.validate(et_pass.getText().toString())) {
                                if (!et_pass.getText().toString().equals(et_confirm_pass.getText().toString())) {
                                    Toast.makeText(getApplicationContext(), "Your password does not match", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (Utility.isNetworkAvaliable(mContext)) {
                                        try {
                                            saveRegistrationDetails getTask = new saveRegistrationDetails(mContext);
                                            getTask.execute();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Please enter password between 8 to 15 characters", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Email ID is not valid", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Mobile number is not valid", Toast.LENGTH_SHORT).show();
                    }
                }

                break;
            case R.id.tv_register_login:
                RegistrationActivity.this.finish();
                Intent intent_login = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent_login);
                break;
            case R.id.tv_terms:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.decidemejob.com/index.php/terms-and-conditions"));
                startActivity(browserIntent);
                break;
            default:
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PDF) {
            try {
                filePath = Utility.getFilePath(this.mContext, data.getData());
                fileName = getFileName(data.getData());
                tv_pdf_name.setVisibility(View.VISIBLE);
                tv_pdf_name.setText(this.fileName);
                base64 = getStringFile(new File(this.filePath));
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    @Override
    public void onBackPressed() {
        RegistrationActivity.this.finish();
        Intent intent_login = new Intent(RegistrationActivity.this, LoginActivity.class);
        startActivity(intent_login);
    }

    public class saveRegistrationDetails extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message, user_id;

        public saveRegistrationDetails(final Context mContext) {
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
                    Utility.getAppPrefString(mContext, "check_flag").equalsIgnoreCase("false");
                    Utility.writeSharedPreferences(mContext, "authentication_type", "authorized");
                    Utility.writeSharedPreferences(mContext, "username", "");
                    Utility.writeSharedPreferences(mContext, "password", "");
                    Utility.writeSharedPreferences(mContext, "check_flag", "false");
                    Intent intent = new Intent(RegistrationActivity.this, OTPActivity.class);
                    intent.putExtra("activity_from", "reg");
                    intent.putExtra("otp_id", user_id);
                    intent.putExtra("otp_email", et_email.getText().toString().trim());
                    intent.putExtra("otp_login_type", "normal");
                    intent.putExtra("back_otp", "Registration");
                    intent.putExtra("otp_user_name", et_name.getText().toString().trim());
                    intent.putExtra("otp_mobile", et_mobile.getText().toString().trim());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_REGISTRATION;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("full_name", et_name.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("last_name", et_name1.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("email", et_email.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("mobile", et_mobile.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("lat", Utility.getAppPrefString(this.mContext, "latitude")));
                nameValuePairs.add(new BasicNameValuePair("longitude", Utility.getAppPrefString(this.mContext, "longitude")));
                nameValuePairs.add(new BasicNameValuePair("password", et_pass.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("confirm_password", et_confirm_pass.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("deviceID", Utility.getAppPrefString(this.mContext, "device_id")));
                nameValuePairs.add(new BasicNameValuePair("deviceToken", Utility.getAppPrefString(this.mContext, "device_token")));
                nameValuePairs.add(new BasicNameValuePair("device", "android"));
                nameValuePairs.add(new BasicNameValuePair("cat_id", c_id));
                nameValuePairs.add(new BasicNameValuePair("document", base64));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);
                JSONObject jObject = new JSONObject(response1);
                Log.v("response", jObject.toString() + "");
                response = jObject.getString("response");
                message = jObject.getString("message");

                if (response.equalsIgnoreCase("true")) {
                    user_id = jObject.getString("user_id");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}