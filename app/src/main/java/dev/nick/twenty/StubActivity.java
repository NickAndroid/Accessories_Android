package dev.nick.twenty;

import android.Manifest;
import android.annotation.SuppressLint;
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

import com.nick.scalpel.Scalpel;
import com.nick.scalpel.annotation.binding.FindView;
import com.nick.scalpel.annotation.binding.MainThreadHandler;
import com.nick.scalpel.annotation.request.RequirePermission;

import dev.nick.imageloader.ZImageLoader;
import dev.nick.imageloader.display.DisplayOption;

@RequirePermission(permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET})
public class StubActivity extends AppCompatActivity {

    @MainThreadHandler
    Handler mHandler;

    @FindView(id = R.id.list)
    ListView listView;

    final String urlDrawable = "drawable://ic_launcher";
    final String url2 = "http://tse2.mm.bing.net/th?id=OIP.M960c6796f4870a8764558c39e9148afao2&pid=15.1";

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stub);
        Scalpel.getInstance().wire(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return 100;
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

                @SuppressLint("ViewHolder")
                View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item, parent, false);

                ImageView imageView = (ImageView) v.findViewById(R.id.image);

                ZImageLoader.getInstance().displayImage(url2, imageView,

                        new DisplayOption(R.drawable.ic_broken_image_black_24dp, R.drawable.ic_cloud_download_black_24dp));

                return v;
            }

        };

        listView.setAdapter(adapter);
    }

}
