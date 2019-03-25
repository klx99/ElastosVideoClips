package org.elastos.videoclips.utils;

import android.content.Context;

public class Utils {
    public static final String TAG = "elastos";

    public static void setAppContext(Context context) {
        mContext = context;
    }

    public static Context getAppContext() {
        return mContext;
    }

    private static Context mContext;
}
