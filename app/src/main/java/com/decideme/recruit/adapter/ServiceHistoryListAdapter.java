package com.decideme.recruit.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decideme.recruit.R;
import com.decideme.recruit.attributes.Constant;
import com.decideme.recruit.attributes.DME_ProgressDilog;
import com.decideme.recruit.attributes.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vivek.shah on 16-Jan-16.
 */
public class ServiceHistoryListAdapter extends BaseAdapter {

    Context context;
    private LayoutInflater inflater = null;
    private String type = "";
    private String[] transaction_id, worker_name, worker_image, worker_cat, status, worker_payment,
            payment_mode, payment_status, date_time, worker_address, user_address, rate, rating,
            service_id, lat, longitude;

    private RefreshList listener;

    public ServiceHistoryListAdapter(Activity context, String[] transaction_id,
                                     String[] worker_name, String[] worker_image, String[] worker_cat,
                                     String[] status, String[] worker_payment, String[] payment_mode,
                                     String[] payment_status, String[] date_time, String[] worker_address,
                                     String[] user_address, String[] rate, String[] rating, String[] service_id,
                                     String[] lat, String[] longitude, RefreshList listener) {
        this.context = context;
        this.transaction_id = transaction_id;
        this.worker_name = worker_name;
        this.worker_image = worker_image;
        this.worker_cat = worker_cat;
        this.status = status;
        this.worker_payment = worker_payment;
        this.payment_mode = payment_mode;
        this.payment_status = payment_status;
        this.date_time = date_time;
        this.worker_address = worker_address;
        this.user_address = user_address;
        this.rate = rate;
        this.rating = rating;
        this.service_id = service_id;
        this.lat = lat;
        this.longitude = longitude;
        this.listener= listener;

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return worker_name.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return worker_name[position];
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder {

        ImageView img_job_type, img_profile, img_accept, img_reject;
        TextView tv_name, tv_job_type, tv_total_rate, tv_date_time, tv_total_hours, tv_source_address,
                tv_destination_address, tv_total;
        RatingBar ratingBar;
        LinearLayout ll_total_time;
        LinearLayout ll_accept_reject;

    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        // TODO Auto-generated method stub

        final Holder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_service_history_list_item, null);
            holder = new Holder();

            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_history_worker_name);
            holder.tv_job_type = (TextView) convertView.findViewById(R.id.tv_history_worker_cat);
            holder.tv_total_rate = (TextView) convertView.findViewById(R.id.tv_history_rating_total);
            holder.tv_date_time = (TextView) convertView.findViewById(R.id.tv_history_date_time);
            holder.tv_total_hours = (TextView) convertView.findViewById(R.id.tv_history_total_hours);
            holder.tv_source_address = (TextView) convertView.findViewById(R.id.tv_history_source);
            holder.tv_destination_address = (TextView) convertView.findViewById(R.id.tv_history_destination);
            holder.tv_total = (TextView) convertView.findViewById(R.id.tv_history_total);
            holder.ll_total_time = (LinearLayout) convertView.findViewById(R.id.ll_total_time);
            holder.ll_accept_reject = (LinearLayout) convertView.findViewById(R.id.ll_accept_reject);

            holder.img_job_type = (ImageView) convertView.findViewById(R.id.img_history_type);
            holder.img_profile = (ImageView) convertView.findViewById(R.id.img_history_profile);
            holder.img_accept = (ImageView) convertView.findViewById(R.id.img_accept);
            holder.img_reject = (ImageView) convertView.findViewById(R.id.img_reject);

            holder.ratingBar = (RatingBar) convertView.findViewById(R.id.rating_history);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        if (status[position].equalsIgnoreCase("request")) {
            holder.ll_accept_reject.setVisibility(View.VISIBLE);
        } else {
            holder.ll_accept_reject.setVisibility(View.GONE);
        }

        holder.tv_name.setText(worker_name[position]);
        holder.tv_job_type.setText(worker_cat[position]);
        holder.tv_total_rate.setText("(" + rate[position] + ")");
        holder.tv_date_time.setText(date_time[position]);
        holder.tv_total_hours.setText("");

        // Source location
        if (user_address[position].equalsIgnoreCase("") ||
                user_address[position].equalsIgnoreCase("Not found"))
            holder.tv_source_address.setText("Location not found");
        else
            holder.tv_source_address.setText(user_address[position]);

        // Destination location
        if (worker_address[position].equalsIgnoreCase("") ||
                worker_address[position].equalsIgnoreCase("Not found"))
            holder.tv_destination_address.setText("Location not found");
        else
            holder.tv_destination_address.setText(worker_address[position]);

        holder.tv_total.setText("");
        holder.ratingBar.setRating(Float.parseFloat(rating[position]));
        holder.ll_total_time.setVisibility(View.GONE);

        Glide.with(context).load(worker_image[position])
                .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user)
                        .error(R.mipmap.user))
                .thumbnail(0.5f)
                .into(holder.img_profile);

        holder.img_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "accept";
                if (Utility.isNetworkAvaliable(context)) {
                    try {
                        acceptRejectRequest getTask = new acceptRejectRequest(context);
                        getTask.execute(service_id[position], lat[position], longitude[position]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        holder.img_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "cancel";
                if (Utility.isNetworkAvaliable(context)) {
                    try {
                        acceptRejectRequest getTask = new acceptRejectRequest(context);
                        getTask.execute(service_id[position], lat[position], longitude[position]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return convertView;
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
                getAboutMeListItem(params[0], params[1], params[2]);
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
                    listener.refresh();
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Object getAboutMeListItem(String s_id, String lat, String lang) {
            String webUrl = Constant.URL_ACCEPT_REJECT;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("type", type));
                nameValuePairs.add(new BasicNameValuePair("service_id", s_id));
                nameValuePairs.add(new BasicNameValuePair("service_type", ""));
                nameValuePairs.add(new BasicNameValuePair("worker_latitude", lat + ""));
                nameValuePairs.add(new BasicNameValuePair("worker_longitude", lang + ""));

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

    public interface RefreshList {
        public void refresh();
    }
}
