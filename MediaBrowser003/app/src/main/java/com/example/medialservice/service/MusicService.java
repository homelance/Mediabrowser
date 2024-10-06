package com.example.medialservice.service;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;

import com.example.medialservice.service.contentcatalogs.AudioRepository;
import com.example.medialservice.service.contentcatalogs.Utils;
import com.example.medialservice.service.notifications.MediaNotificationManager;
import com.example.medialservice.service.players.MediaPlayerAdapter;
import com.example.medialservice.ui.Audio;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MusicService extends MediaBrowserServiceCompat {

    private static final String TAG = MusicService.class.getSimpleName();

    private MediaSessionCompat mSession;
    private PlayerAdapter mPlayback;
    private MediaNotificationManager mMediaNotificationManager;
    private MediaSessionCallback mCallback;
    private boolean mServiceInStartedState;
    private AudioRepository audioRepository;
    List<MediaBrowserCompat.MediaItem> mediaItems;


    @Override
    public void onCreate() {
        super.onCreate();
        audioRepository = new AudioRepository(getApplication());
        // Create a new MediaSession.
        mSession = new MediaSessionCompat(this, "MusicService");
        mCallback = new MediaSessionCallback();
        mSession.setCallback(mCallback);
        mSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        setSessionToken(mSession.getSessionToken());

        mMediaNotificationManager = new MediaNotificationManager(this);

        mPlayback = new MediaPlayerAdapter(this, new MediaPlayerListener());
        Log.d(TAG, "onCreate: MusicService creating MediaSession, and MediaNotificationManager");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        mMediaNotificationManager.onDestroy();
        mPlayback.stop();
        mSession.release();
        Log.d(TAG, "onDestroy: MediaPlayerAdapter stopped, and MediaSession released");
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName,
                                 int clientUid,
                                 Bundle rootHints) {
        return new BrowserRoot(Utils.ROOT, null);
    }

    @Override
    public void onLoadChildren(
            @NonNull final String parentId,
            @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
//        result.sendResult(MusicLibrary.getMediaItems());
        Utils.loge("onLoadChildren", parentId);
        if (Utils.ROOT.equals(parentId)) {
            try {

                mediaItems = new ArrayList<>();
                for (Audio it : audioRepository.listAudioFiles()) {
                    MediaDescriptionCompat.Builder mediaDescriptionBuilder = new MediaDescriptionCompat.Builder()
                            .setMediaId(it.id)
                            .setMediaUri(it.uri)
                            .setTitle(it.name)
                            .setSubtitle(it.artist);
                    mediaItems.add(new MediaBrowserCompat.MediaItem(mediaDescriptionBuilder.build(), 0));
                }
                result.sendResult(mediaItems);

            } catch (Exception e) {
                result.sendResult(null);
            }
        } else {
            result.sendResult(null);
        }
    }

    // MediaSession Callback: Transport Controls -> MediaPlayerAdapter
    public class MediaSessionCallback extends MediaSessionCompat.Callback {
        //        private final List<MediaSessionCompat.QueueItem> mPlaylist = new ArrayList<>();
        private final List<MediaSessionCompat.QueueItem> queueItems = new ArrayList<>();
        private int mQueueIndex = -1;
        private int activeQueueItemId = -1;
        private MediaBrowserCompat.MediaItem nowPlaying = null;
        private MediaMetadataCompat mPreparedMedia;

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            queueItems.add(new MediaSessionCompat.QueueItem(description, description.hashCode()));
//            mQueueIndex = (mQueueIndex == -1) ? 0 : mQueueIndex;
            mSession.setQueue(queueItems);
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
//            mPlaylist.remove(new MediaSessionCompat.QueueItem(description, description.hashCode()));
//            mQueueIndex = (mPlaylist.isEmpty()) ? -1 : mQueueIndex;
//            mSession.setQueue(mPlaylist);
        }

        @Override
        public void onPrepare() {
//            if (mQueueIndex < 0 && mPlaylist.isEmpty()) {
            // Nothing to play.
//                return;
//            }

            /*final String mediaId = mPlaylist.get(mQueueIndex).getDescription().getMediaId();
            mPreparedMedia = MusicLibrary.getMetadata(MusicService.this, mediaId);
            mSession.setMetadata(mPreparedMedia);


             */
            if (!mSession.isActive()) {
                mSession.setActive(true);
            }

        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
            Utils.loge(TAG, "onPlayFromMediaId", "" + mediaId);

            //
            MediaBrowserCompat.MediaItem item = findMediaItemById(mediaId);
            if (item != null) {
                mPreparedMedia = new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, item.getDescription().getSubtitle().toString())
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, item.getDescription().getSubtitle().toString())
//                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, TimeUnit.MILLISECONDS.convert(duration, durationUnit))
//                        .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, item.getDescription().getMediaUri().toString())
//                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, getAlbumArtUri(albumArtResName))
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, item.getDescription().getTitle().toString())
                        .build();

                mPlayback.playFromMedia(mPreparedMedia);
                nowPlaying = item;
                activeQueueItemId = -1;
                for (int i = 0; i < queueItems.size(); i++) {
                    MediaSessionCompat.QueueItem it = queueItems.get(i);
                    if (Objects.equals(nowPlaying.getMediaId(), it.getDescription().getMediaId())) {
                        activeQueueItemId = i;
                        break;
                    }
                }

            }

        }

        @Override
        public void onPlay() {
            if (!isReadyToPlay()) {
//             Nothing to play.
                return;
            }

            if (mPreparedMedia == null) {
//                onPrepare();
                return;
            }

            mPlayback.playFromMedia(mPreparedMedia);

            Log.d(TAG, "onPlayFromMediaId: MediaSession active");
        }

        @Override
        public void onPause() {
            mPlayback.pause();
        }

        @Override
        public void onStop() {
            mPlayback.stop();
            mSession.setActive(false);
        }

        @Override
        public void onSkipToNext() {
//            mQueueIndex = (++mQueueIndex % mPlaylist.size());
//            mPreparedMedia = null;
//            onPlay();
            if (queueItems != null && nowPlaying != null) {

                int nextItemPos = activeQueueItemId;
                if (activeQueueItemId >= queueItems.size() - 1) {
                    nextItemPos = 0;
                } else {
                    nextItemPos++;
                }

                MediaBrowserCompat.MediaItem nextMediaItem = new MediaBrowserCompat.MediaItem(
                        queueItems.get(nextItemPos).getDescription(),
                        0);

                mPreparedMedia = new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, nextMediaItem.getDescription().getMediaId())
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, nextMediaItem.getDescription().getSubtitle().toString())
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, nextMediaItem.getDescription().getSubtitle().toString())
//                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, TimeUnit.MILLISECONDS.convert(duration, durationUnit))
//                        .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, nextMediaItem.getDescription().getMediaUri().toString())
//                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, getAlbumArtUri(albumArtResName))
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, nextMediaItem.getDescription().getTitle().toString())
                        .build();

                mPlayback.playFromMedia(mPreparedMedia);
                nowPlaying = nextMediaItem;
                activeQueueItemId = nextItemPos;
            }
        }

        @Override
        public void onSkipToPrevious() {
//            mQueueIndex = mQueueIndex > 0 ? mQueueIndex - 1 : mPlaylist.size() - 1;
//            mPreparedMedia = null;
//            onPlay();
            if (queueItems != null && nowPlaying != null) {
                int previousItemPos = activeQueueItemId;

                if(activeQueueItemId<=0){
                    previousItemPos = queueItems.size() - 1;
                }else {
                    previousItemPos--;
                }

                MediaBrowserCompat.MediaItem nextMediaItem = new MediaBrowserCompat.MediaItem(
                        queueItems.get(previousItemPos).getDescription(),
                        0);

                mPreparedMedia = new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, nextMediaItem.getDescription().getMediaId())
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, nextMediaItem.getDescription().getSubtitle().toString())
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, nextMediaItem.getDescription().getSubtitle().toString())
//                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, TimeUnit.MILLISECONDS.convert(duration, durationUnit))
//                        .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, nextMediaItem.getDescription().getMediaUri().toString())
//                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, getAlbumArtUri(albumArtResName))
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, nextMediaItem.getDescription().getTitle().toString())
                        .build();

                mPlayback.playFromMedia(mPreparedMedia);
                nowPlaying = nextMediaItem;
                activeQueueItemId  = previousItemPos;;
            }
        }

        @Override
        public void onSeekTo(long pos) {
            mPlayback.seekTo(pos);
        }

        private boolean isReadyToPlay() {
            return (!queueItems.isEmpty());
        }
    }

    // MediaPlayerAdapter Callback: MediaPlayerAdapter state -> MusicService.
    public class MediaPlayerListener extends PlaybackInfoListener {

        private final ServiceManager mServiceManager;

        MediaPlayerListener() {
            mServiceManager = new ServiceManager();
        }

        @Override
        public void onPlaybackStateChange(PlaybackStateCompat state) {
            // Report the state to the MediaSession.
            mSession.setPlaybackState(state);

            // Manage the started state of this service.
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    mServiceManager.moveServiceToStartedState(state);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    mServiceManager.updateNotificationForPause(state);
                    break;
                case PlaybackStateCompat.STATE_STOPPED:
                    mServiceManager.moveServiceOutOfStartedState(state);
                    break;
            }
        }

        class ServiceManager {

            private void moveServiceToStartedState(PlaybackStateCompat state) {
                Notification notification =
                        mMediaNotificationManager.getNotification(
                                mPlayback.getCurrentMedia(), state, getSessionToken());

                if (!mServiceInStartedState) {
                    ContextCompat.startForegroundService(
                            MusicService.this,
                            new Intent(MusicService.this, MusicService.class));
                    mServiceInStartedState = true;
                }

                startForeground(MediaNotificationManager.NOTIFICATION_ID, notification);
            }

            private void updateNotificationForPause(PlaybackStateCompat state) {
                stopForeground(false);
                Notification notification =
                        mMediaNotificationManager.getNotification(
                                mPlayback.getCurrentMedia(), state, getSessionToken());
                mMediaNotificationManager.getNotificationManager()
                        .notify(MediaNotificationManager.NOTIFICATION_ID, notification);
            }

            private void moveServiceOutOfStartedState(PlaybackStateCompat state) {
                stopForeground(true);
                stopSelf();
                mServiceInStartedState = false;
            }
        }
    }

    MediaBrowserCompat.MediaItem findMediaItemById(String mediaId) {
        MediaBrowserCompat.MediaItem item = null;
        for (MediaBrowserCompat.MediaItem it : mediaItems) {
            if (Objects.equals(mediaId, it.getDescription().getMediaId())) {
                item = it;
                break;
            }
        }
        return item;
    }
}