package com.decideme.recruit.GCM;

/**
 * Created by vivek_shah on 23/10/17.
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.decideme.recruit.R;
import com.decideme.recruit.activities.ChatActivity;
import com.decideme.recruit.activities.HomeActivity;
import com.decideme.recruit.attributes.Utility;
import com.decideme.recruit.fragment.Fragment_Home;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    String json;
    JSONObject jobject;
    private int count = 0;
    private boolean isLogout = false;
    private String message;
    private String title, notification_type, service_id;
    private String user_pic, user_mobile, notification_id, user_longitude, job_location, user_rating, user_latitude, user_id, type,
            user_name, total_review, user_image, user_category, user_total_review, date, time, request_type, date_time;

    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "Received message " + remoteMessage.getData());
        Map data = remoteMessage.getData();

        try {
            json = remoteMessage.getData().get("data");
            jobject = new JSONObject(json);
            title = (String) data.get("title");
            message = (String) data.get("message");
            notification_type = jobject.getString("notification_type");
            if (jobject.has("request_type"))
                request_type = jobject.getString("request_type");
            if (notification_type.equalsIgnoreCase("cancelRequest")) {
                title = (String) data.get("message");
                message = jobject.getString("message");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (Utility.getAppPrefString(getApplicationContext(), "login").equalsIgnoreCase("true")) {

            if (notification_type != null && notification_type.equalsIgnoreCase("Request")) {
                if (Utility.getAppPrefString(getApplicationContext(), "switch_mode") == null ||
                        Utility.getAppPrefString(getApplicationContext(), "switch_mode").equalsIgnoreCase("") ||
                        Utility.getAppPrefString(getApplicationContext(), "switch_mode").equalsIgnoreCase("on")) {
                    if (request_type != null && request_type.equalsIgnoreCase("ACCEPT_SCHDULE")) {
                        try {
                            user_id = jobject.getString("user_id");
                            user_name = jobject.getString("user_name");
                            user_pic = jobject.getString("user_pic");
                            notification_id = jobject.getString("notification_id");
                            user_rating = jobject.getString("user_rating");
                            user_total_review = jobject.getString("total_review");
                            user_latitude = jobject.getString("user_latitude");
                            user_longitude = jobject.getString("user_longitude");
                            type = jobject.getString("type");
                            service_id = jobject.getString("service_id");

                            Utility.writeSharedPreferences(getApplicationContext(), "request", "schedule");
                            Utility.writeSharedPreferences(getApplicationContext(), "accept_dialog_flag", "0");
                            Utility.writeSharedPreferences(getApplicationContext(), "data_flag", "false");
                            Utility.writeSharedPreferences(getApplicationContext(), "from", "noti");

                            createNotification("You have new schedule request arrived", "Schedule Request", notification_type);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (request_type != null && request_type.equalsIgnoreCase("ACCEPT_ONDEMAND") &&
                            !Utility.getAppPrefString(getApplicationContext(), "overlap_req").equalsIgnoreCase("1")) {
                        try {
                            user_pic = jobject.getString("user_pic");
                            user_mobile = jobject.getString("user_mobile");
                            notification_id = jobject.getString("notification_id");
                            user_longitude = jobject.getString("user_longitude");
                            user_rating = jobject.getString("user_rating");
                            user_latitude = jobject.getString("user_latitude");
                            user_name = jobject.getString("user_name");
                            user_id = jobject.getString("user_id");
                            type = jobject.getString("type");
                            date = jobject.getString("date");
                            time = jobject.getString("time");
                            service_id = jobject.getString("service_id");

                            Utility.writeSharedPreferences(getApplicationContext(), "request", "schedule_normal_req");
                            Utility.writeSharedPreferences(getApplicationContext(), "accept_dialog_flag", "1");
                            Utility.writeSharedPreferences(getApplicationContext(), "data_flag", "false");
                            Utility.writeSharedPreferences(getApplicationContext(), "from", "noti");

                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                            Utility.writeSharedPreferences(this, "noti_user_pic", user_pic);
                            Utility.writeSharedPreferences(this, "noti_user_mobile", user_mobile);
                            Utility.writeSharedPreferences(this, "noti_notification_id", notification_id);
                            Utility.writeSharedPreferences(this, "noti_user_longitude", user_longitude);
                            Utility.writeSharedPreferences(this, "noti_user_latitude", user_latitude);
                            Utility.writeSharedPreferences(this, "noti_user_rating", user_rating);
                            Utility.writeSharedPreferences(this, "noti_user_name", user_name);
                            Utility.writeSharedPreferences(this, "noti_user_id", user_id);
                            Utility.writeSharedPreferences(this, "noti_type", "schedule_normal_req");
                            Utility.writeSharedPreferences(this, "noti_date", date);
                            Utility.writeSharedPreferences(this, "noti_time", time);
                            Utility.writeSharedPreferences(this, "noti_service_id", service_id);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            user_pic = jobject.getString("user_pic");
                            user_mobile = jobject.getString("user_mobile");
                            notification_id = jobject.getString("notification_id");
                            user_longitude = jobject.getString("user_longitude");
                            user_rating = jobject.getString("user_rating");
                            user_latitude = jobject.getString("user_latitude");
                            user_name = jobject.getString("user_name");
//                        total_review = jobject.getString("user_total_review");
                            user_id = jobject.getString("user_id");
                            type = jobject.getString("type");
                            date = jobject.getString("date");
                            time = jobject.getString("time");
                            service_id = jobject.getString("service_id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Utility.writeSharedPreferences(getApplicationContext(), "request", "normal");
                        Utility.writeSharedPreferences(getApplicationContext(), "accept_dialog_flag", "1");
                        Utility.writeSharedPreferences(getApplicationContext(), "data_flag", "false");
                        Utility.writeSharedPreferences(getApplicationContext(), "from", "noti");

                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        Utility.writeSharedPreferences(this, "noti_user_pic", user_pic);
                        Utility.writeSharedPreferences(this, "noti_user_mobile", user_mobile);
                        Utility.writeSharedPreferences(this, "noti_notification_id", notification_id);
                        Utility.writeSharedPreferences(this, "noti_user_longitude", user_longitude);
                        Utility.writeSharedPreferences(this, "noti_user_latitude", user_latitude);
                        Utility.writeSharedPreferences(this, "noti_user_rating", user_rating);
                        Utility.writeSharedPreferences(this, "noti_user_name", user_name);
                        Utility.writeSharedPreferences(this, "noti_user_id", user_id);
                        Utility.writeSharedPreferences(this, "noti_type", type);
                        Utility.writeSharedPreferences(this, "noti_date", date);
                        Utility.writeSharedPreferences(this, "noti_time", time);
                        Utility.writeSharedPreferences(this, "noti_service_id", service_id);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                } else {
                    Log.v("Notification", "OFF");
                }
            } else if (notification_type != null && notification_type.equalsIgnoreCase("CRON_SCHEDUAL")) {
                if (Utility.getAppPrefString(getApplicationContext(), "switch_mode") == null ||
                        Utility.getAppPrefString(getApplicationContext(), "switch_mode").equalsIgnoreCase("") ||
                        Utility.getAppPrefString(getApplicationContext(), "switch_mode").equalsIgnoreCase("on")) {
                    try {
                        user_pic = jobject.getString("user_pic");
                        user_mobile = jobject.getString("user_mobile");
                        notification_id = jobject.getString("notification_id");
                        user_longitude = jobject.getString("user_longitude");
                        user_rating = jobject.getString("user_rating");
                        user_latitude = jobject.getString("user_latitude");
                        user_name = jobject.getString("user_name");
                        date_time = jobject.getString("date_time");
//                        total_review = jobject.getString("user_total_review");
                        user_id = jobject.getString("user_id");
                        type = jobject.getString("type");
                        service_id = jobject.getString("service_id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Utility.writeSharedPreferences(getApplicationContext(), "accept_dialog_flag", "1");
                    Utility.writeSharedPreferences(getApplicationContext(), "request", "cron");
                    Utility.writeSharedPreferences(getApplicationContext(), "data_flag", "false");
                    Utility.writeSharedPreferences(getApplicationContext(), "from", "noti");

                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    Utility.writeSharedPreferences(this, "noti_user_pic", user_pic);
                    Utility.writeSharedPreferences(this, "noti_user_mobile", user_mobile);
                    Utility.writeSharedPreferences(this, "noti_notification_id", notification_id);
                    Utility.writeSharedPreferences(this, "noti_user_longitude", user_longitude);
                    Utility.writeSharedPreferences(this, "noti_user_latitude", user_latitude);
                    Utility.writeSharedPreferences(this, "noti_user_rating", user_rating);
                    Utility.writeSharedPreferences(this, "noti_user_name", user_name);
//                    Utility.writeSharedPreferences(this, "noti_total_review", total_review);
                    Utility.writeSharedPreferences(this, "noti_date_time", date_time);
                    Utility.writeSharedPreferences(this, "noti_user_id", user_id);
                    Utility.writeSharedPreferences(this, "noti_type", "schedule1");
                    Utility.writeSharedPreferences(this, "noti_service_id", service_id);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Log.v("Notification", "OFF");
                }
            } else if (notification_type != null && notification_type.equalsIgnoreCase("chat")) {
                if (ChatActivity.active) {
                    Utility.writeSharedPreferences(this, "chat_from", "notification");
                    Utility.writeSharedPreferences(getApplicationContext(), "data_flag", "false");
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("chatFrom", "notification");
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    try {
                        user_id = jobject.getString("user_id");
                        user_image = jobject.getString("user_image");
                        user_name = jobject.getString("user_name");
                        user_mobile = jobject.getString("user_mobile");
                        message = jobject.getString("message");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    createNotification(message, "dMe Serv", notification_type);
                }
            } else if (notification_type != null && notification_type.equalsIgnoreCase("cancelinterview")) {
                createNotification(message, "Job Cancelled", notification_type);
            } else if (notification_type != null && notification_type.equalsIgnoreCase("")) {
                try {
                    title = jobject.getString("title");
                    message = jobject.getString("message");
                    createNotification(message, title, "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (notification_type != null && notification_type.equalsIgnoreCase("cancelRequest")) {
                if (Utility.getAppPrefString(getApplicationContext(), "switch_mode") == null ||
                        Utility.getAppPrefString(getApplicationContext(), "switch_mode").equalsIgnoreCase("") ||
                        Utility.getAppPrefString(getApplicationContext(), "switch_mode").equalsIgnoreCase("on")) {
                    createNotification(message, "Request Cancelled", notification_type);
                } else {
                    Log.v("Notification", "OFF");
                }
            } else if (notification_type != null && notification_type.equalsIgnoreCase("payment")) {
                try {
                    user_id = jobject.getString("user_id");
                    user_name = jobject.getString("user_name");
                    user_rating = jobject.getString("user_rate");
                    user_pic = jobject.getString("user_profile");
                    user_total_review = jobject.getString("user_total_review");
                    service_id = jobject.getString("service_id");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Utility.writeSharedPreferences(getApplicationContext(), "p_user_id", user_id);
                Utility.writeSharedPreferences(getApplicationContext(), "p_user_name", user_name);
                Utility.writeSharedPreferences(getApplicationContext(), "p_user_rate", user_rating);
                Utility.writeSharedPreferences(getApplicationContext(), "p_user_profile", user_pic);
                Utility.writeSharedPreferences(getApplicationContext(), "p_user_total_review", user_total_review);
                Utility.writeSharedPreferences(getApplicationContext(), "p_service_id", service_id);
                createNotification(message, "Payment Received", notification_type);
            }
        } else {
            Log.v("Notification: ", "Logout");
        }
    }

    private void createNotification(String message, String nTitle, String type) {

        Intent intent = null;
        String channelId = "1057887";
        String channelName = "dMe Serv";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (type.equalsIgnoreCase("chat")) {
            Utility.writeSharedPreferences(this, "chat_from", "notification");
            intent = new Intent(this, ChatActivity.class);
            intent.putExtra("worker_id", user_id);
            intent.putExtra("user_image", user_image);
            intent.putExtra("user_name", user_name);
            intent.putExtra("user_mobile", user_mobile);
            intent.putExtra("chatFrom", "notification");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else if (type.equalsIgnoreCase("cancelinterview")) {
            Utility.writeSharedPreferences(getApplicationContext(), "accept_dialog_flag", "0");
            Utility.writeSharedPreferences(getApplicationContext(), "activity_from", "cancel");
            if (Fragment_Home.active) {
                intent = new Intent("unique_name");
                getApplicationContext().sendBroadcast(intent);
            } else {
                intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        } else if (type.equalsIgnoreCase("cancelRequest")) {
            Utility.writeSharedPreferences(getApplicationContext(), "accept_dialog_flag", "0");
            Utility.writeSharedPreferences(getApplicationContext(), "data_flag", "false");
            Utility.writeSharedPreferences(getApplicationContext(), "activity_from", "cancel_request");
            if (Fragment_Home.active) {
                intent = new Intent("unique_name");
                getApplicationContext().sendBroadcast(intent);
            } else {
                intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        } else if (type.equalsIgnoreCase("payment")) {
            if (Fragment_Home.active) {
                intent = new Intent("unique_name");
                Utility.writeSharedPreferences(getApplicationContext(), "activity_from", "payment");
                getApplicationContext().sendBroadcast(intent);
            } else {
                intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        } else if (type.equalsIgnoreCase("Request")) {
            if (request_type.equalsIgnoreCase("ACCEPT_SCHDULE")) {
                intent = new Intent(this, HomeActivity.class);
                intent.putExtra("user_id", user_id);
                intent.putExtra("user_name", user_name);
                intent.putExtra("user_pic", user_pic);
                intent.putExtra("user_rating", user_rating);
                intent.putExtra("user_total_review", user_total_review);
                intent.putExtra("user_latitude", user_latitude);
                intent.putExtra("user_longitude", user_longitude);
                intent.putExtra("type", type);
                intent.putExtra("notification_id", notification_id);
                intent.putExtra("title", "Schedule Request");
                intent.putExtra("message", "New schedule request arrived");
                intent.putExtra("service_id", service_id);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } else {
            intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            nm.createNotificationChannel(mChannel);
        }

        Notification notification = new NotificationCompat.Builder(this)
                .setColor(ContextCompat.getColor(this, R.color.sky_blue))
                .setCategory(Notification.CATEGORY_PROMO)
                .setContentTitle(nTitle)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(nTitle).bigText(message))
                .setSmallIcon(R.mipmap.noti_small)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.app_icon))
                .setAutoCancel(true)
                .setVisibility(View.VISIBLE)
                .setContentIntent(contentIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_SOUND).build();
        nm.notify(new Random().nextInt(), notification);
    }
}
