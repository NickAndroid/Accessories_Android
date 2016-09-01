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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dev.nick.accessories.injection.InjectionAccessory;
import dev.nick.accessories.injection.annotation.binding.BindView;
import dev.nick.accessories.injection.annotation.permission.RequestPermissions;
import dev.nick.accessories.logger.LoggerManager;
import dev.nick.accessories.media.MediaAccessory;
import dev.nick.accessories.media.queue.Priority;
import dev.nick.accessories.media.ui.DisplayOption;
import dev.nick.accessories.media.ui.MediaQuality;
import dev.nick.accessories.media.ui.animator.FadeInViewAnimator;
import dev.nick.accessories.media.worker.ProgressListener;
import dev.nick.accessories.media.worker.result.Cause;
import dev.nick.accessories.media.worker.result.ErrorListener;
import dev.nick.accessoriestest.R;

@RequestPermissions(permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET})
public class NetworkImageTest extends BaseTest {

    final String[] urls = new String[]{
            "http://i.imgur.com/ZXVlev9.jpg",
            "http://i.imgur.com/LT6RmQU.png",
            "http://i.imgur.com/8w0hWDS.jpg",
            "http://i.imgur.com/wCbQpOr.jpg",
            "http://i.imgur.com/rUcXDip.jpg",
            "http://i.imgur.com/bzuhIg4.png",
            "http://i.imgur.com/LsEW9kS.png",
            "http://i.imgur.com/MyAcXe5.png",
            "http://i.imgur.com/PwErqAf.png",
            "http://i.imgur.com/jz1zgXU.png",
            "http://i.imgur.com/8PQ43ov.jpg",
            "http://i.imgur.com/vxAIMJt.png",
            "http://i.imgur.com/ZXVlev9.jpg",
            "http://i.imgur.com/LT6RmQU.png",
            "http://i.imgur.com/8w0hWDS.jpg",
            "http://i.imgur.com/wCbQpOr.jpg",
            "http://i.imgur.com/LsEW9kS.png",
            "http://i.imgur.com/MyAcXe5.png",
            "http://i.imgur.com/PwErqAf.png",
            "http://i.imgur.com/jz1zgXU.png",
            "http://i.imgur.com/moer0PI.jpg",
            "http://i.imgur.com/vRUz3TD.jpg",
            "http://i.imgur.com/ZXVlev9.jpg",
            "http://i.imgur.com/LT6RmQU.png",
            "http://i.imgur.com/8w0hWDS.jpg",
            "http://i.imgur.com/wCbQpOr.jpg",
            "http://i.imgur.com/rUcXDip.jpg",
            "http://i.imgur.com/bzuhIg4.png",
            "http://i.imgur.com/LsEW9kS.png",
            "http://i.imgur.com/MyAcXe5.png",
            "http://i.imgur.com/PwErqAf.png",
            "http://i.imgur.com/jz1zgXU.png",
            "http://i.imgur.com/8PQ43ov.jpg",
            "http://i.imgur.com/vxAIMJt.png",
            "http://i.imgur.com/ZXVlev9.jpg",
            "http://i.imgur.com/LT6RmQU.png",
            "http://i.imgur.com/8w0hWDS.jpg",
            "http://i.imgur.com/wCbQpOr.jpg",
            "http://i.imgur.com/LsEW9kS.png",
            "http://i.imgur.com/MyAcXe5.png",
            "http://i.imgur.com/PwErqAf.png",
            "http://i.imgur.com/jz1zgXU.png",
            "http://i.imgur.com/moer0PI.jpg",
            "http://i.imgur.com/vRUz3TD.jpg",
            "http://i.imgur.com/ZXVlev9.jpg",
            "http://i.imgur.com/LT6RmQU.png",
            "http://i.imgur.com/8w0hWDS.jpg",
            "http://i.imgur.com/wCbQpOr.jpg",
            "http://i.imgur.com/rUcXDip.jpg",
            "http://i.imgur.com/bzuhIg4.png",
            "http://i.imgur.com/LsEW9kS.png",
            "http://i.imgur.com/MyAcXe5.png",
            "http://i.imgur.com/PwErqAf.png",
            "http://i.imgur.com/jz1zgXU.png",
            "http://i.imgur.com/8PQ43ov.jpg",
            "http://i.imgur.com/vxAIMJt.png",
            "http://i.imgur.com/ZXVlev9.jpg",
            "http://i.imgur.com/LT6RmQU.png",
            "http://i.imgur.com/8w0hWDS.jpg",
            "http://i.imgur.com/wCbQpOr.jpg",
            "http://i.imgur.com/LsEW9kS.png",
            "http://i.imgur.com/MyAcXe5.png",
            "http://i.imgur.com/PwErqAf.png",
            "http://i.imgur.com/jz1zgXU.png",
            "http://i.imgur.com/moer0PI.jpg",
            "http://i.imgur.com/vRUz3TD.jpg",
            "http://i.imgur.com/ZXVlev9.jpg",
            "http://i.imgur.com/LT6RmQU.png",
            "http://i.imgur.com/8w0hWDS.jpg",
            "http://i.imgur.com/wCbQpOr.jpg",
            "http://i.imgur.com/rUcXDip.jpg",
            "http://i.imgur.com/bzuhIg4.png",
            "http://i.imgur.com/LsEW9kS.png",
            "http://i.imgur.com/MyAcXe5.png",
            "http://i.imgur.com/PwErqAf.png",
            "http://i.imgur.com/jz1zgXU.png",
            "http://i.imgur.com/8PQ43ov.jpg",
            "http://i.imgur.com/vxAIMJt.png",
            "http://i.imgur.com/ZXVlev9.jpg",
            "http://i.imgur.com/LT6RmQU.png",
            "http://i.imgur.com/8w0hWDS.jpg",
            "http://i.imgur.com/wCbQpOr.jpg",
            "http://i.imgur.com/LsEW9kS.png",
            "http://i.imgur.com/MyAcXe5.png",
            "http://i.imgur.com/PwErqAf.png",
            "http://i.imgur.com/jz1zgXU.png",
            "http://i.imgur.com/moer0PI.jpg",
            "http://i.imgur.com/vRUz3TD.jpg",
            "http://i.imgur.com/ZXVlev9.jpg",
            "http://i.imgur.com/LT6RmQU.png",
            "http://i.imgur.com/8w0hWDS.jpg",
            "http://i.imgur.com/wCbQpOr.jpg",
            "http://i.imgur.com/rUcXDip.jpg",
            "http://i.imgur.com/bzuhIg4.png",
            "http://i.imgur.com/LsEW9kS.png",
            "http://i.imgur.com/MyAcXe5.png",
            "http://i.imgur.com/PwErqAf.png",
            "http://i.imgur.com/jz1zgXU.png",
            "http://i.imgur.com/8PQ43ov.jpg",
            "http://i.imgur.com/vxAIMJt.png",
            "http://i.imgur.com/ZXVlev9.jpg",
            "http://i.imgur.com/LT6RmQU.png",
            "http://i.imgur.com/8w0hWDS.jpg",
            "http://i.imgur.com/wCbQpOr.jpg",
            "http://i.imgur.com/LsEW9kS.png",
            "http://i.imgur.com/MyAcXe5.png",
            "http://i.imgur.com/PwErqAf.png",
            "http://i.imgur.com/jz1zgXU.png",
            "http://i.imgur.com/moer0PI.jpg",
            "http://i.imgur.com/vRUz3TD.jpg"
    };
    @BindView(R.id.list)
    ListView listView;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_image_layout);
        setTitle(getClass().getSimpleName());
        InjectionAccessory.shared().process(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final List<Track> tracks = getTrackList();

        final BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return tracks.size();
            }

            @Override
            public Object getItem(int position) {
                return tracks.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, final ViewGroup parent) {

                final ViewHolder holder;

                if (convertView == null) {
                    convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.net_image_layout, parent, false);
                    holder = new ViewHolder(convertView);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                String uri = tracks.get(position).getUrl();

                holder.progressBar.setProgress(0);
                holder.textView.setText("---");

                MediaAccessory.shared()
                        .cancel(holder.imageView)
                        .loadBitmap()
                        .from(uri)
                        .option(DisplayOption.bitmapBuilder()
                                .imageQuality(MediaQuality.OPT)
                                .viewMaybeReused()
                                .animateOnlyNewLoaded()
                                .imageAnimator(new FadeInViewAnimator())
                                .build())
                        .errorListener(new ErrorListener() {
                            @Override
                            public void onError(@NonNull Cause cause) {
                                LoggerManager.getLogger(getClass()).warn(cause);
                                holder.textView.setText(cause.toString());
                            }
                        })
                        .progressListener(new ProgressListener<Bitmap>() {
                            @Override
                            public void onStartLoading() {
                                holder.textView.setText("onStartLoading");
                                holder.progressBar.setProgress(0);
                            }

                            @Override
                            public void onProgressUpdate(float progress) {
                                holder.textView.setText("onProgressUpdate");
                                holder.progressBar.setProgress((int) progress * 100);
                            }

                            @Override
                            public void onCancel() {
                                holder.textView.setText("onCancel");
                                holder.progressBar.setProgress(0);
                            }

                            @Override
                            public void onComplete(Bitmap result) {
                                holder.textView.setText("onComplete");
                                holder.progressBar.setProgress(100);
                            }
                        })
                        .into(holder.imageView)
                        .priority(Priority.HIGH)
                        .start();

                return convertView;
            }
        };

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Track track = (Track) adapter.getItem(i);
                MediaAccessory.shared().cancel(track.getUrl());
            }
        });
    }

    List<Track> getTrackList() {
        List<Track> out = new ArrayList<>(urls.length);
        for (String s : urls) {
            Track t = new Track();
            t.setUrl(s);
            t.setTitle(s);
            out.add(t);
        }
        return out;
    }

    class ViewHolder implements BindView.RootViewProvider {
        @BindView(R.id.image)
        ImageView imageView;
        @BindView(R.id.progressBar)
        ProgressBar progressBar;
        @BindView(R.id.textView)
        TextView textView;

        View mRoot;

        public ViewHolder(View convert) {
            mRoot = convert;
            InjectionAccessory.shared().process(this);
        }

        @NonNull
        @Override
        public View getRootView() {
            return mRoot;
        }
    }
}
