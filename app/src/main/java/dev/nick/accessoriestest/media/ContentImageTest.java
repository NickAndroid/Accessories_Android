/*
 * Copyright (c) 2016 Nick Guo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nick.accessoriestest.media;

import android.Manifest;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nick.scalpel.Scalpel;
import com.nick.scalpel.annotation.binding.FindView;
import com.nick.scalpel.annotation.request.RequirePermission;

import java.io.File;
import java.util.List;

import dev.nick.accessories.binding.BindingAccessories;
import dev.nick.accessories.binding.annotation.permission.RequestPermissions;
import dev.nick.accessories.media.AccessoryConfig;
import dev.nick.accessories.media.MediaAccessory;
import dev.nick.accessories.media.cache.CachePolicy;
import dev.nick.accessories.media.ui.DisplayOption;
import dev.nick.accessories.media.ui.MediaQuality;
import dev.nick.accessories.media.ui.animator.FadeInViewAnimator;
import dev.nick.accessories.media.worker.network.NetworkPolicy;
import dev.nick.accessoriestest.R;
import dev.nick.accessoriestest.model.IMediaTrack;
import dev.nick.accessoriestest.player.repository.TrackLoader;
import dev.nick.accessoriestest.service.MediaPlayerService;
import dev.nick.accessoriestest.service.UserCategory;

@RequirePermission(permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET})
@RequestPermissions
public class ContentImageTest extends BaseTest {

    static String mArtworkUri = "content://media/external/audio/albumart";
    @FindView(id = R.id.list)
    ListView listView;
    MediaAccessory mLoader;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_image_layout);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setTitle(getClass().getSimpleName());
        Scalpel.getInstance().wire(this);
        BindingAccessories.shared().process(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final List<IMediaTrack> tracks = TrackLoader.get().load(UserCategory.ALL, getApplicationContext());

        MediaPlayerService.Proxy.assumePendingList(tracks, getApplicationContext());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MediaPlayerService.Proxy.play(tracks.get(i), getApplicationContext());
            }
        });

        mLoader = MediaAccessory.shared().fork(AccessoryConfig.builder()
                .cachePolicy(CachePolicy.builder()
                        .enableMemCache()
                        .enableDiskCache()
                        .cachingThreads(Runtime.getRuntime().availableProcessors())
                        .cacheDirName("dis.cache.tests.content")
                        .preferredLocation(CachePolicy.Location.INTERNAL)
                        .compressFormat(Bitmap.CompressFormat.JPEG)
                        .build())
                .networkPolicy(NetworkPolicy.builder().trafficStatsEnabled(true).build())
                .build());

        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return tracks.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                ViewHolder holder;

                if (convertView == null) {
                    convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.list_item, parent, false);
                    holder = new ViewHolder(convertView);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.textView.setText(tracks.get(position).getTitle());

                String uri = mArtworkUri + File.separator + tracks.get(position).getAlbumId();

                mLoader.loadBitmap()
                        .from(uri)
                        .option(DisplayOption.bitmapBuilder()
                                .imageQuality(MediaQuality.RAW)
                                .viewMaybeReused()
                                .imageAnimator(new FadeInViewAnimator())
                                .build())
                        .into(holder.imageView)
                        .start();

                return convertView;
            }
        };

        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLoader.clearMemCache();
        mLoader.terminate();
        MediaPlayerService.Proxy.stop(getApplicationContext());
    }

    class ViewHolder {
        @FindView(id = R.id.image)
        ImageView imageView;
        @FindView(id = R.id.textView)
        TextView textView;

        public ViewHolder(View convert) {
            Scalpel.getInstance().wire(convert, this);
        }
    }
}
