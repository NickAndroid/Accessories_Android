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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dev.nick.accessories.injection.Injector;
import dev.nick.accessories.injection.annotation.binding.BindView;
import dev.nick.accessories.injection.annotation.permission.RequestPermissions;
import dev.nick.accessories.media.loader.LoaderConfig;
import dev.nick.accessories.media.loader.MediaLoader;
import dev.nick.accessories.media.loader.ProgressListenerStub;
import dev.nick.accessories.media.loader.cache.CachePolicy;
import dev.nick.accessories.media.loader.ui.DisplayOption;
import dev.nick.accessories.media.loader.ui.MediaQuality;
import dev.nick.accessories.media.loader.ui.animator.FadeInViewAnimator;
import dev.nick.accessories.media.loader.worker.bitmap.BitmapSource;
import dev.nick.accessoriestest.R;

@RequestPermissions(permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET})
public class ScrollStateTest extends BaseTest {

    @BindView(R.id.list)
    ListView listView;

    MediaLoader mediaLoader;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_image_layout);
        setTitle(getClass().getSimpleName());
        Injector.shared().inject(this);
        mediaLoader = MediaLoader.shared().fork(LoaderConfig.builder()
                .debugLevel(Log.VERBOSE)
                .cachePolicy(CachePolicy.builder().build()).build());
        mediaLoader.linkScrollStateTo(listView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaLoader.unLinkScrollStateTo(listView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final List<Track> tracks = gallery();

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

                //  convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.list_item, parent, false);
                //holder = new ViewHolder(convertView);

                holder.textView.setText(tracks.get(position).getTitle());

                // String uri = mArtworkUri + File.separator + tracks.get(position).getAlbumId();
                String uri = BitmapSource.FILE.getPrefix() + tracks.get(position).getUrl();

                mediaLoader.loadBitmap()
                        .from(uri)
                        .option(DisplayOption.bitmapBuilder()
                                .imageQuality(MediaQuality.OPT)
                                .viewMaybeReused()
                                .animateOnlyNewLoaded()
                                .showOnLoading(null)
                                .imageAnimator(new FadeInViewAnimator())
                                .build())
                        .progressListener(new ProgressListenerStub<Bitmap>())
                        .into(holder.imageView)
                        .start();

                return convertView;
            }

        };

        listView.setAdapter(adapter);
    }

    List<Track> gallery() {

        List<Track> tracks = new ArrayList<>();

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (cursor == null || cursor.getCount() == 0) return tracks;

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
            int pathIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

            String name = cursor.getString(nameIndex);
            String path = cursor.getString(pathIndex);

            Track track = new Track();
            track.setTitle(name);
            track.setUrl(path);

            tracks.add(track);
        }

        cursor.close();

        return tracks;
    }

    class ViewHolder implements BindView.RootViewProvider {
        @BindView(R.id.image)
        ImageView imageView;
        @BindView(R.id.textView)
        TextView textView;

        View mRoot;

        public ViewHolder(View convert) {
            mRoot = convert;
            Injector.shared().inject(this);
        }

        @NonNull
        @Override
        public View getRootView() {
            return mRoot;
        }
    }
}
