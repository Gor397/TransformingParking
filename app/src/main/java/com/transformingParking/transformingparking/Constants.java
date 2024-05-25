package com.transformingParking.transformingparking;

import android.content.Context;

public class Constants {
    public static final int CROP_IMAGE_REQUEST_CODE = 1;
    public static final int FCM_SERVER_KEY_REC_INT = R.string.FCM_SERVER_KEY;

    // Server and hardware
    public static String SERVER_URL = "https://parking-server.simonyan-gor-397.workers.dev/";
    public static String SERVER_SECRET = "920ef64e081d6c8d5861db5a34919506";
    public static final int PENDING = -1;
    public static final int GET_STATUS = 0;
    public static final int OPEN_COMMAND = 1;
    public static final int BUSY = 2;
    public static final int FREE = 3;
    public static final int PAID = 5;
    public static final int UPDATE = 6;
    public static final int OPEN_COMMAND_OWNER = 7;
    public static final int CLOSE_COMMAND_OWNER = 8;
}
