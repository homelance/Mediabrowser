package com.example.medialservice.ui;

import static com.example.medialservice.service.contentcatalogs.Utils.loge;

import android.content.Context;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.medialservice.R;
import com.example.medialservice.client.MediaBrowserHelper;
import com.example.medialservice.databinding.FragmentTrackBinding;
import com.example.medialservice.service.MusicService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrackFragment extends Fragment {

    FragmentTrackBinding binding;
    TrackViewmodel vm;
    ActivityResultLauncher<String> requestPermissionLauncher;
    AudioAdapter adapter;
    MediaBrowserHelper mMediaBrowserHelper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTrackBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    vm.onPermissionGranted();
                }
            }
        });

        NowPLayingViewModelFactory factory = new NowPLayingViewModelFactory(getActivity().getApplication(), requestPermissionLauncher);
        vm = new ViewModelProvider(this, factory).get(TrackViewmodel.class);

        binding.permissionButton.setOnClickListener(v -> {
            vm.checkPermission();
        });

        vm.storagePermissionGranted.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                binding.permissionLayout.setVisibility(aBoolean ? View.GONE : View.VISIBLE);
                binding.audioList.setVisibility(aBoolean ? View.VISIBLE : View.GONE);

                if (aBoolean) {

                    mMediaBrowserHelper = new MediaBrowserConnection(getContext());
                    mMediaBrowserHelper.registerCallback(new MediaBrowserListener());

                    initAdapter(mMediaBrowserHelper, vm.nowPlaying);
                    vm.audios.observe(getViewLifecycleOwner(), audio -> {
                        adapter.submitList(audio);
                    });
                }
            }
        });

        /*vm.mediaController.observe(getViewLifecycleOwner(), mediaControllerCompat -> {

            if(mediaControllerCompat!=null){
                initAdapter(mediaControllerCompat, vm.nowPlaying);
                vm.audios.observe(getViewLifecycleOwner(), audio -> {
                    adapter.submitList(audio);
                });
            }

        });*/

    }

    void initAdapter(MediaBrowserHelper mediaController, LiveData<Audio> nowPlaying) {
        adapter = new AudioAdapter(mediaController, nowPlaying, getViewLifecycleOwner());
        binding.audioList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.audioList.setAdapter(adapter);

    }

    private class MediaBrowserConnection extends MediaBrowserHelper {
        private MediaBrowserConnection(Context context) {
            super(context, MusicService.class);
        }

        @Override
        protected void onConnected(@NonNull MediaControllerCompat mediaController) {
//            mSeekBarAudio.setMediaController(mediaController);
            loge("Frag", "onConnected", "");
        }

        @Override
        protected void onChildrenLoaded(@NonNull String parentId,
                                        @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);

            final MediaControllerCompat mediaController = getMediaController();

            // Queue up all media items for this simple sample.
            List<Audio> items = new ArrayList<>();
            for (final MediaBrowserCompat.MediaItem mediaItem : children) {
                mediaController.addQueueItem(mediaItem.getDescription());
                items.add(new Audio(
                        mediaItem.getDescription().getMediaId().toString(),
                        mediaItem.getDescription().getTitle().toString(),
                        mediaItem.getDescription().getSubtitle().toString(),
                        mediaItem.getDescription().getMediaUri()
                ));
            }

            vm.audios.setValue(items);

            // Call prepare now so pressing play just works.
            mediaController.getTransportControls().prepare();


            loge("Frag", "onChildrenLoaded", "" + parentId);
        }
    }

    private class MediaBrowserListener extends MediaControllerCompat.Callback {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            //playback state
            loge("Frag", "onPlaybackStateChanged", "");
            if(/*vm.audios.getValue()!=null && !vm.audios.getValue().isEmpty() &&*/ state!=null){
                Bundle bundle = state.getExtras();

                Audio audio = new Audio(
                        bundle.getString("id").toString(),
                        bundle.getString("title").toString(),
                        bundle.getString("subtitle").toString(),
                        Uri.parse(bundle.getString("uri").toString())
                );
                if (((PlaybackState)state.getPlaybackState()).getState() ==  PlaybackState.STATE_PLAYING) {
                    audio.isPlaying = true;
                }

                vm.nowPlaying.setValue(audio);
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);

            // get changed music info
            loge("Frag", "onMetadataChanged", "");
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
        }
    }


}