/*
 * Copyright (C) 2016 The Android Open Source Project
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

package org.elastos.videoclips.ui.cards.presenters;

import android.content.Context;

import org.elastos.videoclips.ui.models.Card;
import org.elastos.videoclips.ui.models.VideoCard;
import org.elastos.videoclips.utils.Utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.support.v17.leanback.widget.ImageCardView;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.util.HashMap;

/**
 * Presenter for rendering video cards on the Vertical Grid fragment.
 */
public class VideoCardViewPresenter extends ImageCardViewPresenter {

    public VideoCardViewPresenter(Context context, int cardThemeResId) {
        super(context, cardThemeResId);
    }

    public VideoCardViewPresenter(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(Card card, final ImageCardView cardView) {
        super.onBindViewHolder(card, cardView);
        VideoCard videoCard = (VideoCard) card;

        if(videoCard.getImageUrl() == null) {
            new Thread(() -> {
                loadImageFromVideo(videoCard.getVideoSources().get(0), cardView);
            }).start();
            return;
        }

        Glide.with(getContext())
                .asBitmap()
                .load(videoCard.getImageUrl())
                .into(cardView.getMainImageView());
    }

    private void loadImageFromVideo(String videoUrl, final ImageCardView cardView) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            if (Build.VERSION.SDK_INT >= 14) {
                retriever.setDataSource(videoUrl, new HashMap<String, String>());
            } else {
                retriever.setDataSource(videoUrl);
            }
            /*getFrameAtTime()--->在setDataSource()之后调用此方法。 如果可能，该方法在任何时间位置找到代表性的帧，
             并将其作为位图返回。这对于生成输入数据源的缩略图很有用。**/
            Bitmap bitmap = retriever.getFrameAtTime();

            cardView.getMainImageView().setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.w(Utils.TAG, "Failed to get video poster for url: " + videoUrl, e);
        } finally {
            try {
                retriever.release();
            } catch (IllegalArgumentException e) {
            }
        }
    }
}
