package org.elastos.videoclips.ui.app;

import android.app.Application;

public class VideoClipsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        final String customerId = " 0x45A0EADD";
        final int port = 29986;
        SdkManager.getInstance(this).setCustomerID(customerId);
        /* 设置HTTP本地监听端口，端口由Titan为客户分配*/
        SdkManager.getInstance(this).setLocalDataPort (port);
        /* 固定调用 */
        SdkManager.getInstance(this).init();
    }
}
