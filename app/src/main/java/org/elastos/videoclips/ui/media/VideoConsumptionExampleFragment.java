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

package org.elastos.videoclips.ui.media;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v17.leanback.app.VideoFragment;
import android.support.v17.leanback.app.VideoFragmentGlueHost;
import android.support.v17.leanback.media.MediaPlayerAdapter;
import android.support.v17.leanback.media.PlaybackGlue;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.util.Log;
import android.widget.Toast;


public class VideoConsumptionExampleFragment extends VideoFragment {

    private static final String URL = "https://storage.googleapis.com/android-tv/Sample videos/"
            + "April Fool's 2013/Explore Treasure Mode with Google Maps.mp4";
    public static final String TAG = "VideoConsumption";
    private VideoMediaPlayerGlue<MediaPlayerAdapter> mMediaPlayerGlue;
    final VideoFragmentGlueHost mHost = new VideoFragmentGlueHost(this);

    static void playWhenReady(PlaybackGlue glue) {
        if (glue.isPrepared()) {
            glue.play();
        } else {
            glue.addPlayerCallback(new PlaybackGlue.PlayerCallback() {
                @Override
                public void onPreparedStateChanged(PlaybackGlue glue) {
                    if (glue.isPrepared()) {
                        glue.removePlayerCallback(this);
                        glue.play();
                    }
                }
            });
        }
    }

    AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener
            = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int state) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AudioManager audioManager = (AudioManager) getActivity()
                .getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.w(TAG, "video player cannot obtain audio focus!");
        }

        MediaMetaData intentMetaData = getActivity().getIntent().getParcelableExtra(
                VideoExampleActivity.TAG);
        if(intentMetaData == null || intentMetaData.getAdvertisingUrl() != null) {
            mMediaPlayerGlue = initPlayerGlue(intentMetaData.getAdvertisingUrl(), null, null);
            final PlaybackGlue.PlayerCallback playerCallback = new PlaybackGlue.PlayerCallback() {
                @Override
                public void onPlayCompleted(PlaybackGlue glue) {
                    if(intentMetaData == null) {
                        return;
                    }
                    mMediaPlayerGlue = initPlayerGlue(
                            intentMetaData.getMediaSourcePath(),
                            intentMetaData.getMediaTitle(),
                            intentMetaData.getMediaArtistName()
                    );
                }
            };
            mMediaPlayerGlue.addPlayerCallback(playerCallback);
            mMediaPlayerGlue.setControlsOverlayAutoHideEnabled(false);
            hideControlsOverlay(false);
        } else {
            mMediaPlayerGlue = initPlayerGlue(
                    intentMetaData.getMediaSourcePath(),
                    intentMetaData.getMediaTitle(),
                    intentMetaData.getMediaArtistName()
            );
        }
    }

    @Override
    public void onPause() {
        if (mMediaPlayerGlue != null) {
            mMediaPlayerGlue.pause();
        }
        super.onPause();
    }

    private VideoMediaPlayerGlue<MediaPlayerAdapter> initPlayerGlue(String url, String title, String artistName) {
        VideoMediaPlayerGlue<MediaPlayerAdapter> mediaPlayerGlue = new VideoMediaPlayerGlue(getActivity(),
                new MediaPlayerAdapter(getActivity()));
        mediaPlayerGlue.setHost(mHost);
        mediaPlayerGlue.setMode(PlaybackControlsRow.RepeatAction.NONE);
        mediaPlayerGlue.setTitle(title);
        mediaPlayerGlue.setSubtitle(artistName);
        mediaPlayerGlue.getPlayerAdapter().setDataSource(Uri.parse(url));
        PlaybackSeekDiskDataProvider.setDemoSeekProvider(mediaPlayerGlue);
        playWhenReady(mediaPlayerGlue);
        setBackgroundType(BG_LIGHT);

        return mediaPlayerGlue;
    }
}
