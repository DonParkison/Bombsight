package com.bombbomb.bombsight;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by cos-mbp-don on 3/21/17.
 */

public class PermissionsUtil {

    public enum PERMISSIONS {

        CALENDER (Manifest.permission.READ_CALENDAR, 1000),
        CAMERA (Manifest.permission.CAMERA, 2000),
        CONTACTS (Manifest.permission.READ_CONTACTS, 3000),
        LOCATION (Manifest.permission.ACCESS_FINE_LOCATION, 4000),
        MICROPHONE (Manifest.permission.RECORD_AUDIO, 5000),
        PHONE (Manifest.permission.READ_PHONE_STATE, 6000),
        SENSORS (Manifest.permission.BODY_SENSORS, 7000),
        SMS (Manifest.permission.SEND_SMS, 8000),
        STORAGE (Manifest.permission.READ_EXTERNAL_STORAGE, 9000);

        private final String name;
        private final int request;

        private PERMISSIONS(String s, int r) {name = s; request = r; }
        public String toString() {
            return this.name;
        }
        public int requestCode() {
            return this.request;
        }
    }


    //Create ------------------->
    public static String[] CreatePermissions(PERMISSIONS[] permissions) {

        String[] returnValue = new String[permissions.length];

        int count = 0;
        for (int i = 0; i < permissions.length; i++)
        {
            returnValue[i] = permissions[i].toString();
        }

        return returnValue;

    }



    //Request ----------------->
    public static void RequestPermission(Activity activity, PERMISSIONS permission) {

        ActivityCompat.requestPermissions(activity, new String[]{permission.toString()}, permission.requestCode());
    }



    //Check ------------------->
    public static boolean CheckPermission(Activity activity, PERMISSIONS permission) {

        if(ContextCompat.checkSelfPermission(activity, permission.toString()) != PackageManager.PERMISSION_GRANTED)
            return false;
        else
            return true;

    }
}
