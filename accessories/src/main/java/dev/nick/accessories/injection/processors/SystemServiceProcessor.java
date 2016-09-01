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

package dev.nick.accessories.injection.processors;

import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.DownloadManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.app.admin.DevicePolicyManager;
import android.app.usage.NetworkStatsManager;
import android.appwidget.AppWidgetManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.hardware.fingerprint.FingerprintManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.UserManager;
import android.os.Vibrator;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.support.annotation.NonNull;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.CaptioningManager;
import android.view.inputmethod.InputMethodManager;

import com.google.guava.base.Preconditions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import dev.nick.accessories.injection.annotation.binding.SystemService;

class SystemServiceProcessor extends FieldProcessor {

    public SystemServiceProcessor(Context appContext) {
        super(appContext);
    }

    @Override
    protected Object parseField(Field field) {
        SystemService systemService = field.getAnnotation(SystemService.class);
        String serviceName = systemService.value();
        boolean nameValid = !TextUtils.isEmpty(serviceName);
        if (nameValid)
            //noinspection WrongConstant
            return getContext().getSystemService(serviceName);
        Known known = determineType(field.getType());
        Preconditions.checkNotNull(known, "Unknow service type, please set the service name manually.");
        switch (known) {
            case PM:
                return getContext().getSystemService(Context.POWER_SERVICE);
            case ACCOUNT:
                return getContext().getSystemService(Context.ACCOUNT_SERVICE);
            case ALARM:
                return getContext().getSystemService(Context.ALARM_SERVICE);
            case AM:
                return getContext().getSystemService(Context.ACTIVITY_SERVICE);
            case WM:
                return getContext().getSystemService(Context.WINDOW_SERVICE);
            case NM:
                return getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            case TM:
                return getContext().getSystemService(Context.TELEPHONY_SERVICE);
            case TCM:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    return getContext().getSystemService(Context.TELECOM_SERVICE);
                }
                sdkTooLow(field);
                break;
            case SP:
                return PreferenceManager.getDefaultSharedPreferences(getContext());
            case PKM:
                return getContext().getPackageManager();
            case ASM:
                return getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
            case CAP:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    return getContext().getSystemService(Context.CAPTIONING_SERVICE);
                }
                sdkTooLow(field);
                break;
            case KGD:
                return getContext().getSystemService(Context.KEYGUARD_SERVICE);
            case LOCATION:
                return getContext().getSystemService(Context.LOCATION_SERVICE);
            case SEARCH:
                return getContext().getSystemService(Context.SEARCH_SERVICE);
            case SENSOR:
                return getContext().getSystemService(Context.SENSOR_SERVICE);
            case STORAGE:
                return getContext().getSystemService(Context.STORAGE_SERVICE);
            case WALLPAPER:
                return getContext().getSystemService(Context.WALLPAPER_SERVICE);
            case VIBRATOR:
                return getContext().getSystemService(Context.VIBRATOR_SERVICE);
            case CONNECT:
                return getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            case NETWORK_STATUS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return getContext().getSystemService(Context.NETWORK_STATS_SERVICE);
                }
                sdkTooLow(field);
                break;
            case WIFI:
                return getContext().getSystemService(Context.WIFI_SERVICE);
            case AUDIO:
                return getContext().getSystemService(Context.AUDIO_SERVICE);
            case FP:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return getContext().getSystemService(Context.FINGERPRINT_SERVICE);
                }
                sdkTooLow(field);
                break;
            case MEDIA_ROUTER:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    return getContext().getSystemService(Context.MEDIA_ROUTER_SERVICE);
                }
                sdkTooLow(field);
                break;
            case SUB:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    return getContext().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                }
                sdkTooLow(field);
                break;
            case IME:
                return getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            case CLIP_BOARD:
                return getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            case APP_WIDGET:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    return getContext().getSystemService(Context.APPWIDGET_SERVICE);
                }
                sdkTooLow(field);
                break;
            case DEVICE_POLICY:
                return getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
            case DOWNLOAD:
                return getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            case BATTERY:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    return getContext().getSystemService(Context.BATTERY_SERVICE);
                }
                sdkTooLow(field);
                break;
            case NFC:
                return getContext().getSystemService(Context.NFC_SERVICE);
            case DISPLAY:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    return getContext().getSystemService(Context.DISPLAY_SERVICE);
                }
                sdkTooLow(field);
                break;
            case USER:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    return getContext().getSystemService(Context.USER_SERVICE);
                }
                sdkTooLow(field);
                break;
            case APP_OPS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    return getContext().getSystemService(Context.APP_OPS_SERVICE);
                }
                sdkTooLow(field);
                break;
        }
        return unSupported();
    }

    protected Known determineType(Class clz) {
        Known[] all = Known.values();
        for (Known t : all) {
            if (isTypeOf(clz, t.targetClass)) {
                return t;
            }
        }
        return null;
    }

    protected boolean isTypeOf(Class clz, Class target) {
        if (clz == target) return true;
        Class sup = clz.getSuperclass();
        return sup != null && isTypeOf(sup, target);
    }

    @NonNull
    @Override
    public Class<? extends Annotation> targetAnnotation() {
        return SystemService.class;
    }

    enum Known {
        AUTO(null),
        PM(PowerManager.class),
        PKM(PackageManager.class),
        WM(WindowManager.class),
        INFLATER(LayoutInflater.class),
        ACCOUNT(AccountManager.class),
        AM(ActivityManager.class),
        ASM(AccessibilityManager.class),
        CAP(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? CaptioningManager.class : NULL.class),
        KGD(KeyguardManager.class),
        LOCATION(LocationManager.class),
        SEARCH(SearchManager.class),
        SENSOR(SensorManager.class),
        STORAGE(StorageManager.class),
        WALLPAPER(WallpaperService.class),
        VIBRATOR(Vibrator.class),
        CONNECT(ConnectivityManager.class),
        NETWORK_STATUS(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? NetworkStatsManager.class : NULL.class),
        WIFI(WifiManager.class),
        AUDIO(AudioManager.class),
        FP(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? FingerprintManager.class : NULL.class),
        SUB(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 ? SubscriptionManager.class : NULL.class),
        IME(InputMethodManager.class),
        CLIP_BOARD(ClipboardManager.class),
        MEDIA_ROUTER(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ? MediaRouter.class : NULL.class),
        APP_WIDGET(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? AppWidgetManager.class : NULL.class),
        DEVICE_POLICY(DevicePolicyManager.class),
        DOWNLOAD(DownloadManager.class),
        BATTERY(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? BatteryManager.class : NULL.class),
        NFC(NfcManager.class),
        DISPLAY(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ? DisplayManager.class : NULL.class),
        USER(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ? UserManager.class : NULL.class),
        APP_OPS(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? AppOpsManager.class : NULL.class),
        ALARM(AlarmManager.class),
        NM(NotificationManager.class),
        TM(TelephonyManager.class),
        TCM(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? TelecomManager.class : NULL.class),
        SP(SharedPreferences.class),
        VIEW(android.view.View.class),
        BITMAP(Bitmap.class),
        COLOR(int.class),
        STRING(String.class),
        BOOL(boolean.class),
        INTEGER(int.class),
        STRING_ARRAY(String[].class),
        INT_ARRAY(int[].class);

        @NonNull
        public Class targetClass;

        Known(@NonNull Class targetClass) {
            this.targetClass = targetClass;
        }
    }

    interface NULL {
    }
}
