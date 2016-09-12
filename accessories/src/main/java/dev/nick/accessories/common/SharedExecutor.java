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

package dev.nick.accessories.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nick on 16-2-7.
 * Email: nick.guo.dev@icloud.com
 * Github: https://github.com/NickAndroid
 * <p/>
 * FIXME: Do not use this executor to do tasks that takes a lot of time.
 */
public class SharedExecutor {

    private static SharedExecutor sInstance;

    private ExecutorService mService;

    private SharedExecutor() {
        this.mService = Executors.newFixedThreadPool(Runtime
                .getRuntime().availableProcessors() / 2 + 1);
    }

    public static synchronized SharedExecutor get() {
        if (sInstance == null) sInstance = new SharedExecutor();
        return sInstance;
    }

    public static void terminate() {
        SharedExecutor service = get();
        if (service != null) service.shutdown();
    }

    public void execute(Runnable runnable) {
        mService.execute(runnable);
    }

    private void shutdown() {
        mService.shutdownNow();
    }
}
