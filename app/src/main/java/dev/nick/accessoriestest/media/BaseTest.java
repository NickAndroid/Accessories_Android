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

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import dev.nick.accessories.media.MediaLoader;
import dev.nick.accessoriestest.R;

public class BaseTest extends AppCompatActivity {

    boolean mPermRequired;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menus, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermRequired = true;
        for (int i : grantResults) {
            if (i != PackageManager.PERMISSION_GRANTED) {
                mPermRequired = false;
            }
        }
    }

    public boolean isPermRequired() {
        return mPermRequired;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pause:
                MediaLoader.shared().pause();
                break;
            case R.id.resume:
                MediaLoader.shared().resume();
                break;
            case R.id.terminate:
                MediaLoader.shared().terminate();
                break;
            case R.id.cancel:
                MediaLoader.shared().cancelAllTasks();
                break;
            case R.id.clear:
                MediaLoader.shared().clearAllCache();
                break;
            case R.id.clear_disc:
                MediaLoader.shared().clearDiskCache();
                break;
            case R.id.clear_mem:
                MediaLoader.shared().clearMemCache();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
