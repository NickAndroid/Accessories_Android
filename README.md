# MediaAccessories
A more controllable MediaLoader contains Image, Movie that load bitmap, gif from anywhere with both disk and mem cache, and
a lot of ui effects.

## Arts

## Usage

[![](https://jitpack.io/v/NickAndroid/ImageLoader_Android.svg)](https://jitpack.io/#NickAndroid/ImageLoader_Android)
<a href="http://www.methodscount.com/?lib=dev.nick%3Aimageloader%3A1.4"><img src="https://img.shields.io/badge/Size-223 KB-e91e63.svg"/></a>

### Change log

## Samples

### How to init/get the accessory:
*  Use the shared(single) instance
```java
public class MyApp {
    @Override
    public void onCreate() {
        super.onCreate();
        // Create the shared instance
         MediaAccessory.createShared(getApplicationContext(), AccessoryConfig.builder()
                        .queuePolicy(QueuePolicy.LIFO)
                        .cachePolicy(CachePolicy.builder()
                                .enableMemCache()
                                .enableDiskCache()
                                .enableStorageStats()
                                .cacheDirName("dis.cache.tests")
                                .preferredLocation(CachePolicy.Location.EXTERNAL)
                                .compressFormat(Bitmap.CompressFormat.PNG)
                                .build())
                        .networkPolicy(NetworkPolicy.builder()
                                .onlyOnWifi()
                                .enableTrafficStats().build())
                        .debugLevel(Log.VERBOSE)
                        .build());
        // Some works
        MediaAccessory.shared().works...
    }
}
```
*  Create a new accessory with new config
```java
public class Z {
    @Override
    public void onCreate() {
        super.onCreate();
        // Fork a new instance
        MediaAccessory newOne = MediaAccessory.shared().fork(AccessoryConfig.builder()
                        .cachePolicy(CachePolicy.builder()
                                .enableMemCache()
                                .enableDiskCache()
                                .cachingThreads(Runtime.getRuntime().availableProcessors())
                                .cacheDirName("dis.cache.tests.content")
                                .preferredLocation(CachePolicy.Location.INTERNAL)
                                .compressFormat(Bitmap.CompressFormat.JPEG)
                                .build())
                        .networkPolicy(NetworkPolicy.builder().enableTrafficStats().build())
                        .build());
        // Some works
        accessory.works...
    }
}
```

### Loading or Display:
```java
 MediaAccessory.shared()
                .loadBitmap()
                .from(urlDrawable)
                .listener(listener)
                .option(DisplayOption.builder()...) // Ignore this param if your just want to load a bitmap.
                .into(imageView) // Ignore this param if your just want to load a bitmap.
                .start();
```

### CustomView support:
```java
class CustomView implements MediaHolder<X> {}
```
```java
MediaAccessory.shared()
                .loadMovie()
                .from(urlDrawable)
                .listener(listener)
                .option(DisplayOption.builder()...)
                .into(new CustomView())
                .start();
```

### Cancel tasks:
```java
mMediaAccessory.cancel(ImageView/ImageSettable/Url);
```

### Clear cache:
```java
 @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        mMediaAccessory.clearMemCache();
    }
```
```java
@Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaAccessory.clearAllCache();
    }
```

## Supported url:
- [x] http://
- [x] https://
- [x] file://
- [x] content://
- [x] drawable://
- [x] assets://

## Supported media format:
- [x] All image formats android supported.
- [ ] Video
- [x] Gif


## Contact me
**nick.guo.dev@icloud.com** :email:
