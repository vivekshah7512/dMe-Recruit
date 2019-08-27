package com.decideme.recruit.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.decideme.recruit.R;
import com.decideme.recruit.attributes.Constant;
import com.decideme.recruit.attributes.DME_ProgressDilog;
import com.decideme.recruit.attributes.RandomString;
import com.decideme.recruit.attributes.Utility;
import com.paymaya.sdk.android.PayMayaConfig;
import com.paymaya.sdk.android.checkout.PayMayaCheckout;
import com.paymaya.sdk.android.checkout.PayMayaCheckoutCallback;
import com.paymaya.sdk.android.checkout.models.Buyer;
import com.paymaya.sdk.android.checkout.models.Checkout;
import com.paymaya.sdk.android.checkout.models.Contact;
import com.paymaya.sdk.android.checkout.models.Item;
import com.paymaya.sdk.android.checkout.models.RedirectUrl;
import com.paymaya.sdk.android.checkout.models.TotalAmount;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by vivek_shah on 9/11/17.
 */
public class Fragment_Earning extends Fragment implements View.OnClickListener,
        PayMayaCheckoutCallback {

    View view;
    String cardNumber, cardCVV, cardType, cardMonth, cardYear;
    private TextView tv_earning, tv_note;
    private Button btn_settle;
    private String order_id, payment_status, base_price;
    private LinearLayout ll_settle;
    private EditText et_amount;
    private PayMayaCheckout mPayMayaCheckout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PayMayaConfig.setEnvironment(PayMayaConfig.ENVIRONMENT_PRODUCTION);
        mPayMayaCheckout = new PayMayaCheckout(Constant.PAYMAYA_PUBLIC_KEY, this);
        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        view = inflater.inflate(R.layout.fragment_earning,
                container, false);

        init();

        if (Utility.isNetworkAvaliable(getActivity())) {
            try {
                getEarning getTask = new getEarning(getActivity());
                getTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return view;
    }

    public void init() {
        tv_earning = (TextView) view.findViewById(R.id.tv_earning);
        tv_note = (TextView) view.findViewById(R.id.tv_note);
        btn_settle = (Button) view.findViewById(R.id.btn_settle);
        btn_settle.setOnClickListener(this);

        ll_settle = (LinearLayout) view.findViewById(R.id.ll_settle_amount);
        et_amount = (EditText) view.findViewById(R.id.et_money);

        Utility.writeSharedPreferences(getActivity(), "back", "1");
        Utility.writeSharedPreferences(getActivity(), "request", "");
        Utility.writeSharedPreferences(getActivity(), "from", "activity");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_settle:
                if (!et_amount.getText().toString().isEmpty()) {
                    if (Integer.parseInt(et_amount.getText().toString()) >= Integer.parseInt(base_price)) {
                        try {
                            Contact contact = new Contact("+63" + Utility.getAppPrefString(getActivity(), Constant.USER_MOBILE),
                                    Utility.getAppPrefString(getActivity(), Constant.USER_EMAIL));
                            Buyer buyer = new Buyer(Utility.getAppPrefString(getActivity(), Constant.USER_NAME), "",
                                    Utility.getAppPrefString(getActivity(), Constant.USER_NAME));
                            buyer.setContact(contact);

                            BigDecimal summaryTotal = BigDecimal.valueOf(0);
                            List itemsList = new ArrayList<>();
                            String currency = "PHP";

                            BigDecimal item1Amount = BigDecimal.valueOf(Double.parseDouble(et_amount.getText().toString()));
                            summaryTotal.add(item1Amount);
                            TotalAmount totalAmount = new TotalAmount(item1Amount, currency);
                            int quantity = 1;
                            Item item1 = new Item("DME Serv", quantity, totalAmount);
                            itemsList.add(item1);

                            String successURL = "https://www.decidemejob.com/success";
                            String failedURL = "https://www.decidemejob.com/failed";
                            String canceledURL = "https://www.decidemejob.com/canceled";

                            RedirectUrl redirectUrl = new RedirectUrl(successURL, failedURL, canceledURL);
                            RandomString gen = new RandomString(5, ThreadLocalRandom.current());
                            order_id = gen.nextString() + "_" + Utility.getTimeStamp();
                            Checkout checkout = new Checkout(totalAmount, buyer, itemsList, order_id, redirectUrl);
                            Log.v("Request : ", checkout.toString());
                            mPayMayaCheckout.execute(getActivity(), checkout);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Please enter settle amount greater than base amount",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Please enter settle amount",
                            Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckoutSuccess() {
        if (Utility.isNetworkAvaliable(getActivity())) {
            try {
                payment_status = "approved";
                getSettle getTask = new getSettle(getActivity());
                getTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCheckoutCanceled() {
        Utility.toast("Transaction canceled.", getActivity());
    }

    @Override
    public void onCheckoutFailure(String message) {
        Utility.toast("Transaction failed.", getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPayMayaCheckout.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            order_id = data.getStringExtra("checkoutId");
        }
    }

    public class getEarning extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        private String response, message, earning_amount, settle_flag, note;

        public getEarning(final Context mContext) {
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
                    note = note.replace("& ", "\n\n");
                    tv_note.setText(note);
                    et_amount.setText(base_price);
                    if (earning_amount.equalsIgnoreCase("")) {
                        tv_earning.setText("0.00");
                    } else {
                        tv_earning.setText(earning_amount);
                    }

                    if (Float.parseFloat(earning_amount) < Float.parseFloat(base_price)) {
                        ll_settle.setVisibility(View.VISIBLE);
                    } else {
                        ll_settle.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_GET_EARNING;

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(getActivity(), Constant.USER_ID)));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);
                JSONObject jObject = new JSONObject(response1);
                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

                if (response.equalsIgnoreCase("true")) {
                    earning_amount = jObject.getString("earning_amount");
                    settle_flag = jObject.getString("settle_flag");
                    note = jObject.getString("note");
                    base_price = jObject.getString("base_price");
                }

                return null;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class getSettle extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        private String response, message, earning_amount, settle_flag;

        public getSettle(final Context mContext) {
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
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    et_amount.setText(base_price);
                    if (earning_amount.equalsIgnoreCase("")) {
                        tv_earning.setText("0.00");
                    } else {
                        tv_earning.setText(earning_amount);
                    }
                    if (Float.parseFloat(earning_amount) < Float.parseFloat(base_price)) {
                        ll_settle.setVisibility(View.VISIBLE);
                    } else {
                        ll_settle.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_SETTLE;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("worker_id", Utility.getAppPrefString(getActivity(), Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("order_id", order_id));
                nameValuePairs.add(new BasicNameValuePair("settaleamount", et_amount.getText().toString()));
                nameValuePairs.add(new BasicNameValuePair("payment_status", payment_status));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);

                JSONObject jObject = new JSONObject(response1);

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

                if (response.equalsIgnoreCase("true")) {
                    earning_amount = jObject.getString("earning_amount");
                    settle_flag = jObject.getString("settle_flag");
                }

                return null;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class sendNonce extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        private String response, message;

        public sendNonce(final Context mContext) {
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
                    payment_status = "approved";
                    if (Utility.isNetworkAvaliable(getActivity())) {
                        try {
                            getSettle getTask = new getSettle(getActivity());
                            getTask.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_SEND_NONCE1;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("creditCardType", cardType));
                nameValuePairs.add(new BasicNameValuePair("creditCardNumber", cardNumber));
                nameValuePairs.add(new BasicNameValuePair("expDateMonth", cardMonth));
                nameValuePairs.add(new BasicNameValuePair("expDateYear", cardYear));
                nameValuePairs.add(new BasicNameValuePair("cvv2Number", cardCVV));
                nameValuePairs.add(new BasicNameValuePair("amount", et_amount.getText().toString()));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);
                JSONObject jObject = new JSONObject(response1);
                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");
                if (response.equalsIgnoreCase("true")) {
                    order_id = jObject.getString("order_id");
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}