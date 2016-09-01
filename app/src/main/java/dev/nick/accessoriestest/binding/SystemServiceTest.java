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

package dev.nick.accessoriestest.binding;

import android.content.ServiceConnection;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;

import dev.nick.accessories.injection.annotation.binding.BindService;
import dev.nick.accessories.injection.annotation.binding.BindString;
import dev.nick.accessories.injection.annotation.binding.ServiceConnectionStub;
import dev.nick.accessories.injection.annotation.binding.SystemService;
import dev.nick.accessoriestest.R;
import dev.nick.accessoriestest.service.IMusicPlayerService;
import dev.nick.accessoriestest.service.MediaPlayerService;

public class SystemServiceTest extends AppCompatActivity {

    @SystemService(POWER_SERVICE)
    PowerManager powerManager;

    @BindString(R.string.app_name)
    int invalid;

    @BindService(clazz = MediaPlayerService.class,
            connectionStub = @ServiceConnectionStub("mConnection"))
    IMusicPlayerService mService;

    @BindService(action = "action.media.service",
            connectionStub = @ServiceConnectionStub("mConnection"))
    IMusicPlayerService mService2;

    @BindService(action = "action.media.service",
            pkgName = "dev.nick.accessoriestest",
            connectionStub = @ServiceConnectionStub("mConnection"))
    IMusicPlayerService mService3;

    ServiceConnection mConnection;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }
}
