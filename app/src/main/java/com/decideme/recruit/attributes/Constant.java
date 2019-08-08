package com.decideme.recruit.attributes;

public class Constant {

    public static String font_bold = "fonts/Roboto-Bold.ttf";
    public static String font_light = "fonts/Roboto-Light.ttf";
    public static String font_regular = "fonts/Roboto-Regular.ttf";

    public static String PREFS_NAME = "dme_recruit_pref";
    public static String TAG = "DME_RECRUIT";

    public static int MY_SOCKET_TIMEOUT_MS = 10000;

    public static String USER_ID = "user_id";
    public static String USER_NAME = "user_name";
    public static String USER_IMAGE = "user_image";
    public static String USER_EMAIL = "user_email";
    public static String USER_MOBILE = "user_mobile";
    public static String USER_RATING = "user_rating";
    public static String USER_TOTAL_REVIEW = "user_total_review";

    //    public static String PAYMAYA_SECRET_KEY = "sk-DiZJiYisTICvq87nA1Whp6yflS0tcmajCokh3tErNMX"; // Sandbox
    public static String PAYMAYA_SECRET_KEY = "sk-IUPCxncassgLByEWINh6hmuozBKm60sBtkNkyHIqAmC"; // Production
    //    public static String PAYMAYA_PUBLIC_KEY = "pk-ag4C50lxTcfXBc4XC7Pe6LHJGWzN2JXkgTnzG6xsKf1"; // Sandbox
    public static String PAYMAYA_PUBLIC_KEY = "pk-QBtF42MPyTeM4wT83JyadOStko9MIyNYTt3EwTYnhyV"; // Production

    // For Services Parent URL
//    public static String URL_PARENT = "http://dme.twinkas.club/api/recruit/";
    public static String URL_PARENT = "https://www.decidemejob.com/API/api/recruit/";
    public static String URL_PARENT1 = "https://www.decidemejob.com/API/api/client/";

//    public static String URL_PARENT = "http://13.251.218.144/API/api/recruit/";
//    public static String URL_PARENT1 = "http://13.251.218.144/API/api/client/";

    // For All Services URL
    public static String URL_LOGIN = URL_PARENT + "login";
        public static String URL_REGISTRATION = URL_PARENT + "create";
//    public static String URL_REGISTRATION = URL_PARENT + "new_create";
    public static String URL_FORGOT_PASSWORD = URL_PARENT + "forgot";
    public static String URL_GET_RULES = URL_PARENT + "guiderules";
    public static String URL_GET_CATEGORY = URL_PARENT + "getCategory";
    public static String URL_ACCEPT_REJECT = URL_PARENT + "acceptrejectreq";
    public static String URL_CHAT_HISTORY = URL_PARENT + "getchatlist";
    public static String URL_SEND_MESSAGE = URL_PARENT + "sendmessage";
    public static String URL_LOCATION_UPDATE = URL_PARENT + "locationupdate";
    public static String URL_START_SERVICES = URL_PARENT + "getstartservice";
    public static String URL_FINISH_SERVICES = URL_PARENT + "getfinishservice";
    public static String URL_ON_OFF = URL_PARENT + "onoffreq";
    public static String URL_GET_CURRENT_DATA = URL_PARENT + "getcurrdata";
    public static String URL_INTERVIEWED_SERVICE = URL_PARENT + "getinterviewser";
    public static String URL_PAUSE_SERVICE = URL_PARENT + "saveservicetime";
    public static String URL_CANCEL_INTERVIEWED = URL_PARENT + "cancelinterview";
    public static String URL_UPLOAD_DOC = URL_PARENT + "upload_doc";
    public static String URL_GET_HISTORY_LIST = URL_PARENT + "reqserhistory";
    public static String URL_GET_EARNING = URL_PARENT + "getearning";
    public static String URL_SETTLE = URL_PARENT + "AddRecPayment";
    public static String URL_60_SEC_COMPLETED = URL_PARENT + "removeUserRequest";
    public static String URL_LOGOUT_USER = URL_PARENT1 + "userLogout";
    public static String URL_SEND_NONCE = URL_PARENT1 + "brainTreePayment";
    public static String URL_SEND_NONCE1 = URL_PARENT1 + "paypalPayment";

    public static String URL_GET_PROFILE = URL_PARENT + "getProfile";
    public static String URL_SAVE_PROFILE = URL_PARENT + "saveProfile";
    public static String URL_SAVE_EDIT_PROFILE = URL_PARENT + "saveEditProfile";
    public static String URL_ADD_RATING = URL_PARENT + "addRating";

    public static String URL_GET_OTP = URL_PARENT + "getOTPRec";
    public static String URL_VERIFY_OTP = URL_PARENT + "verifyOTPRec";
    public static String URL_RESEND_OTP = URL_PARENT + "resendOTPEditProfile";

}
