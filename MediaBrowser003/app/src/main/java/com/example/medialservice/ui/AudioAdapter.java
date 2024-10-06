package com.example.medialservice.ui;

import android.annotation.SuppressLint;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medialservice.R;
import com.example.medialservice.client.MediaBrowserHelper;
import com.example.medialservice.databinding.AudioTileBinding;

public class AudioAdapter extends ListAdapter<Audio, AudioAdapter.AudioViewHolder> {

    MediaBrowserHelper mediaController;
    LiveData<Audio> nowPlaying;
    LifecycleOwner lifecycleOwner;

    protected AudioAdapter(MediaBrowserHelper mediaController, LiveData<Audio> nowPlaying, LifecycleOwner lifecycleOwner) {
        super(new DiffUtil.ItemCallback<Audio>() {
            @Override
            public boolean areItemsTheSame(@NonNull Audio oldItem, @NonNull Audio newItem) {
                return oldItem.id.equals(newItem.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Audio oldItem, @NonNull Audio newItem) {
                return oldItem.equals(newItem);
            }
        });

        this.mediaController = mediaController;
        this.nowPlaying = nowPlaying;
        this.lifecycleOwner = lifecycleOwner;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AudioViewHolder(AudioTileBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), mediaController);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        Audio audio = getItem(position);
        holder.bind(audio);
    }

    class AudioViewHolder extends RecyclerView.ViewHolder {
        AudioTileBinding binding;
        MediaBrowserHelper mediaController;

        public AudioViewHolder(@NonNull AudioTileBinding itemView, MediaBrowserHelper mediaController) {
            super(itemView.getRoot());
            binding = itemView;
            this.mediaController = mediaController;
        }

        public void bind(Audio audio) {

            binding.tileContainer.setOnClickListener(v -> {
                mediaController.getTransportControls().playFromMediaId(audio.id, null);
            });

            binding.name.setText(audio.name);
            binding.title.setText(audio.artist);

            nowPlaying.observe(lifecycleOwner, it -> {
                if (audio.name.equals(it.name)) {

                    if(it.isPlaying){
                        binding.tileContainer.setBackgroundColor(
                                binding.tileContainer.getResources().getColor(
                                        R.color.primaryDarkColor
                                )
                        );

                    }
                }else {
                    binding.tileContainer.setBackgroundColor(
                            binding.tileContainer.getResources().getColor(
                                    R.color.primaryColor
                            )
                    );
                }
            });
        }
    }
}
