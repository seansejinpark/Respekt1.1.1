/*
 * Copyright 2018 Nazmul Idris. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.respekt.mediaplayer;

import android.content.Context;
import android.media.MediaPlayer;

import com.respekt.models.DataItem;
import com.respekt.utils.MeData;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Provides access to a MediaPlayer object which is used to play a single MP3 file from the
 * <code>res/raw</code> folder.
 */
public class MediaPlayerHolder {

    public static final int SEEKBAR_REFRESH_INTERVAL_MS = 1000;

    private String mResourceId;
    private final MediaPlayer mMediaPlayer;
    private Context mContext;
    private ArrayList<String> mLogMessages = new ArrayList<>();
    private ScheduledExecutorService mExecutor;
    private Runnable mSeekbarProgressUpdateTask;
    private DataItem dataItem = null;

    public enum PlayerState {
        PLAYING, PAUSED, COMPLETED, RESET, STOP
    }

    public void setDataItem(DataItem dataItem) {
        this.dataItem = dataItem;
    }

    public DataItem getDataItem() {
        return dataItem;
    }

    public MediaPlayerHolder(Context context) {
        mContext = context;
        EventBus.getDefault().register(this);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopUpdatingSeekbarWithPlaybackProgress(true);
                logToUI("MediaPlayer playback completed");
                EventBus.getDefault().post(new LocalEventFromMediaPlayerHolder.PlaybackCompleted());
                EventBus.getDefault()
                        .post(new LocalEventFromMediaPlayerHolder.StateChanged(
                                PlayerState.COMPLETED));
            }
        });
        logToUI("mMediaPlayer = new MediaPlayer()");
    }

    // MediaPlayer orchestration.

    public void release() {
        logToUI("release() and mMediaPlayer = null");
        mMediaPlayer.release();
        EventBus.getDefault().unregister(this);
    }

    public void play() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            startUpdatingSeekbarWithPlaybackProgress();
            EventBus.getDefault()
                    .post(new LocalEventFromMediaPlayerHolder.StateChanged(PlayerState.PLAYING));
        }
    }


    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            logToUI("pause()");
            EventBus.getDefault()
                    .post(new LocalEventFromMediaPlayerHolder.StateChanged(PlayerState.PAUSED));
        }
    }

    public void reset(boolean isStop) {
        logToUI("reset()");
        mMediaPlayer.reset();
        load(mResourceId, isStop);
        stopUpdatingSeekbarWithPlaybackProgress(true);
        EventBus.getDefault()
                .post(new LocalEventFromMediaPlayerHolder.StateChanged(PlayerState.RESET));
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void load(String resourceId, boolean isStop) {
        mResourceId = resourceId;

        try {
            logToUI("load() {1. setDataSource}");
            mMediaPlayer.setDataSource(mResourceId);
        } catch (Exception e) {
            logToUI(e.toString());
        }

        try {
            logToUI("load() {2. prepare}");
            mMediaPlayer.prepareAsync();

        } catch (Exception e) {
            logToUI(e.toString());
        }
        if (isStop) {
            EventBus.getDefault()
                    .post(new LocalEventFromMediaPlayerHolder.StateChanged(PlayerState.STOP));
        }
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                initSeekbar();
                if (!isStop)
                    EventBus.getDefault().post(new LocalEventFromMediaPlayerHolder.PreparedDone());
            }
        });
    }

    public void seekTo(int duration) {
        logToUI(String.format("seekTo() %d ms", duration));
        mMediaPlayer.seekTo(duration);
    }

    // Reporting media playback position to Seekbar in MainActivity.

    private void stopUpdatingSeekbarWithPlaybackProgress(boolean resetUIPlaybackPosition) {
        if (mExecutor != null) {
            mExecutor.shutdownNow();
            mExecutor = null;
            mSeekbarProgressUpdateTask = null;
        }
        if (resetUIPlaybackPosition) {
            EventBus.getDefault().post(new LocalEventFromMediaPlayerHolder.PlaybackPosition(0));
        }
    }

    private void startUpdatingSeekbarWithPlaybackProgress() {
        // Setup a recurring task to sync the mMediaPlayer position with the Seekbar.
        if (mExecutor == null) {
            mExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        if (mSeekbarProgressUpdateTask == null) {
            mSeekbarProgressUpdateTask = new Runnable() {
                @Override
                public void run() {
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        int currentPosition = mMediaPlayer.getCurrentPosition();
                        Integer totalMinutes = Integer.parseInt(MeData.Companion.getTotalMinutes());
                        if (mMediaPlayer.isPlaying())
                            totalMinutes = totalMinutes + 1000;

                        MeData.Companion.setTotalMinutes(totalMinutes + "");
                        EventBus.getDefault().post(
                                new LocalEventFromMediaPlayerHolder.PlaybackPosition(
                                        currentPosition));
                    }
                }
            };
        }
        mExecutor.scheduleAtFixedRate(
                mSeekbarProgressUpdateTask,
                0,
                SEEKBAR_REFRESH_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }

    public void initSeekbar() {
        // Set the duration.
        final int duration = mMediaPlayer.getDuration();
        EventBus.getDefault().post(
                new LocalEventFromMediaPlayerHolder.PlaybackDuration(duration));
        logToUI(String.format("setting seekbar max %d sec",
                TimeUnit.MILLISECONDS.toSeconds(duration)));
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(LocalEventFromMainActivity.SeekTo event) {
        seekTo(event.position);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(
            LocalEventFromMainActivity.StopUpdatingSeekbarWithMediaPosition event) {
        stopUpdatingSeekbarWithPlaybackProgress(false);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(
            LocalEventFromMainActivity.StartUpdatingSeekbarWithPlaybackPosition event) {
        startUpdatingSeekbarWithPlaybackProgress();
    }

    // Logging to UI methods.

    public void logToUI(String msg) {
        mLogMessages.add(msg);
        fireLogUpdate();
    }

    /**
     * update the MainActivity's UI with the debug log messages
     */
    public void fireLogUpdate() {
        StringBuffer formattedLogMessages = new StringBuffer();
        for (int i = 0; i < mLogMessages.size(); i++) {
            formattedLogMessages.append(i)
                    .append(" - ")
                    .append(mLogMessages.get(i));
            if (i != mLogMessages.size() - 1) {
                formattedLogMessages.append("\n");
            }
        }
        EventBus.getDefault().post(
                new LocalEventFromMediaPlayerHolder.UpdateLog(formattedLogMessages));
    }

    // Respond to playback localevents.

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(LocalEventFromMainActivity.PausePlayback event) {
        pause();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(LocalEventFromMainActivity.StartPlayback event) {
        play();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(LocalEventFromMainActivity.StopPlayback event) {
        reset(true);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(LocalEventFromMainActivity.ResetPlayback event) {
        reset(false);
    }

}
