# ImageLoader
Android image accessory library


## Arts

## Usage

### Latest version:

[ ![JCenter](https://api.bintray.com/packages/nickandroid/maven/imageloader/images/download.svg) ](https://bintray.com/nickandroid/maven/imageloader/_latestVersion)
<a href="http://www.methodscount.com/?lib=dev.nick%3Aimageloader%3A1.3"><img src="https://img.shields.io/badge/Size-217 KB-e91e63.svg"/></a>
[![](https://jitpack.io/v/NickAndroid/ImageLoader_Android.svg)](https://jitpack.io/#NickAndroid/ImageLoader_Android)



### mvn:
```
<dependency>
  <groupId>dev.nick</groupId>
  <artifactId>imageloader</artifactId>
  <version>$latest</version>
  <type>pom</type>
</dependency>
```

### gradle
```
compile 'dev.nick:imageloader:$latest@aar'
```

## Samples

### How to init/get the accessory:
*  Use the shared(single) instance
```java
public class MyApp {
    @Override
    public void onCreate() {
        super.onCreate();
        // Create the shared instance
        ImageLoader.createShared(getApplicationContext(), new LoaderConfig.Builder()
                .cachePolicy(new CachePolicy.Builder()
                        .enableMemCache()
                        .enableDiskCache()
                        .cachingThreads(Runtime.getRuntime().availableProcessors())
                        .cacheDirName("tests")
                        .preferredLocation(CachePolicy.Location.EXTERNAL)
                        .compressFormat(Bitmap.CompressFormat.PNG)
                        .build())
                .debugLevel(Log.VERBOSE)
                .loadingThreads(Runtime.getRuntime().availableProcessors() * 2)
                .build());
        // Some works
        ImageLoader.shared().works...
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
        ImageLoader newLoader = ImageLoader.shared().fork(LoaderConfig.builder()
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
 ImageLoader.shared()
                .load()
                .from(urlDrawable)
                .listener(listener)
                .option(DisplayOption.builder()...) // Ignore this param if your just want to load a bitmap.
                .into(imageView) // Ignore this param if your just want to load a bitmap.
                .start();
```

### CustomView support:
```java
class CustomView implements ImageSettable {}
```
```java
ImageLoader.shared()
                .load()
                .from(urlDrawable)
                .listener(listener)
                .option(DisplayOption.builder()...)
                .into(new CustomView())
                .start();
```

### Cancel tasks:
```java
mLoader.cancel(ImageView/ImageSettable/Url);
```

### Clear cache:
```java
 @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        mLoader.clearMemCache();
    }
```
```java
@Override
    protected void onDestroy() {
        super.onDestroy();
        mLoader.clearAllCache();
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
- [ ] Gif


## Contact me
**nick.guo.dev@icloud.com** :email:
