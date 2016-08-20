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

package dev.nick.twenty;

import android.Manifest;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nick.scalpel.Scalpel;
import com.nick.scalpel.annotation.binding.FindView;
import com.nick.scalpel.annotation.request.RequirePermission;

import java.util.ArrayList;
import java.util.List;

import dev.nick.imageloader.ImageLoader;
import dev.nick.imageloader.LoaderConfig;
import dev.nick.imageloader.ProgressListenerStub;
import dev.nick.imageloader.cache.CachePolicy;
import dev.nick.imageloader.ui.DisplayOption;
import dev.nick.imageloader.ui.ImageQuality;
import dev.nick.imageloader.ui.animator.FadeInImageAnimator;
import dev.nick.imageloader.worker.bitmap.BitmapImageSource;

@RequirePermission(permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET})
public class ScrollStateTest extends BaseTest {

    @FindView(id = R.id.list)
    ListView listView;

    ImageLoader imageLoader;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_image_layout);
        setTitle(getClass().getSimpleName());
        Scalpel.getInstance().wire(this);
        imageLoader = ImageLoader.shared().fork(LoaderConfig.builder()
                .cachePolicy(CachePolicy.builder().build()).build());
        imageLoader.linkScrollStateTo(listView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageLoader.unLinkScrollStateTo(listView);
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
                String uri = BitmapImageSource.FILE.getPrefix() + tracks.get(position).getUrl();

                ImageLoader.shared().loadBitmap()
                        .from(uri)
                        .option(DisplayOption.bitmapBuilder()
                                .imageQuality(ImageQuality.OPT)
                                .viewMaybeReused()
                                .animateOnlyNewLoaded()
                                .imageAnimator(new FadeInImageAnimator())
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
