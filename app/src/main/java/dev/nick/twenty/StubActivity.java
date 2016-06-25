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
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nick.scalpel.Scalpel;
import com.nick.scalpel.annotation.binding.FindView;
import com.nick.scalpel.annotation.binding.MainThreadHandler;
import com.nick.scalpel.annotation.request.RequirePermission;

import java.io.File;
import java.util.List;

import dev.nick.imageloader.ZImageLoader;
import dev.nick.imageloader.display.DisplayOption;

@RequirePermission(permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET})
public class StubActivity extends AppCompatActivity {

    @MainThreadHandler
    Handler mHandler;

    @FindView(id = R.id.list)
    ListView listView;

    final String urlDrawable = "drawable://girl";
    final String urlHttp = "http://tse2.mm.bing.net/th?id=OIP.M960c6796f4870a8764558c39e9148afao2&pid=15.1";
    final String urlAssets = "assets://girl.jpg";

    static String mArtworkUri = "content://media/external/audio/albumart";

    String[] urls = new String[]{urlAssets, urlDrawable, urlHttp, urlDrawable, urlAssets};

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stub);
        Scalpel.getInstance().wire(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final List<Track> tracks = MediaUtils.getTrackList(this);

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
                    convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item, parent, false);
                    holder = new ViewHolder(convertView);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.textView.setText(tracks.get(position).getTitle());

                String uri = mArtworkUri + File.separator + tracks.get(position).getAlbumId();

                ZImageLoader.getInstance().displayImage(uri, holder.imageView,
                        new DisplayOption(R.drawable.ic_broken_image_black_24dp,
                                R.drawable.ic_cloud_download_black_24dp));

                return convertView;
            }

        };

        listView.setAdapter(adapter);
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
