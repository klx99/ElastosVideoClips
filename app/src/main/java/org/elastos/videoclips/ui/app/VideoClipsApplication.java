package org.elastos.videoclips.ui.app;

import android.app.Application;

import org.elastos.thirdparty.carrier.CarrierHelper;
import org.elastos.videoclips.utils.Utils;

import vip.z4k.android.sdk.manager.SdkManager;

public class VideoClipsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Utils.setAppContext(this);

        // init carrier sdk
//        CarrierHelper.startCarrier(this);

        // init titan sdk
        final int customerId = 0x45A0EADD;
        final int port = 29986;
        SdkManager.getInstance(this).setCustomerID(customerId);
        SdkManager.getInstance(this).setLocalDataPort (port);
        SdkManager.getInstance(this).init();
    }
}
