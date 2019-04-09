/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.elastos.videoclips.ui.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.elastos.videoclips.R;

/**
 * TODO: Javadoc
 */
public class QRCodeView extends LinearLayout {
    public QRCodeView(Context context) {
        super(context);

        LayoutParams layoutParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        setLayoutParams(layoutParams);

        this.setOrientation(LinearLayout.VERTICAL);

        mTitle = new TextView(context);
        this.addView(mTitle);
        mQRCode = new ImageView(context);
        this.addView(mQRCode);
        mStatus = new TextView(context);
        this.addView(mStatus);
        mStatus.setText("Please scan this QRCode.");
    }

    public void setTitle(String title) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() ->
                mTitle.setText(title)
        );
    }

    public void setQRCode(Bitmap bitmap) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() ->
                mQRCode.setImageBitmap(bitmap)
        );
    }

    public void setStatus(String status) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() ->
                mStatus.setText(status)
        );
    }

    private TextView mTitle;
    private ImageView mQRCode;
    private TextView mStatus;
}
